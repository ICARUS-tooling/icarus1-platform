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
public enum NGramState {
	
	/**
	 * Search object has been initialized but not started.
	 */
	BLANK,
	
	/**
	 * Search has been started.
	 */
	RUNNING,
	
	/**
	 * Search performed without interruptions.
	 */
	FINISHED,
	
	/**
	 * Search got cancelled either by user decision or by
	 * errors.
	 */
	CANCELLED;

}
