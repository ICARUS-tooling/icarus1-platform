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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils.Visitor;
import net.ikarus_systems.icarus.util.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintUnifier {
	
	private SearchGraph graph;
	private List<SearchConstraint> groupConstraints;
	private List<SearchConstraint> wrapper;

	public ConstraintUnifier(SearchGraph graph) {
		if(graph==null)
			throw new IllegalArgumentException("Invalid graph"); //$NON-NLS-1$
		
		this.graph = graph;
		
		if(graph.getRootOperator()==SearchGraph.OPERATOR_DISJUNCTION) {
			List<SearchNode> roots = CollectionUtils.asList(graph.getRootNodes());
			groupConstraints = aggregateGroupConstraints(null, roots);
		} else {
			groupConstraints = new ArrayList<>();
			for(SearchNode root : graph.getRootNodes()) {
				groupConstraints.addAll(collectGroupConstraints(root));
			}
		}
		
		Set<Integer> usedIndices = new HashSet<>();
		for(SearchConstraint constraint : groupConstraints) {
			int index = (int) constraint.getValue();
			if(usedIndices.contains(index))
				throw new IllegalArgumentException("Duplicate group index "+index+" at constraint "+constraint.getToken()); //$NON-NLS-1$ //$NON-NLS-2$
			usedIndices.add(index);
		}
	}
	
	public SearchGraph getGraph() {
		return graph;
	}
	
	public boolean hasGroupConstraints() {
		return groupConstraints!=null && !groupConstraints.isEmpty();
	}
	
	public List<SearchConstraint> getGroupConstraints() {
		if(wrapper==null) {
			wrapper = Collections.unmodifiableList(groupConstraints);
		}
		
		return wrapper;
	}
	
	public SearchConstraint[] toGroupArray() {
		return groupConstraints.toArray(new SearchConstraint[0]);
	}

	protected List<SearchConstraint> collectGroupConstraints(SearchNode node) {
		if(node.getNodeType()==NodeType.DISJUNCTION)
			throw new IllegalArgumentException();
		
		List<SearchConstraint> groupConstraints = new ArrayList<>();
		
		feedConstraints(groupConstraints, node.getConstraints());
		
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge.getEdgeType()==EdgeType.PRECEDENCE
					|| edge.getEdgeType()==EdgeType.LINK) {
				continue;
			}
			
			SearchNode target = edge.getTarget();
			
			if(target.getNodeType()==NodeType.DISJUNCTION) {
				// Ignore edge constraints on disjunctor nodes
				List<SearchNode> nodes = SearchUtils.getChildNodes(
						target, SearchUtils.regularEdges);
				
				if(nodes==null || nodes.isEmpty())
					throw new IllegalArgumentException();
				
				groupConstraints.addAll(aggregateGroupConstraints(node, nodes));
			} else {
				// Add constraints (including edge-constraints)
				feedConstraints(groupConstraints, edge.getConstraints());
				groupConstraints.addAll(collectGroupConstraints(target));
			}
		}
		
		return groupConstraints;
	}
	
	protected List<SearchConstraint> aggregateGroupConstraints(SearchNode parent, List<SearchNode> nodes) {
		List<List<SearchConstraint>> subLists = new ArrayList<>(nodes.size());
		
		// Unsorted collection of the constraints in the first subtree 
		List<SearchConstraint> result = null;
		String[] tokenSets = new String[nodes.size()];
		for(int i=0; i<nodes.size(); i++) {
			List<SearchConstraint> list = collectGroupConstraints(nodes.get(i)); 
			subLists.add(list);
			
			if(result==null) {
				result = new ArrayList<>(list);
			}
			
			Collections.sort(list, SearchUtils.constraintSorter);
			StringBuilder sb = new StringBuilder();
			for(Iterator<SearchConstraint> it = list.iterator(); it.hasNext(); ) {
				SearchConstraint constraint = it.next();
				sb.append(constraint.getToken()).append(constraint.getValue());
				if(it.hasNext()) {
					sb.append('_');
				}
			}
			tokenSets[i] = sb.toString();
		}
		
		// Simple way of checking if requirements are fulfilled
		if(!CollectionUtils.isUniform(tokenSets))
			throw new IllegalArgumentException(
					"Uniformity violation in sub-tree at node "+(parent==null ? "<root-list>" : parent.getId())); //$NON-NLS-1$ //$NON-NLS-2$
		
		return result;
	}
	
	protected int feedConstraints(List<SearchConstraint> list, SearchConstraint[] constraints) {
		if(constraints==null || constraints.length==0) {
			return 0;
		}
		
		int count = 0;
		
		for(SearchConstraint constraint : constraints) {
			if(SearchManager.isGroupingOperator(constraint.getOperator())) {
				list.add(constraint);
				count++;
			}
		}
		
		return count;
	}
	
	public static List<SearchConstraint> collectUnunifiedGroupConstraints(SearchGraph graph) {
		final List<SearchConstraint> result = new ArrayList<>();
		final Set<Integer> usedIndices = new HashSet<>();
		
		Visitor visitor = new Visitor() {
			
			@Override
			public void visit(SearchEdge edge) {
				if(edge.getEdgeType()!=EdgeType.PRECEDENCE
						&& edge.getEdgeType()!=EdgeType.LINK) {
					collectGroups(edge.getConstraints(), result, usedIndices);
				}
			}
			
			@Override
			public void visit(SearchNode node) {
				if(node.getNodeType()!=NodeType.DISJUNCTION) {
					collectGroups(node.getConstraints(), result, usedIndices);
				}
			}
		};
		
		SearchUtils.traverse(graph, visitor);
		
		return result;
	}
	
	private static void collectGroups(SearchConstraint[] constraints,
			List<SearchConstraint> list, Set<Integer> usedIndices) {
		if(constraints==null || constraints.length==0) {
			return;
		}
		
		for(SearchConstraint constraint : constraints) {
			if(SearchManager.isGroupingOperator(constraint.getOperator())
					&& !usedIndices.contains((int) constraint.getValue())) {
				list.add(constraint);
				usedIndices.add((int) constraint.getValue());
			}
		}
	}
}
