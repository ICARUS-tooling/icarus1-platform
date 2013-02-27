/*
 * $Revision: 271 $
 * $Date: 2013-01-09 18:38:43 +0100 (Mi, 09 Jan 2013) $
 * $URL: https://subversion.assembla.com/svn/ims-studienarbeit/trunk/Hermes/net.ikarus_systems.hermes/net/ikarus_systems/hermes/registry/DuplicateIdentifierException.java $
 *
 * $LastChangedDate: 2013-01-09 18:38:43 +0100 (Mi, 09 Jan 2013) $ 
 * $LastChangedRevision: 271 $ 
 * $LastChangedBy: mcgaerty $
 */

/**
 *
 */
package net.ikarus_systems.icarus.util.id;

/**
 * Signals an {@code id-clash} within a single name-space.
 * 
 * @author Markus Gärtner 
 * @version $Id: DuplicateIdentifierException.java 271 2013-01-09 17:38:43Z mcgaerty $
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
