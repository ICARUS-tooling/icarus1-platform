/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

/**
 * Type safe definitions for the order possible bewteen two objects.  
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum Order {
	
	/**
	 * Indicates that for two objects {@code A} and {@code B} and order
	 * is defined such that {@code A} is placed <i>before</i> {@code B}.
	 */
	BEFORE,

	/**
	 * Indicates that for two objects {@code A} and {@code B} and order
	 * is defined such that {@code B} is placed <i>before</i> {@code A}.
	 */
	AFTER,
	
	/**
	 * No particular order exists between the two objects.
	 */
	UNDEFINED,

}
