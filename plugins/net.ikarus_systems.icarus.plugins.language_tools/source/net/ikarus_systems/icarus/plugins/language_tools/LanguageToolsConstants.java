/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LanguageToolsConstants {
	
	// Plugin ID
	public static final String LANGUAGE_TOOLS_PLUGIN_ID = 
			"net.ikarus_systems.icarus.languageTools"; //$NON-NLS-1$
		
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
