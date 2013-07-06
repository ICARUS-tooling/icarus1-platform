/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.errormining;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public interface ErrorMiningConstants {
	
	// Plugin ID
	public static final String ERRORMINING_PLUGIN_ID = 
			"net.ikarus_systems.icarus.errormining"; //$NON-NLS-1$
		
	// Perspective IDs
	public static final String ERRORMINING_PERSPECTIVE_ID = 
			ERRORMINING_PLUGIN_ID+"@ErrorMiningPerspective"; //$NON-NLS-1$
	
	// Event constants
	public static final String ERRORMINING_RESULT_VIEW_CHANGED = 
			"errorMiningTools:explorerSelectionChanged"; //$NON-NLS-1$
	
	
	// View IDs
	public static final String NGRAM_RESULT_VIEW_ID = 
			ERRORMINING_PLUGIN_ID+"@NGramResultView"; //$NON-NLS-1$


}
