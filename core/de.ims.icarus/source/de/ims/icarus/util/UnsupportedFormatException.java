/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util;

/**
 * Signals that some data did not met the requirements defined
 * by some method or framework in terms of structural format.
 * For example a parser that reads data from a text-file would
 * use this exception to signal unexpected syntactical structures
 * he was unable to interpret or recognize.
 * 
 * @author Markus Gärtner 
 * @version $Id$
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
