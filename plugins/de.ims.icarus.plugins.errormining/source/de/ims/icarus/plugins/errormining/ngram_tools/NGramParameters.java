/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining.ngram_tools;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public interface NGramParameters {
	
	//TODO maybe with heuristics? 
	public static final String NGRAM_MODE = "ngramMode"; //$NON-NLS-1$ 

	public static final String NGRAM_RESULT_LIMIT = "ngramResultLimit"; //$NON-NLS-1$	
	
	public static final boolean USE_FRINGE_HEURISTIC = false;
	
	public static final String FRINGE_START = "fringeStart";  //$NON-NLS-1$
	
	public static final String FRINGE_END = "fringeEnd"; //$NON-NLS-1$

}
