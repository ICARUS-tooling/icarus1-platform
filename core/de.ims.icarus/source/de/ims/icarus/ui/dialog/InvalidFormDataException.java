/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.dialog;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class InvalidFormDataException extends RuntimeException {

	private static final long serialVersionUID = 5985293057659915298L;

	public InvalidFormDataException() {
		// no-op
	}

	public InvalidFormDataException(String message) {
		super(message);
	}

	public InvalidFormDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
