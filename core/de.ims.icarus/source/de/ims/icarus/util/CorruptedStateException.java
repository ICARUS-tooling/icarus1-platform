/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/CorruptedStateException.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.util;

/**
 * Signals that some object reached a state in which it has no 
 * longer full control of its managed resources. This exception
 * is used for certain framework parts to forward encountered problems
 * in a way that enables the framework control to decide whether or
 * not a warning should be presented to the user suggesting him or her
 * to exit the program.
 * @author Markus Gärtner 
 * @version $Id: CorruptedStateException.java 7 2013-02-27 13:18:56Z mcgaerty $
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
