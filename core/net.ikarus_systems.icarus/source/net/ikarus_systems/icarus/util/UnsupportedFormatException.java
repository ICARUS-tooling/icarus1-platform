/*
 * $Revision: 44 $
 * $Date: 2013-05-27 15:59:58 +0200 (Mo, 27 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/UnsupportedFormatException.java $
 *
 * $LastChangedDate: 2013-05-27 15:59:58 +0200 (Mo, 27 Mai 2013) $ 
 * $LastChangedRevision: 44 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.util;

/**
 * Signals that some data did not met the requirements defined
 * by some method or framework in terms of structural format.
 * For example a parser that reads data from a text-file would
 * use this exception to signal unexpected syntactical structures
 * he was unable to interpret or recognize.
 * 
 * @author Markus Gärtner 
 * @version $Id: UnsupportedFormatException.java 44 2013-05-27 13:59:58Z mcgaerty $
 *
 */
public class UnsupportedFormatException extends Exception {

	private static final long serialVersionUID = 4049528089908906476L;

	public UnsupportedFormatException() {
	}

	public UnsupportedFormatException(String message) {
		super(message);
	}

	public UnsupportedFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
