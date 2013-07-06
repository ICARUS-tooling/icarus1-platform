/*
 * $Revision: 32 $
 * $Date: 2013-05-07 12:09:34 +0200 (Di, 07 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/SecureDispatcher.java $
 *
 * $LastChangedDate: 2013-05-07 12:09:34 +0200 (Di, 07 Mai 2013) $ 
 * $LastChangedRevision: 32 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.util;

/**
 * Helper class to perform unsafe operations that might throw
 * exceptions on other threads and to be able to receive those
 * exceptions later on the original thread. A typical usage
 * is the delegation of task execution to the {@code EventDispatchThread},
 * to wait for execution and then handle errors.
 * 
 * 
 * @author Markus Gärtner
 * @version $Id: SecureDispatcher.java 32 2013-05-07 10:09:34Z mcgaerty $
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
