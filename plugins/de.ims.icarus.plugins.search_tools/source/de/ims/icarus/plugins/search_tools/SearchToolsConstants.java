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
package de.ims.icarus.plugins.search_tools;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchToolsConstants {
	
	// Plugin ID
	public static final String SEARCH_TOOLS_PLUGIN_ID = 
			"de.ims.icarus.searchTools"; //$NON-NLS-1$
		
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
