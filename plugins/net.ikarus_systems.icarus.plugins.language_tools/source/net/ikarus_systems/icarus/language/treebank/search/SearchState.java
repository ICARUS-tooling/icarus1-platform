/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank.search;

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
	FINISHED,
	
	/**
	 * Search got cancelled either by user decision or by
	 * errors.
	 */
	CANCELLED;
}