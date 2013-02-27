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
package net.ikarus_systems.icarus.util.id;

/**
 * Signals an {@code id-clash} within a single name-space.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class DuplicateIdentifierException extends RuntimeException {

	private static final long serialVersionUID = -4034962554933011733L;

	/**
	 * @param message
	 */
	public DuplicateIdentifierException(String message) {
		super(message);
	}
}
