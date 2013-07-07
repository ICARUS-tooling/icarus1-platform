/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/id/DuplicateIdentifierException.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */

/**
 *
 */
package de.ims.icarus.util.id;

/**
 * Signals an {@code id-clash} within a single name-space.
 * 
 * @author Markus Gärtner 
 * @version $Id: DuplicateIdentifierException.java 7 2013-02-27 13:18:56Z mcgaerty $
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
