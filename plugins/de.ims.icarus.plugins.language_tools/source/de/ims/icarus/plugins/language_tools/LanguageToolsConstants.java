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
package de.ims.icarus.plugins.language_tools;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LanguageToolsConstants {
	
	// Plugin ID
	public static final String LANGUAGE_TOOLS_PLUGIN_ID = 
			"de.ims.icarus.languageTools"; //$NON-NLS-1$
		
	// Perspective IDs
	public static final String TREEBANK_MANAGER_PERSPECTIVE_ID = 
			LANGUAGE_TOOLS_PLUGIN_ID+"@TreebankManagerPerspective"; //$NON-NLS-1$
	
	// View IDs
	public static final String TEXT_INPUT_VIEW_ID = 
			LANGUAGE_TOOLS_PLUGIN_ID+"@TextInputView"; //$NON-NLS-1$
	public static final String TREEBANK_EXPLORER_VIEW_ID = 
			LANGUAGE_TOOLS_PLUGIN_ID+"@TreebankExplorerView"; //$NON-NLS-1$
	public static final String TREEBANK_EDIT_VIEW_ID = 
			LANGUAGE_TOOLS_PLUGIN_ID+"@TreebankEditView"; //$NON-NLS-1$
	public static final String TREEBANK_PROPERTIES_VIEW_ID = 
			LANGUAGE_TOOLS_PLUGIN_ID+"@TreebankPropertiesView"; //$NON-NLS-1$

	// Event constants
	public static final String TREEBANK_EXPLORER_SELECTION_CHANGED = 
			"treebankTools:explorerSelectionChanged"; //$NON-NLS-1$
	
	// Category constants
	public static final String CATEGORY_EDITOR = "editor"; //$NON-NLS-1$
	public static final String CATEGORY_INPUT = "input"; //$NON-NLS-1$
	public static final String CATEGORY_GRAPH = "graph"; //$NON-NLS-1$
	public static final String CATEGORY_TABLE = "table"; //$NON-NLS-1$
	public static final String CATEGORY_CHOICE = "choice"; //$NON-NLS-1$
}
