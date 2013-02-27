/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ManagementConstants {

	// Perspective IDs
	public static final String MANAGEMENT_PERSPECTIVE_ID = 
			"ManagementPerspective"; //$NON-NLS-1$

	// View IDs
	public static final String EXTENSION_POINT_OUTLINE_VIEW_ID = 
			"ExtensionPointOutlineView"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_HIERARCHY_VIEW_ID = 
			"ExtensionPointHierarchyView"; //$NON-NLS-1$
	public static final String PLUGIN_EXPLORER_VIEW_ID = 
			"PluginExplorerView"; //$NON-NLS-1$
	public static final String DEFAULT_LOG_VIEW_ID = 
			"DefaultLogView"; //$NON-NLS-1$
	public static final String DEFAULT_OUTPUT_VIEW_ID = 
			"DefaultOutputView"; //$NON-NLS-1$

	// Event constants	
	public static final String EXPLORER_SELECTION_CHANGED = 
			"management:explorerSelectionChanged"; //$NON-NLS-1$
	public static final String OUTLINE_CONTENT_CHANGED = 
			"management:outlineContentChanged"; //$NON-NLS-1$
	public static final String OUTLINE_SELECTION_CHANGED = 
			"management:outlineSelectionChanged"; //$NON-NLS-1$
	public static final String HIERARCHY_CONTENT_CHANGED = 
			"management:hierarchyContentChanged"; //$NON-NLS-1$
	public static final String HIERARCHY_SELECTION_CHANGED = 
			"management:hierarchySelectionChanged"; //$NON-NLS-1$
	public static final String LOG_SELECTION_CHANGED = 
			"management:logSelectionChanged"; //$NON-NLS-1$
	
	// Output constants
	public static final String REUSE_TAB_OPTION = "reuseTab"; //$NON-NLS-1$
	public static final String TITLE_OPTION = "title"; //$NON-NLS-1$
	public static final String OWNER_OPTION = "owner"; //$NON-NLS-1$
	

}
