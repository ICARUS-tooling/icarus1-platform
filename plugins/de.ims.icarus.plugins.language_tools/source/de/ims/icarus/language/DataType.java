/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum DataType {
	
	/**
	 * Data that originated from some sort of automated generation
	 * process should be associated with this type.
	 */
	SYSTEM,

	/**
	 * Describes the gold standard, i.e. the one version
	 * of some data that is considered to define the "correct"
	 * state or annotation.
	 */
	GOLD,
	
	/**
	 * Describes data that was created by the user himself.
	 */
	USER,
}
