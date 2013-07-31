/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CorefConstants {

	// Plugin ID
	public static final String COREFERENCE_PLUGIN_ID = 
			"de.ims.icarus.coref"; //$NON-NLS-1$
		
	// Perspective IDs
	public static final String COREFERENCE_PERSPECTIVE_ID = 
			COREFERENCE_PLUGIN_ID+"@CoreferencePerspective"; //$NON-NLS-1$
	
	// View IDs
	public static final String COREFERENCE_MANAGER_VIEW_ID = 
			COREFERENCE_PLUGIN_ID+"@CoreferenceManagerView"; //$NON-NLS-1$
	public static final String COREFERENCE_EXPLORER_VIEW_ID = 
			COREFERENCE_PLUGIN_ID+"@CoreferenceExplorerView"; //$NON-NLS-1$
	public static final String COREFERENCE_DOCUMENT_VIEW_ID = 
			COREFERENCE_PLUGIN_ID+"@CoreferenceDocumentView"; //$NON-NLS-1$

	// Event constants
	public static final String DOCUMENT_EXPLORER_SELECTION_CHANGED = 
			"corefTools:explorerSelectionChanged"; //$NON-NLS-1$

}
