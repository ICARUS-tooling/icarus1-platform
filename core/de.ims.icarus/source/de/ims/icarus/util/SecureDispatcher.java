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
 * Helper class to perform unsafe operations that might throw
 * exceptions on other threads and to be able to receive those
 * exceptions later on the original thread. A typical usage
 * is the delegation of task execution to the {@code EventDispatchThread},
 * to wait for execution and then handle errors.
 * 
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class SecureDispatcher implements Runnable {

	private Exception exception;

	/**
	 * @param runnable
	 */
	public SecureDispatcher() {
		// no-op
	}

	/**
	 * Executes the {@link #runUnsafe()} method and catches all 
	 * exceptions for later processing.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		try {
			runUnsafe();
		} catch(Exception e) {
			exception = e;
		}
	}
	
	/**
	 * Wraps the execution of unsafe code. 
	 * @throws Exception
	 */
	protected abstract void runUnsafe() throws Exception;
	
	/**
	 * Returns the {@code Exception} object that was catched during
	 * the execution of {@link #runUnsafe()} or {@code null} if the
	 * method finished without errors.
	 * @return
	 */
	public Exception getException() {
		return exception;
	}
	
	public boolean hasException() {
		return exception!=null;
	}
}
