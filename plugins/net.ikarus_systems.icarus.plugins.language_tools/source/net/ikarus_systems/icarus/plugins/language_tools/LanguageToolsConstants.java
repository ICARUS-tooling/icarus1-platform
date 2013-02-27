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
	public static final String CORPUS_MANAGER_PERSPECTIVE_ID = 
			"CorpusManagerPerspective"; //$NON-NLS-1$
	
	// View IDs
	public static final String DEFAULT_TEXT_INPUT_VIEW_ID = 
			"DefaultInputView"; //$NON-NLS-1$
	public static final String CORPUS_EXPLORER_VIEW_ID = 
			"CorpusExplorerView"; //$NON-NLS-1$
	public static final String CORPUS_EDIT_VIEW_ID = 
			"CorpusEditView"; //$NON-NLS-1$
	public static final String CORPUS_INSPECT_VIEW_ID = 
			"CorpusInspectView"; //$NON-NLS-1$
	public static final String CORPUS_PROPERTIES_VIEW_ID = 
			"CorpusPropertiesView"; //$NON-NLS-1$

	// Event constants
	public static final String CORPUS_EXPLORER_SELECTION_CHANGED = 
			"corpusTools:explorerSelectionChanged"; //$NON-NLS-1$
	
	// Category constants
	public static final String CATEGORY_EDITOR = "editor"; //$NON-NLS-1$
	public static final String CATEGORY_INPUT = "input"; //$NON-NLS-1$
	public static final String CATEGORY_GRAPH = "graph"; //$NON-NLS-1$
	public static final String CATEGORY_TABLE = "table"; //$NON-NLS-1$
	public static final String CATEGORY_CHOICE = "choice"; //$NON-NLS-1$
}
