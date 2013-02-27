/*
 * $Revision: 271 $
 * $Date: 2013-01-09 18:38:43 +0100 (Mi, 09 Jan 2013) $
 * $URL: https://subversion.assembla.com/svn/ims-studienarbeit/trunk/Hermes/net.ikarus_systems.hermes/net/ikarus_systems/hermes/registry/UnknownIdentifierException.java $
 *
 * $LastChangedDate: 2013-01-09 18:38:43 +0100 (Mi, 09 Jan 2013) $ 
 * $LastChangedRevision: 271 $ 
 * $LastChangedBy: mcgaerty $
 */

/**
 *
 */
package net.ikarus_systems.icarus.util;

/**
 * @author Markus Gärtner 
 * @version $Id: UnknownIdentifierException.java 271 2013-01-09 17:38:43Z mcgaerty $
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
