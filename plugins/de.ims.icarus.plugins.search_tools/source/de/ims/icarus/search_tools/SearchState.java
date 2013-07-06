/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum SearchState {
	
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
	DONE,
	
	/**
	 * Search got cancelled either by user decision or by
	 * errors.
	 */
	CANCELLED;
}