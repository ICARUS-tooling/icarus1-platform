/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */

/**
 *
 */
package de.ims.icarus.util.id;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class UnknownIdentifierException extends RuntimeException {

	private static final long serialVersionUID = -5182797096921790100L;

	/**
	 * @param message
	 */
	public UnknownIdentifierException(String message) {
		super(message);
	}
}
