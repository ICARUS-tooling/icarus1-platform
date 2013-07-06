/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchToolsConstants {
	
	// Plugin ID
	public static final String SEARCH_TOOLS_PLUGIN_ID = 
			"net.ikarus_systems.icarus.searchTools"; //$NON-NLS-1$
		
	// Perspective IDs
	public static final String SEARCH_PERSPECTIVE_ID = 
			SEARCH_TOOLS_PLUGIN_ID+"@SearchPerspective"; //$NON-NLS-1$
	
	// View IDs
	public static final String QUERY_EDITOR_VIEW_ID = 
			SEARCH_TOOLS_PLUGIN_ID+"@QueryEditorView"; //$NON-NLS-1$
	public static final String SEARCH_MANAGER_VIEW_ID = 
			SEARCH_TOOLS_PLUGIN_ID+"@SearchManagerView"; //$NON-NLS-1$
	public static final String SEARCH_RESULT_VIEW_ID = 
			SEARCH_TOOLS_PLUGIN_ID+"@SearchResultView"; //$NON-NLS-1$

}
