/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

/**
 * Signals that some object reached a state in which it has no 
 * longer full control of its managed resources. This exception
 * is used for certain framework parts to forward encountered problems
 * in a way that enables the framework control to decide whether or
 * not a warning should be presented to the user suggesting him or her
 * to exit the program.
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class CorruptedStateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 397341637226758734L;

	/**
	 * 
	 */
	public CorruptedStateException() {
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CorruptedStateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public CorruptedStateException(String message) {
		super(message);
	}

}
