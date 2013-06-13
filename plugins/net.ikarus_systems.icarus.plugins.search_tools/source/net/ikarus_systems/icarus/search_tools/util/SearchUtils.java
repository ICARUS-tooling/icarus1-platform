/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ikarus_systems.icarus.language.LanguageConstants;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.ConstraintContext;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchFactory;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;
import net.ikarus_systems.icarus.search_tools.standard.DefaultGraphEdge;
import net.ikarus_systems.icarus.search_tools.standard.DefaultGraphNode;
import net.ikarus_systems.icarus.search_tools.standard.DefaultSearchGraph;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SearchUtils implements LanguageConstants {

	private SearchUtils() {
		// no-op
	}
	
	/**
	 * Returns an exact copy of the given {@code SearchGraph} with
	 * all its constraints contained within nodes and edges instantiated
	 * using the {@code ConstraintFactory} implementations provided
	 * by the specified context. In addition the provided flags are passed
	 * to the factories so one can create a copy of an existing search graph
	 * with new settings.
	 * <p>
	 * it is recommended that {@link SearchFactory} implementations make use
	 * of this method when creating the actual {@link Search} object so they
	 * can be sure that all constraints are properly instantiated and not
	 * plain instances of {@link DefaultConstraint}.
	 */
	public static SearchGraph instantiate(SearchGraph graph, ConstraintContext context, int flags) {
		if(graph==null)
			throw new IllegalArgumentException("Invalid graph"); //$NON-NLS-1$
		if(context==null)
			throw new IllegalArgumentException("Invalid context"); //$NON-NLS-1$
		
		Map<SearchNode, DefaultGraphNode> cloneMap = new HashMap<>();
		
		List<DefaultGraphNode> nodes = new ArrayList<>();
		List<DefaultGraphEdge> edges = new ArrayList<>();
		List<DefaultGraphNode> roots = new ArrayList<>();
		
		for(SearchNode node : graph.getNodes()) {
			DefaultGraphNode clone = new DefaultGraphNode();
			clone.setId(node.getId());
			clone.setNegated(node.isNegated());
			clone.setNodeType(node.getNodeType());
			clone.setConstraints(instantiate(node.getConstraints(), context, flags));
			
			cloneMap.put(node, clone);
			nodes.add(clone);
		}
		
		for(SearchEdge edge : graph.getEdges()) {
			DefaultGraphEdge clone = new DefaultGraphEdge();
			clone.setId(edge.getId());
			clone.setNegated(edge.isNegated());
			clone.setEdgeType(edge.getEdgeType());
			clone.setConstraints(instantiate(edge.getConstraints(), context, flags));
			
			edges.add(clone);
			
			DefaultGraphNode target = cloneMap.get(edge.getTarget());
			DefaultGraphNode source = cloneMap.get(edge.getSource());
			
			clone.setSource(source);
			clone.setTarget(target);
			
			source.addEdge(clone, false);
			target.addEdge(clone, true);
		}
		
		for(SearchNode root : graph.getRootNodes()) {
			roots.add(cloneMap.get(root));
		}
		
		DefaultSearchGraph result = new DefaultSearchGraph();
		
		result.setRootNodes(roots.toArray(new SearchNode[0]));
		result.setNodes(nodes.toArray(new SearchNode[0]));
		result.setEdges(edges.toArray(new SearchEdge[0]));
		
		return result;
	}
	
	private static SearchConstraint[] instantiate(SearchConstraint[] constraints, 
			ConstraintContext context, int flags) {
		if(constraints==null) {
			return null;
		}
		
		SearchConstraint[] result = new SearchConstraint[constraints.length];
		
		for(int i=0; i<constraints.length; i++) {
			SearchConstraint constraint = constraints[i];
			ConstraintFactory factory = context.getFactory(constraint.getToken());
			result[i] = factory.createConstraint(
					constraint.getValue(), constraint.getOperator(), flags);
		}
		
		return result;
	}
	
	public static String getResultStats(SearchResult searchResult) {
		if(searchResult==null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		int groupCount = searchResult.getDimension();
		sb.append(groupCount).append(" "); //$NON-NLS-1$
		sb.append(ResourceManager.getInstance().get(
				groupCount==1 ? "plugins.searchTools.labels.groupSg"  //$NON-NLS-1$
						: "plugins.searchTools.labels.groupPl")); //$NON-NLS-1$
		
		int hitCount = searchResult.getTotalMatchCount();
		sb.append(", ").append(hitCount).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(ResourceManager.getInstance().get(
				groupCount==1 ? "plugins.searchTools.labels.entrySg"  //$NON-NLS-1$
						: "plugins.searchTools.labels.entryPl")); //$NON-NLS-1$
		
		return sb.toString();
	}
	
	public static String getQueryStats(SearchQuery searchQuery) {
		if(searchQuery==null) {
			return null;
		}
		
		String stats = getGraphStats(searchQuery.getSearchGraph());
		String query = searchQuery.getQueryString();
		if(stats==null && query!=null && !query.isEmpty()) {
			stats = ResourceManager.getInstance().get(
					"plugins.searchTools.queryStats", query.length()); //$NON-NLS-1$
		}
		
		return stats;
	}
	
	public static String getGraphStats(SearchGraph searchGraph) {
		if(searchGraph==null) {
			return null;
		}
		
		SearchNode[] nodes = searchGraph.getNodes();
		SearchEdge[] edges = searchGraph.getEdges();
		SearchNode[] roots = searchGraph.getRootNodes();
		
		int nodeCount = nodes==null ? 0 : nodes.length;
		int edgeCount = edges==null ? 0 : edges.length;
		int rootCount = roots==null ? 0 : roots.length;
		
		StringBuilder sb = new StringBuilder();

		sb.append(nodeCount).append(" "); //$NON-NLS-1$
		sb.append(ResourceManager.getInstance().get(
				nodeCount==1 ? "plugins.searchTools.labels.nodeSg"  //$NON-NLS-1$
						: "plugins.searchTools.labels.nodePl")); //$NON-NLS-1$

		sb.append(", ").append(edgeCount).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(ResourceManager.getInstance().get(
				edgeCount==1 ? "plugins.searchTools.labels.edgeSg"  //$NON-NLS-1$
						: "plugins.searchTools.labels.edgePl")); //$NON-NLS-1$
		
		sb.append(", ").append(rootCount).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(ResourceManager.getInstance().get(
				rootCount==1 ? "plugins.searchTools.labels.rootSg"  //$NON-NLS-1$
						: "plugins.searchTools.labels.rootPl")); //$NON-NLS-1$
		
		return sb.toString();
	}
	
	public static boolean asBoolean(Object value) {
		if(value instanceof Boolean) {
			return (boolean)value;
		} else {
			return Boolean.parseBoolean(value.toString());
		}
	}
	
	public static int asInteger(Object value) {
		if(value instanceof Integer) {
			return (int)value;
		} else {
			return Integer.parseInt(value.toString());
		}
	}
	
	public static double asDouble(Object value) {
		if(value instanceof Double) {
			return (double)value;
		} else {
			return Double.parseDouble(value.toString());
		}
	}
	
	public static long asLong(Object value) {
		if(value instanceof Long) {
			return (long)value;
		} else {
			return Long.parseLong(value.toString());
		}
	}
	
	public interface Visitor {
		void visit(SearchNode node);
		
		void visit(SearchEdge edge);
	}
	
	public static void traverse(SearchGraph graph, Visitor visitor) {
		if(graph==null)
			throw new IllegalArgumentException("Invalid graph"); //$NON-NLS-1$
		if(visitor==null)
			throw new IllegalArgumentException("Invalid visitor"); //$NON-NLS-1$
		
		Set<Object> visited = new HashSet<>();
		
		for(SearchNode root : graph.getRootNodes()) {
			traverse(root, visitor, visited);
		}
	}
	
	public static void traverse(SearchNode node, Visitor visitor) {
		if(node==null)
			throw new IllegalArgumentException("Invalid node"); //$NON-NLS-1$
		if(visitor==null)
			throw new IllegalArgumentException("Invalid visitor"); //$NON-NLS-1$
		
		Set<Object> visited = new HashSet<>();
		
		traverse(node, visitor, visited);
	}
	
	private static void traverse(SearchNode node, Visitor visitor, Set<Object> visited) {
		if(visited.contains(node)) {
			return;
		}
		
		visited.add(node);
		visitor.visit(node);
		
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			
			visitor.visit(edge);
			traverse(edge.getTarget(), visitor, visited);
		}
	}
	
	public static EnumSet<EdgeType> regularEdges = EnumSet.of(EdgeType.DOMINANCE, EdgeType.TRANSITIVE);
	public static EnumSet<EdgeType> allEdges = EnumSet.of(EdgeType.DOMINANCE, EdgeType.values());
	public static EnumSet<EdgeType> utilityEdges = EnumSet.of(EdgeType.LINK, EdgeType.PRECEDENCE);
	

	public static List<SearchNode> getChildNodes(SearchNode node) {
		return getChildNodes(node, null);
	}
	
	public static List<SearchNode> getChildNodes(SearchNode node, EnumSet<EdgeType> allowedEdges) {
		List<SearchNode> children = new ArrayList<>();
		
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(allowedEdges==null || allowedEdges.contains(edge.getEdgeType())) {
				children.add(edge.getTarget());
			}
		}
		
		return children;
	}
	
	public static SearchConstraint[] cloneSimple(SearchConstraint[] constraints) {
		if(constraints==null) {
			return null;
		}
		int size = constraints.length;
		SearchConstraint[] result = new SearchConstraint[size];
		
		for(int i=0; i<size; i++) {
			SearchConstraint constraint = constraints[i];
			result[i] = new DefaultConstraint(constraint.getToken(), 
					constraint.getValue(), constraint.getOperator());
		}
		
		return result;
	}
	
	public static SearchConstraint[] cloneConstraints(SearchConstraint[] source) {
		if(source==null) {
			return null;
		}

		int size = source.length;
		SearchConstraint[] newConstraints = new SearchConstraint[size];
		for(int i=0; i<size; i++) {
			newConstraints[i] = source[i].clone();
		}
		return newConstraints;
	}
	
	public static boolean isEmpty(SearchGraph graph) {
		return graph==null || graph.getRootNodes()==null || graph.getRootNodes().length==0;
	}
		
	public static final Comparator<SearchNode> nodeIdSorter = new Comparator<SearchNode>() {

		@Override
		public int compare(SearchNode o1, SearchNode o2) {
			return o1.getId().compareTo(o2.getId()); 
		}
		
	};
	
	public static final Comparator<SearchEdge> edgeIdSorter = new Comparator<SearchEdge>() {

		@Override
		public int compare(SearchEdge o1, SearchEdge o2) {
			return o1.getId().compareTo(o2.getId()); 
		}
		
	};
	
	public static final Comparator<SearchConstraint> constraintSorter = new Comparator<SearchConstraint>() {

		@Override
		public int compare(SearchConstraint o1, SearchConstraint o2) {
			return o1.getToken().compareTo(o2.getToken());
		}
	};
}