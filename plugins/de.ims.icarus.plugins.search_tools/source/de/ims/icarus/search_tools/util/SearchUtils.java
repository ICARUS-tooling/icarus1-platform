/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData;
import de.ims.icarus.plugins.search_tools.view.graph.ConstraintEdgeData;
import de.ims.icarus.plugins.search_tools.view.graph.ConstraintNodeData;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.ConstraintFactory;
import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.InvalidSearchGraphException;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchMode;
import de.ims.icarus.search_tools.SearchNode;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.SearchParameters;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultGraphEdge;
import de.ims.icarus.search_tools.standard.DefaultGraphNode;
import de.ims.icarus.search_tools.standard.DefaultSearchGraph;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Orientation;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SearchUtils implements LanguageConstants, SearchParameters {

	private SearchUtils() {
		// no-op
	}
	
	public static ContentType getConstraintNodeContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(ConstraintNodeData.class);
	}
	
	public static ContentType getConstraintEdgeContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(ConstraintEdgeData.class);
	}
	
	public static ContentType getConstraintCellContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(ConstraintCellData.class);
	}
	
	public static ContentType getSearchNodeContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(SearchNode.class);
	}
	
	public static ContentType getSearchEdgeContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(SearchEdge.class);
	}
	
	public static boolean isExhaustiveSearch(Search search) {
		return search.getParameters().get(SEARCH_MODE, DEFAULT_SEARCH_MODE).isExhaustive();
	}
	
	public static boolean isLeftToRightSearch(Search search) {
		return search.getParameters().get(SEARCH_ORIENTATION, DEFAULT_SEARCH_ORIENTATION)==Orientation.LEFT_TO_RIGHT;
	}
	
	public static boolean isOptimizedSearch(Search search) {
		return search.getParameters().getBoolean(OPTIMIZE_SEARCH, DEFAULT_OPTIMIZE_SEARCH);
	}
	
	public static boolean isCaseSensitiveSearch(Search search) {
		return search.getParameters().getBoolean(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE);
	}
	
	public static Object getDefaultSpecifier(ConstraintFactory factory) {
		Object[] specifiers = factory.getSupportedSpecifiers();
		return (specifiers!=null && specifiers.length>0) ?
				specifiers[0] : null;
	}
	
	/**
	 * Returns an exact copy of the given {@code SearchGraph} with
	 * all its constraints contained within nodes and edges instantiated
	 * using the {@code ConstraintFactory} implementations provided
	 * by the specified context. In addition the provided options are passed
	 * to the factories so one can create a copy of an existing search graph
	 * with new settings.
	 * <p>
	 * It is recommended that {@link SearchFactory} implementations make use
	 * of this method when creating the actual {@link Search} object so they
	 * can be sure that all constraints are properly instantiated and not
	 * plain instances of {@link DefaultConstraint}.
	 */
	public static SearchGraph instantiate(SearchGraph graph, ConstraintContext context, Options options) {
		if(graph==null)
			throw new InvalidSearchGraphException("Graph is null"); //$NON-NLS-1$
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
			clone.setConstraints(instantiate(node.getConstraints(), context, options));
			
			cloneMap.put(node, clone);
			nodes.add(clone);
		}
		
		for(SearchEdge edge : graph.getEdges()) {
			DefaultGraphEdge clone = new DefaultGraphEdge();
			clone.setId(edge.getId());
			clone.setNegated(edge.isNegated());
			clone.setEdgeType(edge.getEdgeType());
			clone.setConstraints(instantiate(edge.getConstraints(), context, options));
			
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
		result.setRootOperator(graph.getRootOperator());
		
		result.setRootNodes(roots.toArray(new SearchNode[0]));
		result.setNodes(nodes.toArray(new SearchNode[0]));
		result.setEdges(edges.toArray(new SearchEdge[0]));
		
		return result;
	}
	
	private static SearchConstraint[] instantiate(SearchConstraint[] constraints, 
			ConstraintContext context, Options options) {
		if(constraints==null) {
			return null;
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		List<SearchConstraint> result = new ArrayList<>();
		
		for(SearchConstraint constraint : constraints) {
			if(constraint==null || constraint.isUndefined() || !constraint.isActive()) {
				continue;
			}
			ConstraintFactory factory = context.getFactory(constraint.getToken());
			result.add(factory.createConstraint(
					constraint.getValue(), constraint.getOperator(),
					constraint.getSpecifier(), options));
		}
		
		return toArray(result);
	}
	
	public static String getResultStats(SearchResult searchResult) {
		if(searchResult==null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		int groupCount = searchResult.getDimension();
		sb.append(StringUtil.formatDecimal(groupCount)).append(" "); //$NON-NLS-1$
		sb.append(ResourceManager.getInstance().get(
				groupCount==1 ? "plugins.searchTools.labels.groupSg"  //$NON-NLS-1$
						: "plugins.searchTools.labels.groupPl")); //$NON-NLS-1$
		
		int matchCount = searchResult.getTotalMatchCount();
		sb.append(", ").append(StringUtil.formatDecimal(matchCount)).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(ResourceManager.getInstance().get(
				matchCount==1 ? "plugins.searchTools.labels.entrySg"  //$NON-NLS-1$
						: "plugins.searchTools.labels.entryPl")); //$NON-NLS-1$
		
		int hitCount = searchResult.getTotalHitCount();
		sb.append(", ").append(StringUtil.formatDecimal(hitCount)).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(ResourceManager.getInstance().get(
				hitCount==1 ? "plugins.searchTools.labels.hitSg"  //$NON-NLS-1$
						: "plugins.searchTools.labels.hitPl")); //$NON-NLS-1$
		
		return sb.toString();
	}
	
	public static String getQueryStats(SearchQuery searchQuery) {
		if(searchQuery==null) {
			return null;
		}
		
		if(isEmpty(searchQuery.getSearchGraph())) {
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
	
	public static String getParameterStats(Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		ResourceManager rm = ResourceManager.getInstance();
		StringBuilder sb = new StringBuilder();
		
		String yes = rm.get("yes"); //$NON-NLS-1$
		String no = rm.get("no"); //$NON-NLS-1$
		
		// Mode
		SearchMode mode = options.get(SEARCH_MODE, DEFAULT_SEARCH_MODE);
		sb.append(rm.get("plugins.searchTools.labels.searchMode")) //$NON-NLS-1$
			.append(": ").append(mode.getName()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// Orientation
		Orientation orientation = options.get(SEARCH_ORIENTATION, DEFAULT_SEARCH_ORIENTATION);
		sb.append(rm.get("plugins.searchTools.labels.orientation")) //$NON-NLS-1$
			.append(": ").append(orientation.getName()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// Case-Sensitive
		boolean caseSensitive = options.getBoolean(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE);
		sb.append(rm.get("plugins.searchTools.labels.caseSensitive")) //$NON-NLS-1$
			.append(": ").append(caseSensitive ? yes : no).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// Optimize
		boolean optimize = options.getBoolean(OPTIMIZE_SEARCH, DEFAULT_OPTIMIZE_SEARCH);
		sb.append(rm.get("plugins.searchTools.labels.optimize")) //$NON-NLS-1$
			.append(": ").append(optimize ? yes : no).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// Result Limit
		int resultLimit = options.getInteger(SEARCH_RESULT_LIMIT, DEFAULT_SEARCH_RESULT_LIMIT);
		String limit = resultLimit==0 ? "-" : String.valueOf(resultLimit); //$NON-NLS-1$
		sb.append(rm.get("plugins.searchTools.labels.resultLimit")) //$NON-NLS-1$
			.append(": ").append(limit); //$NON-NLS-1$
		
		return sb.toString();
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
	
	public static int getGroupId(SearchResult searchResult, int index) {
		if(searchResult==null) {
			return -1;
		}
		
		SearchConstraint constraint = searchResult.getGroupConstraint(index);
		
		return constraint==null ? -1 : (int) constraint.getValue();
	}


	public static List<SearchConstraint> cloneConstraints(List<SearchConstraint> constraints) {		
		if(constraints==null) {
			return null;
		}
		
		List<SearchConstraint> result = new ArrayList<>();
		
		for(SearchConstraint constraint : constraints) {
			result.add(constraint.clone());
		}
		
		return result;
	}
	
	public static SearchConstraint[] toArray(Collection<SearchConstraint> constraints) {
		return constraints==null ? null : constraints.toArray(
				new SearchConstraint[constraints.size()]);
	}
	
	public static int getMinInstanceCount(ConstraintFactory factory) {
		int value = factory.getMinInstanceCount();
		return value==-1 ? 1 : value;
	}
	
	public static int getMaxInstanceCount(ConstraintFactory factory) {
		int value = factory.getMaxInstanceCount();
		return value==-1 ? 9 : value;
	}
	
	public static SearchConstraint[] createDefaultConstraints(List<ConstraintFactory> factories) {
		List<SearchConstraint> constraints = new ArrayList<>();
		
		for(ConstraintFactory factory : factories) {
			int min = getMinInstanceCount(factory);
			int max = factory.getMaxInstanceCount();
			if(max!=-1 && max<min)
				throw new IllegalArgumentException("Max instance count of factory is too small: "+factory.getClass()); //$NON-NLS-1$
			
			SearchOperator operator = factory.getSupportedOperators()[0];
			
			for(int i=0; i<min; i++) {
				constraints.add(new DefaultConstraint(
						factory.getToken(), factory.getDefaultValue(), operator));
			}
		}
		
		return toArray(constraints);
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
	
	public static boolean isUndefined(SearchConstraint[] constraints) {
		if(constraints==null) {
			return true;
		}
		
		for(SearchConstraint constraint : constraints) {
			if(!constraint.isUndefined()) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isUndefined(SearchEdge edge) {
		if(edge==null) {
			return true;
		}
		
		return isUndefined(edge.getConstraints());
	}
	
	public static boolean isUndefined(SearchNode node) {
		if(node==null) {
			return true;
		}
		
		return isUndefined(node.getConstraints());
	}
	
	public static boolean searchIsReady(Search search) {
		if(search==null)
			throw new IllegalArgumentException("Invalid search"); //$NON-NLS-1$
		
		if(search.getTarget()==null) {
			return false;
		}
		if(search.getQuery()==null) {
			return false;
		}
		if(isEmpty(search.getQuery().getSearchGraph())) {
			return false;
		}
		
		return !search.isRunning() && !search.isDone();
	}
	
	public static Collection<ResultEntry> diffResults(SearchResult resultA, SearchResult resultB) {
		Set<ResultEntry> entriesA = getEntries(resultA);
		Set<ResultEntry> entriesB = getEntries(resultB);
		
		//System.out.println("entriesA ("+entriesA.size()+"): "+Arrays.toString(entriesA.toArray()));
		//System.out.println("entriesB ("+entriesB.size()+"): "+Arrays.toString(entriesB.toArray()));
		
		Set<ResultEntry> result = null;
		if(entriesA.size()>entriesB.size()) {
			entriesA.removeAll(entriesB);
			result = entriesA;
		} else {
			entriesB.removeAll(entriesA);
			result = entriesB;
		}
		
		return result;
	}
	
	public static Set<ResultEntry> getEntries(SearchResult searchResult) {
		int size = searchResult.getTotalMatchCount();
		Set<ResultEntry> entries = new HashSet<>(size);
		
		for(int i=0; i<size; i++) {
			entries.add(searchResult.getRawEntry(i));
		}
		
		return entries;
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
	
	public static final Comparator<ConstraintFactory> factorySorter = new Comparator<ConstraintFactory>() {

		@Override
		public int compare(ConstraintFactory o1, ConstraintFactory o2) {
			return o1.getToken().compareTo(o2.getToken());
		}
	};
}
