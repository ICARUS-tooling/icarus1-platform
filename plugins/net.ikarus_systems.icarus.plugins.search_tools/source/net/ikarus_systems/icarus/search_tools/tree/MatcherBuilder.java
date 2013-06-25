/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.util.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatcherBuilder {
	
	protected final Search search;
	
	protected Map<TreeNode, Matcher> matcherMap;
	protected Map<Matcher, Matcher> cloneMap;
	protected Map<SearchNode, Integer> idMap;
	
	protected AtomicInteger idGen;
	
	protected Matcher rootMatcher;
	protected Matcher lastMatcher;

	public MatcherBuilder(Search search) {
		if(search==null)
			throw new IllegalArgumentException("Invalid search"); //$NON-NLS-1$
		
		this.search = search;
	}
	
	public Matcher createRootMatcher() {
		matcherMap = new HashMap<>();
		idMap = new HashMap<>();
		idGen = new AtomicInteger();
		
		SearchGraph graph = search.getQuery().getSearchGraph();
		boolean isDisjunction = graph.getRootOperator()==SearchGraph.OPERATOR_DISJUNCTION;
		
		TreeNode tree = new TreeNode();
		
		// Create tree
		for(SearchNode root : graph.getRootNodes()) {
			tree.addChild(createTree0(tree, root, null));
		}
		
		// Optimize tree
		for(TreeNode node : tree.getChildren()) {
			optimizeTree(node);
		}
		
		// Create all plain matchers
		for(int i=0; i<tree.getChildCount(); i++) {
			createMatcher0(null, tree.getChildAt(i), false);
		}
		
		// Now link matchers starting from root nodes
		if(isDisjunction) {
			Matcher last = null;
			for(TreeNode root : tree.getChildren()) {
				Matcher matcher = matcherMap.get(root);
				if(last!=null) {
					last.setAlternate(matcher);
				}
				last = matcher;
				
				lastMatcher = null;
				linkMatcher0(root);
			}
		} else {
			for(TreeNode root : tree.getChildren()) {
				linkMatcher0(root);
			}
		}
		
		return rootMatcher;
	}
	
	protected TreeNode createTree0(TreeNode parent, SearchNode node, SearchEdge head) {
		TreeNode treeNode = new TreeNode(parent);
		treeNode.setSearchNode(node);
		treeNode.setSearchEdge(head);
		
		int edgeCount = node.getOutgoingEdgeCount();
		for(int i=0; i<edgeCount; i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge.getEdgeType()==EdgeType.LINK
					|| edge.getEdgeType()==EdgeType.PRECEDENCE) {
				continue;
			}
			
			treeNode.addChild(createTree0(treeNode, edge.getTarget(), edge));
		}
		
		return treeNode;
	}
	
	protected void optimizeTree(TreeNode tree) {
		
		// Ensure each node has at most one disjunction child
		TreeNode disjunction = null;
		for(int i=0; i<tree.getChildCount(); i++) {
			TreeNode child = tree.getChildAt(i);
			
			if(child.isDisjunction()) {
				if(tree.isDisjunction())
					throw new IllegalStateException("Directly nested disjunction!"); //$NON-NLS-1$
				
				if(disjunction==null) {
					// Save first disjunction node
					disjunction = child;
				} else {
					// Append grandchildren to the single disjunction child
					tree.removeChildAt(i);
					i--;
					disjunction.addChildren(child.getChildren());
				}
			}
		}

		// Now process child nodes
		for(int i=0; i<tree.getChildCount(); i++) {
			optimizeTree(tree);
		}
		
		// Special handling for disjunction nodes
		if(tree.isDisjunction()) {
			List<TreeNode> negatedChildren = tree.getNegatedChildren();
			List<TreeNode> unnegatedChildren = tree.getUnnegatedChildren();
			
			int index = tree.indexInParent();
			TreeNode lastAlternate = tree.getParent();
			int alternateCount = 0;

			// Make sure that each negated disjunction member resides
			// in its own instance of the original parent tree
			if(negatedChildren!=null) {
				for(int i=0; i<negatedChildren.size(); i++) {
					TreeNode child = negatedChildren.get(i);
					if(alternateCount>0) {
						TreeNode nextAlternate = lastAlternate.clone();
						lastAlternate.setAlternate(nextAlternate);
						lastAlternate = nextAlternate;
					}
					
					lastAlternate.setChild(index, child);
					
					alternateCount++;
				}
			}
			
			// If the disjunction contained unnegated members
			// simply add them as a list of alternates
			if(unnegatedChildren!=null) {
				TreeNode leader = createAlternateSet(unnegatedChildren);

				if(alternateCount>0) {
					TreeNode nextAlternate = lastAlternate.clone();
					lastAlternate.setAlternate(nextAlternate);
					lastAlternate = nextAlternate;
				}
				
				lastAlternate.setChild(index, leader);
				
				alternateCount++;
			}
		}
	}
	
	protected TreeNode createAlternateSet(List<TreeNode> alternates) {
		if(alternates==null || alternates.isEmpty()) {
			return null;
		}
		
		for(int i=1; i<alternates.size(); i++) {
			alternates.get(i-1).setAlternate(alternates.get(i));
		}
		
		return alternates.get(0);
	}
	
	protected Matcher linkMatcher0(TreeNode treeNode) {
		
		Matcher matcher = matcherMap.get(treeNode);
		
		// Append matcher to pre-order list
		if(lastMatcher!=null) {
			lastMatcher.setNext(matcher);
		}
		lastMatcher = matcher;
		
		matcher.setId(getId(matcher.getNode()));
		
		for(int i=0; i<treeNode.getChildCount(); i++) {
			linkMatcher0(treeNode.getChildAt(i));
		}
		
		TreeNode alternate = treeNode.getAlternate();
		if(alternate!=null) {
			// Force clean entry point for alternate tree
			Matcher tmp = lastMatcher;
			lastMatcher = null;
			
			matcher.setAlternate(linkMatcher0(alternate));
			
			lastMatcher = tmp;
		}
		
		return matcher;
	}
	
	protected Matcher createMatcher0(Matcher parent, TreeNode treeNode, 
			boolean exclusionMember) {
		
		SearchNode node = treeNode.getSearchNode();
		SearchEdge edge = treeNode.getSearchEdge();
		
		exclusionMember |= node.isNegated();
		if(edge!=null) {
			exclusionMember |= edge.isNegated();
		}
	
		Matcher matcher;
		if(edge==null) {
			matcher = new RootMatcher(node);
		} else if(edge.getEdgeType()==EdgeType.TRANSITIVE) {
			matcher = new TransitiveMatcher(node, edge);
		} else {
			matcher = new Matcher(node, edge);
		}
		
		// Load constraints
		List<SearchConstraint> constraints = new ArrayList<>();
		if(edge!=null) {
			CollectionUtils.feedItems(constraints, edge.getConstraints());
		}
		CollectionUtils.feedItems(constraints, node.getConstraints());
		for(Iterator<SearchConstraint> i = constraints.iterator(); i.hasNext();) {
			if(i.next().isUndefined()) {
				i.remove();
			}
		}
		matcher.setConstraints(constraints.isEmpty() ? null : constraints.toArray(new SearchConstraint[0]));
		
		matcher.setExclusionMember(exclusionMember);
		matcher.setParent(parent);
		
		matcherMap.put(treeNode, matcher);
		
		// Build exclusion list if required
		List<TreeNode> negatedChildren = treeNode.getNegatedChildren();
		if(negatedChildren!=null) {
			Matcher[] exclusions = new Matcher[negatedChildren.size()];
			for(int i=0; i<negatedChildren.size(); i++) {
				exclusions[i] = createMatcher0(matcher, 
						negatedChildren.get(i), true);
			}
			
			matcher.setExclusions(exclusions);
			treeNode.removeChildren(negatedChildren);
		}
		
		// Process child nodes
		for(int i=0; i<treeNode.getChildCount(); i++) {
			createMatcher0(matcher, treeNode.getChildAt(i), exclusionMember);
		}
		
		return matcher;
	}
	
	protected int getId(SearchNode node) {
		Integer id = idMap.get(node);
		if(id==null) {
			id = idGen.getAndIncrement();
			idMap.put(node, id);
		}
		return id;
	}
	
	public Matcher cloneMatcher(Matcher matcher) {
		if(cloneMap==null) {
			cloneMap = new  HashMap<>();
		} else {
			cloneMap.clear();
		}
		
		return cloneMatcher0(matcher);
	}
	
	protected Matcher cloneMatcher0(Matcher matcher) {
		Matcher clone = cloneMap.get(matcher);
		if(clone==null) {
			// Create a shallow clone
			clone = matcher.clone();
			// Immediately save reference to clone to prevent
			// duplicates when cloning inner members
			cloneMap.put(matcher, clone);
			
			// Now do the deep cloning with respect to already cloned members
			clone.setParent(cloneMatcher0(matcher.getParent()));
			clone.setNext(cloneMatcher0(matcher.getNext()));
			clone.setAlternate(cloneMatcher0(matcher.getAlternate()));
			
			clone.setAfter(cloneMatchers(matcher.getAfter()));
			clone.setBefore(cloneMatchers(matcher.getBefore()));
		}
		return clone;
	}
	
	protected Matcher[] cloneMatchers(Matcher[] matchers) {
		if(matchers==null) {
			return null;
		}
		
		int size = matchers.length;
		Matcher[] clones = new Matcher[size];
		
		for(int i=0; i<size; i++) {
			clones[i] = cloneMatcher0(matchers[i]);
		}
		
		return clones;
	}
	
	protected static class TreeNode {
		protected SearchEdge searchEdge;
		protected SearchNode searchNode;
		
		protected TreeNode parent;
		protected TreeNode alternate;
		
		protected List<TreeNode> children;
		
		public TreeNode() {
			// no-op
		}
		
		public TreeNode(TreeNode parent) {
			setParent(parent);
		}
		
		public TreeNode getAlternate() {
			return alternate;
		}

		public void setAlternate(TreeNode alternate) {
			this.alternate = alternate;
			if(alternate!=null) {
				alternate.setParent(parent);
			}
		}

		public SearchEdge getSearchEdge() {
			return searchEdge;
		}

		public SearchNode getSearchNode() {
			return searchNode;
		}

		public TreeNode getParent() {
			return parent;
		}

		public void setSearchEdge(SearchEdge searchEdge) {
			this.searchEdge = searchEdge;
		}

		public void setSearchNode(SearchNode searchNode) {
			this.searchNode = searchNode;
		}

		public void setParent(TreeNode parent) {
			this.parent = parent;
			if(alternate!=null) {
				alternate.setParent(parent);
			}
		}
		
		public void addChild(TreeNode child) {
			if(children==null) {
				children = new ArrayList<>();
			}
			
			children.add(child);
			child.setParent(this);
		}
		
		public List<TreeNode> getChildren() {
			return children;
		}
		
		public int getChildCount() {
			return children==null ? 0 : children.size();
		}
		
		public TreeNode getChildAt(int index) {
			return children==null ? null : children.get(index);
		}
		
		public int indexInParent() {
			return parent==null ? -1 : parent.indexOfChild(this);
		}
		
		public void removeChildAt(int index) {
			if(children!=null) {
				TreeNode child = children.remove(index);
				if(child!=null) {
					child.setParent(null);
				}
			}
		}
		
		public void removeChild(TreeNode child) {
			if(children!=null) {
				children.remove(child);
				child.setParent(null);
			}
		}
		
		public void removeChildren(Collection<TreeNode> items) {
			if(items==null || children==null) {
				return;
			}
			
			children.removeAll(items);
			
			for(TreeNode child : items) {
				child.setParent(null);
			}
		}
		
		public int indexOfChild(TreeNode child) {
			return children==null ? -1 : children.indexOf(child);
		}
		
		public void setChild(int index, TreeNode child) {
			children.set(index, child);
			child.setParent(this);
		}
		
		public void insertChildAt(int index, TreeNode child) {
			children.add(index, child);
			child.setParent(this);
		}
		
		public void replaceChild(TreeNode oldChild, TreeNode newChild) {
			if(children==null) {
				return;
			}
			
			int index = children.indexOf(oldChild);
			children.set(index, newChild);
			oldChild.setParent(null);
			newChild.setParent(this);
		}
		
		public void addChildren(List<TreeNode> items) {
			if(items==null) {
				return;
			}
			if(children==null) {
				children = new ArrayList<>();
			}
			
			children.addAll(items);
			for(TreeNode child : items) {
				child.setParent(this);
			}
		}
		
		public boolean isNegated() {
			return searchNode.isNegated() || (searchEdge!=null && searchEdge.isNegated());
		}
		
		public boolean hasNegatedChild() {
			if(children!=null) {
				for(TreeNode child : children) {
					if(child.isNegated()) {
						return true;
					}
				}
			}
			return false;
		}
		
		public boolean isDisjunction() {
			return searchNode.getNodeType()==NodeType.DISJUNCTION;
		}
		
		public List<TreeNode> getNegatedChildren() {
			if(children==null) {
				return null;
			}
			
			List<TreeNode> result = null;
			
			for(TreeNode child : children) {
				if(child.isNegated()) {
					if(result==null) {
						result = new ArrayList<>();
					}
					result.add(child);
				}
			}
			
			return result;
		}
		
		public List<TreeNode> getUnnegatedChildren() {
			if(children==null) {
				return null;
			}
			
			List<TreeNode> result = null;
			
			for(TreeNode child : children) {
				if(!child.isNegated()) {
					if(result==null) {
						result = new ArrayList<>();
					}
					result.add(child);
				}
			}
			
			return result;
		}
		
		public TreeNode clone() {
			TreeNode clone = new TreeNode(parent);
			clone.searchNode = searchNode;
			clone.searchEdge = searchEdge;
			
			if(alternate!=null) {
				clone.alternate = alternate.clone();
			}
			
			if(children!=null) {
				clone.children = new ArrayList<>(children.size());
				for(TreeNode child : children) {
					clone.children.add(child.clone());
				}
			}
			
			return clone;
		}
	}

	protected static class PrecedencePair {
		private final SearchNode source, target;
		
		public PrecedencePair(SearchEdge edge) {
			this(edge.getSource(), edge.getTarget());
		}
		
		public PrecedencePair(SearchNode source, SearchNode target) {
			this.source = source;
			this.target = target;
		}

		public SearchNode getSource() {
			return source;
		}

		public SearchNode getTarget() {
			return target;
		}
	}
}
