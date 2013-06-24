/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.corpus;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.ikarus_systems.icarus.io.Loadable;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchFactory;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.standard.GraphValidationResult;
import net.ikarus_systems.icarus.search_tools.standard.GraphValidator;
import net.ikarus_systems.icarus.search_tools.standard.GroupCache;
import net.ikarus_systems.icarus.search_tools.tree.TargetTree;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.util.CollectionUtils;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractCorpusSearch extends Search {
	
	protected AtomicInteger processedItems = new AtomicInteger();
	
	protected SentenceDataList corpus;
	
	protected Batch lastBatch;
	
	protected AbstractCorpusSearchResult result;
	
	protected int batchSize = 100;
	protected final int sourceSize;
	
	protected boolean disjuntiveRoots; 
	protected Set<Node> nodes = new HashSet<>();
	protected Set<Node> roots = new HashSet<>();
	protected Map<Node, Node> precedenceNodes = new HashMap<>();
	protected Map<SearchNode, Object> nodeMap = new HashMap<>();
	
	protected AbstractCorpusSearch(SearchFactory factory, SearchQuery query, 
			Object target, Options parameters) {
		super(factory, query, parameters, target);
		
		// TODO Apply parameters
		
		corpus = createTargetList(target);
		if(corpus==null)
			throw new IllegalArgumentException("Invalid corpus"); //$NON-NLS-1$
		
		sourceSize = corpus.size();
		
		disjuntiveRoots = query.getSearchGraph().getRootOperator()==SearchGraph.OPERATOR_DISJUNCTION;

		if(!SearchUtils.isEmpty(query.getSearchGraph())) {
			collectNodes();
			
			result = createResult();
		}
	}
	
	public SearchGraph getSearchGraph() {
		return getQuery().getSearchGraph();
	}
	
	protected void collectNodes() {
		for(SearchNode source : getSearchGraph().getRootNodes()) {
			roots.add(createNode(source, null, null));
		}
	}
	
	protected AbstractCorpusSearchResult createResult() {
		List<SearchConstraint> groupConstraints = null;
		
		try {
			// Try to unify group constraints
			groupConstraints = new ConstraintUnifier(getSearchGraph()).getGroupConstraints();
		} catch(Exception e) {
			LoggerFactory.log(this, Level.WARNING, 
					"Aggregation of group-constraints failed", e); //$NON-NLS-1$
		}
		
		/* If unifying the group constraints failed allow user
		 * to manually override and switch to raw collection of
		 * all existing group constraints (ignoring duplicates)
		 * 
		 * 'ok' will cause collection of all group constraints 
		 * without aggregation check
		 */
		if(groupConstraints==null) {
			if(DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.searchTools.graphValidation.title",  //$NON-NLS-1$
					"plugins.searchTools.graphValidation.ununifiedGroups")) { //$NON-NLS-1$
				groupConstraints = ConstraintUnifier.collectUnunifiedGroupConstraints(getSearchGraph());
			}
		}
		
		if(groupConstraints==null) {
			return null;
		}
		
		ContentType entryType = ContentTypeRegistry.getEntryType(getTarget());
		
		/* Allow user to run search with a dimension that is not
		 * covered by a specialized result presenter.
		 * 
		 * 'ok' will cause search to ignore group count limits
		 */
		int dimension = groupConstraints.size();
		List<Extension> presenters = SearchManager.getResultPresenterExtensions(
				entryType, dimension);
		if(presenters==null || presenters.isEmpty()) {
			if(!DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.searchTools.graphValidation.title",  //$NON-NLS-1$
					"plugins.searchTools.graphValidation.groupLimitExceeded",  //$NON-NLS-1$
					dimension)) {
				return null;
			}
		}
		
		/* Only distinguish between 0D and ND where N>0 since 0D
		 * can be implemented efficiently by using a simple list storage.
		 */
		if(groupConstraints.isEmpty()) {
			return new CorpusSearchResult0D(this);
		} else {
			return new CorpusSearchResultND(this, 
					groupConstraints.toArray(new SearchConstraint[0]));
		}
	}
	
	protected abstract TargetTree createTargetTree();
	
	protected abstract SentenceDataList createTargetList(Object target);

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#execute()
	 */
	@Override
	public boolean innerExecute() throws Exception {
		
		// TODO DEBUG
		/*TaskManager.getInstance().execute(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0; i<100; i++) {
					progress = i;
					
					if(isDone()) {
						return;
					}
					
					try {
						Thread.sleep(350);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				finish();
			}
		});*/
		
		if(result==null) {
			return false;
		}
		if(roots.isEmpty()) {
			return false;
		}
		if(corpus.size()==0) {
			return false;
		}
		
		Object target = getTarget();
		if(target instanceof Loadable && !((Loadable)target).isLoaded()) {
			try {
				((Loadable)target).load();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to load search target: "+target, e); //$NON-NLS-1$
				throw new IOException("Could not load traget - aborting", e); //$NON-NLS-1$
			}
		}
		
		// TODO
		
		return true;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#getPerformanceInfo()
	 */
	@Override
	public SearchPerformanceInfo getPerformanceInfo() {
		// TODO
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#getResult()
	 */
	@Override
	public SearchResult getResult() {
		return result;
	}
	
	public SentenceDataList getCorpus() {
		return corpus;
	}
	
	protected synchronized Batch nextBatch() {
		int start = 0;
		if(lastBatch!=null) {
			start = lastBatch.getStart()+lastBatch.getSize();
		}
		
		if(start<sourceSize) {
			lastBatch = new Batch(start, sourceSize-start);
			return lastBatch;
		} else {
			return null;
		}
	}
	
	protected synchronized void batchProcessed(Batch batch) {
		int processed = processedItems.addAndGet(batch.getSize());
		double total = corpus.size();
		setProgress((int)(processed/total * 100d));
	}
	
	protected Node createNode(SearchNode source, SearchEdge headEdge, Node head) {
		if(source.getNodeType()==NodeType.DISJUNCTION)
			throw new IllegalArgumentException("Cannot create node from disjunctor: "+source); //$NON-NLS-1$
		
		Node node = new Node();
		node.source = source;
		node.head = head;
		node.nodeType = source.getNodeType();
		
		// Save compressed constraints list (including head edge if present)
		List<SearchConstraint> constraints = new ArrayList<>();
		if(headEdge!=null) {
			CollectionUtils.feedItems(constraints, headEdge.getConstraints());
			node.edgeType = headEdge.getEdgeType();
		}
		CollectionUtils.feedItems(constraints, source.getConstraints());
		for(Iterator<SearchConstraint> i = constraints.iterator(); i.hasNext();) {
			if(i.next().isUndefined()) {
				i.remove();
			}
		}
		node.constraints = constraints.isEmpty() ? null : constraints.toArray(new SearchConstraint[0]);
		
		// Traverse outgoing edges and create children
		List<Node> excludings = new LinkedList<>();
		List<Node> requirements = new LinkedList<>();
		List<Disjunction> disjunctions = new LinkedList<>();
		for(int i=0; i<source.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = source.getOutgoingEdgeAt(i);
			SearchNode target = edge.getTarget();
			
			if(target.getNodeType()==NodeType.DISJUNCTION) {
				Disjunction disjunction = createDisjunction(target, edge, node);
				disjunctions.add(disjunction);
			} else if(edge.getEdgeType()==EdgeType.DOMINANCE
					|| edge.getEdgeType()==EdgeType.TRANSITIVE) {
				Node child = createNode(target, edge, node);
				if(!edge.isNegated() && !target.isNegated()) {
					requirements.add(child);
				} else {
					excludings.add(child);
				}
			}
		}
		node.excludings = excludings.isEmpty() ? null : excludings.toArray(new Node[0]);
		node.requirements = requirements.isEmpty() ? null : requirements.toArray(new Node[0]);
		node.disjunctions = disjunctions.isEmpty() ? null : disjunctions.toArray(new Disjunction[0]);
		
		// Calculate height and descendant counts
		int descendantCount = 0;
		int height = 1;
		for(Node child : requirements) {
			descendantCount += 1 + child.descendantCount;
			height = Math.max(height, child.height);
		}
		for(Disjunction disjunction : disjunctions) {
			descendantCount += disjunction.descendantCount;
			height = Math.max(height, disjunction.height);
		}
		node.height = 1 + height;
		node.descendantCount = descendantCount;
		
		nodeMap.put(source, node);
		
		return node;
	}
	
	protected Disjunction createDisjunction(SearchNode source, SearchEdge headEdge, Node head) {
		if(source.getNodeType()!=NodeType.DISJUNCTION)
			throw new IllegalArgumentException("Cannot create disjunction from node: "+source); //$NON-NLS-1$
		
		Disjunction disjunction = new Disjunction();
		disjunction.source = source;
		disjunction.head = head;

		// Traverse outgoing edges and create children
		List<Node> excludings = new LinkedList<>();
		List<Node> options = new LinkedList<>();
		for(int i=0; i<source.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = source.getOutgoingEdgeAt(i);
			SearchNode target = edge.getTarget();
			
			if(target.getNodeType()==NodeType.DISJUNCTION) {
				throw new IllegalStateException("Nested disjunction"); //$NON-NLS-1$
			} else if(edge.getEdgeType()==EdgeType.DOMINANCE
					|| edge.getEdgeType()==EdgeType.TRANSITIVE) {
				Node child = createNode(target, edge, head);
				
				boolean negated = source.isNegated();
				if(edge.isNegated()) {
					negated = !negated;
				}
				if(target.isNegated()) {
					negated = !negated;
				}
				
				if(negated) {
					excludings.add(child);
				} else {
					options.add(child);
				}
			}
		}
		
		if(excludings.isEmpty() && options.isEmpty())
			throw new IllegalArgumentException("Empty disjunction at node "+source.getId()); //$NON-NLS-1$
		
		disjunction.excludings = excludings.isEmpty() ? null : excludings.toArray(new Node[0]);
		disjunction.options = options.isEmpty() ? null : options.toArray(new Node[0]);

		// Calculate height and descendant counts
		if(!options.isEmpty()) {
			int descendantCount = Integer.MAX_VALUE;
			int height = 1;
			for(Node child : options) {
				descendantCount = Math.min(descendantCount, child.descendantCount);
				height = Math.min(height, child.height);
			}
			disjunction.height = height;
			disjunction.descendantCount = descendantCount;
		} else {
			disjunction.height = 0;
			disjunction.descendantCount = 0;
		}
		
		nodeMap.put(source, disjunction);
		
		return disjunction;
	}
	
	protected Matcher[] createMatchers(Node[] nodes, Matcher parent) {
		if(nodes==null) {
			return null;
		}
		
		int size = nodes.length;
		Matcher[] matchers = new Matcher[size];
		
		for(int i=0; i<size; i++) {
			matchers[i] = createMatcher(nodes[i], parent);
		}
		
		return matchers;
	}
	
	protected Matcher[] createMatchers(Disjunction[] disjunctions, Matcher parent) {
		if(disjunctions==null) {
			return null;
		}
		
		int size = disjunctions.length;
		Matcher[] matchers = new Matcher[size];
		
		for(int i=0; i<size; i++) {
			matchers[i] = createMatcher(disjunctions[i], parent);
		}
		
		return matchers;
	}
	
	protected Matcher createMatcher(Node node, Matcher parent) {
		// TODO distinguish between node types and create specialized matchers!
		NodeMatcher matcher = new NodeMatcher(node, parent);
		
		matcher.setRequirements(createMatchers(node.requirements, matcher));
		matcher.setExcludings(createMatchers(node.excludings, matcher));
		matcher.setDisjunctions(createMatchers(node.disjunctions, matcher));
		
		return matcher;
	}
	
	protected Matcher createMatcher(Disjunction disjunction, Matcher parent) {
		DisjunctionMatcher matcher = new DisjunctionMatcher(disjunction, parent);
		
		matcher.setOptions(createMatchers(disjunction.options, matcher));
		matcher.setExcludings(createMatchers(disjunction.excludings, matcher));
		
		return matcher;
	}
	
	/**
	 * Basic container of search related data associated with a single node
	 * and its incoming head-edge (optional).
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected static class Node {
		protected SearchNode source;
		// Parent node or null if root of a search-tree
		protected Node head;
		// Minimized constraint list for this node, 
		// guaranteed to be either a grouping constraint
		// or a not-undefined constraint of arbitrary type
		protected SearchConstraint[] constraints;
		// List of child nodes whose existence would cause
		// a negative match
		protected Node[] excludings;
		// Required child nodes that are to be found in the
		// target tree to result in a positive match
		protected Node[] requirements;
		// List of disjunctive child nodes divided into groups.
		protected Disjunction[] disjunctions;
		// Node type (all node-types excluding DISJUNCTION)
		protected NodeType nodeType;
		// Edge type of head-edge (DOMINANCE or TRANSITIVE)
		protected EdgeType edgeType;
		// Number of descendants a target-node is required to have
		protected int descendantCount;
		// Height of search-tree this node spans (maximum of all child heights +1)
		protected int height;
		//
		protected int filterStrength = -1;
		protected int minIndex = -1;
		protected int maxIndex = -1;
	}
	
	protected static class Disjunction {
		protected SearchNode source;
		// Node this disjunction serves as a child of
		protected Node head;
		// List of nodes that are not allowed to be present in a target child list
		protected Node[] excludings;
		// List of optional "one-of" nodes. At least one of them has to match
		// a child node in the target list to yield a positive result.
		protected Node[] options;
		protected int descendantCount;
		protected int height;
		protected int filterStrength = -1;
	}
	
	protected static class Batch {
		final int start;
		final int size;
		
		public Batch(int start, int size) {
			this.start = start;
			this.size = size;
		}

		public int getStart() {
			return start;
		}

		public int getSize() {
			return size;
		}
	}

	protected abstract class SearchWorker implements Runnable {
		
		protected final TargetTree targetTree;
		protected final GroupCache cache;
		
		protected SearchWorker() {
			targetTree = createTargetTree();
			cache = result.createCache();
		}
		
		protected Batch currentBatch;
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while((currentBatch=nextBatch()) != null) {
				// TODO
			}
		}
	}
	
	protected abstract class Matcher {
		private GroupCache cache;
		private TargetTree targetTree;
		
		private final Matcher parent;
		
		protected Matcher(Matcher parent) {
			this.parent = parent;
		}
		
		public abstract boolean matches();

		public GroupCache getCache() {
			return cache;
		}

		public TargetTree getTargetTree() {
			return targetTree;
		}

		public Matcher getParent() {
			return parent;
		}

		public void setCache(GroupCache cache) {
			this.cache = cache;
		}

		public void setTargetTree(TargetTree targetTree) {
			this.targetTree = targetTree;
		}
	}
	
	protected class NodeMatcher extends Matcher {
		protected final Node node;
		protected final SearchConstraint[] constraints;
		
		protected Matcher[] requirements;
		protected Matcher[] excludings;
		protected Matcher[] disjunctions;
		
		protected boolean lastToMatch = false;
		
		public NodeMatcher(Node node, Matcher parent) {
			super(parent);
			
			this.node = node;
			
			constraints = SearchUtils.cloneConstraints(node.constraints);
		}
		
		public void setRequirements(Matcher[] matchers) {
			requirements = matchers;
		}
		
		public void setExcludings(Matcher[] matchers) {
			excludings = matchers;
		}
		
		public void setDisjunctions(Matcher[] matchers) {
			disjunctions = matchers;
		}
		
		public void setLastToMatch(boolean value) {
			lastToMatch = value;
		}
		
		/**
		 * @see net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearch.Matcher#matches()
		 */
		@Override
		public boolean matches() {
		}
		
		protected boolean matchesConstraints() {
			if(constraints==null) {
				return true;
			}
			
			for(SearchConstraint constraint : constraints) {
				if(!constraint.matches(getTargetTree())) {
					return false;
				}
			}
			
			return true;
		}

		@Override
		public void setCache(GroupCache cache) {
			super.setCache(cache);
			
			if(excludings!=null) {
				for(Matcher matcher : excludings) {
					matcher.setCache(cache);
				}
			}
			
			if(requirements!=null) {
				for(Matcher matcher : requirements) {
					matcher.setCache(cache);
				}
			}
			
			if(disjunctions!=null) {
				for(Matcher matcher : disjunctions) {
					matcher.setCache(cache);
				}
			}
		}

		@Override
		public void setTargetTree(TargetTree targetTree) {
			super.setTargetTree(targetTree);
			
			if(excludings!=null) {
				for(Matcher matcher : excludings) {
					matcher.setTargetTree(targetTree);
				}
			}
			
			if(requirements!=null) {
				for(Matcher matcher : requirements) {
					matcher.setTargetTree(targetTree);
				}
			}
			
			if(disjunctions!=null) {
				for(Matcher matcher : disjunctions) {
					matcher.setTargetTree(targetTree);
				}
			}
		}
	}
	
	protected class DisjunctionMatcher extends Matcher {
		protected final Disjunction disjunction;
		
		protected Matcher[] excludings;
		protected Matcher[] options;
		
		protected DisjunctionMatcher(Disjunction disjunction, Matcher parent) {
			super(parent);
			
			this.disjunction = disjunction;
		}
		
		public void setExcludings(Matcher[] matchers) {
			excludings = matchers;
		}
		
		public void setOptions(Matcher[] matchers) {
			options = matchers;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearch.Matcher#matches()
		 */
		@Override
		public int matches() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setCache(GroupCache cache) {
			super.setCache(cache);
			
			if(excludings!=null) {
				for(Matcher matcher : excludings) {
					matcher.setCache(cache);
				}
			}
			
			if(options!=null) {
				for(Matcher matcher : options) {
					matcher.setCache(cache);
				}
			}
		}

		@Override
		public void setTargetTree(TargetTree targetTree) {
			super.setTargetTree(targetTree);
			
			if(excludings!=null) {
				for(Matcher matcher : excludings) {
					matcher.setTargetTree(targetTree);
				}
			}
			
			if(options!=null) {
				for(Matcher matcher : options) {
					matcher.setTargetTree(targetTree);
				}
			}
		}
	}
}
