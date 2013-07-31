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
package de.ims.icarus.plugins.core;

import de.ims.icarus.plugins.PluginUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ManagementConstants {

	// Perspective IDs
	public static final String MANAGEMENT_PERSPECTIVE_ID = 
			PluginUtil.CORE_PLUGIN_ID+"@ManagementPerspective"; //$NON-NLS-1$

	// View IDs
	public static final String EXTENSION_POINT_OUTLINE_VIEW_ID = 
			PluginUtil.CORE_PLUGIN_ID+"@ExtensionPointOutlineView"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_HIERARCHY_VIEW_ID = 
			PluginUtil.CORE_PLUGIN_ID+"@ExtensionPointHierarchyView"; //$NON-NLS-1$
	public static final String PLUGIN_EXPLORER_VIEW_ID = 
			PluginUtil.CORE_PLUGIN_ID+"@PluginExplorerView"; //$NON-NLS-1$
	public static final String DEFAULT_LOG_VIEW_ID = 
			PluginUtil.CORE_PLUGIN_ID+"@DefaultLogView"; //$NON-NLS-1$
	public static final String DEFAULT_OUTPUT_VIEW_ID = 
			PluginUtil.CORE_PLUGIN_ID+"@DefaultOutputView"; //$NON-NLS-1$
	public static final String TABLE_VIEW_ID = 
			PluginUtil.CORE_PLUGIN_ID+"@TableView"; //$NON-NLS-1$

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
	

}
