/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/id/UnknownIdentifierException.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */

/**
 *
 */
package de.ims.icarus.util.id;

/**
 * @author Markus Gärtner 
 * @version $Id: UnknownIdentifierException.java 23 2013-04-17 12:39:04Z mcgaerty $
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
