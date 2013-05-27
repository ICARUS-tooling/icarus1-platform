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

import net.ikarus_systems.icarus.language.LanguageConstants;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SearchUtils implements LanguageConstants {

	private SearchUtils() {
		// no-op
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
}
