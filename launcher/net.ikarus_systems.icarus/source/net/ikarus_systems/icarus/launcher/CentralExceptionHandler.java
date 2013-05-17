/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.launcher;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * 
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CentralExceptionHandler implements UncaughtExceptionHandler {
	
	private static UncaughtExceptionHandler handler;

	public CentralExceptionHandler() {
		// no-op
	}

	/**
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if(handler!=null) {
			handler.uncaughtException(t, e);
		} else {
			System.err.println("Exception on "+t.getName()); //$NON-NLS-1$
			e.printStackTrace();
		}
	}
	
	public void handle(Throwable thrown) {
		uncaughtException(Thread.currentThread(), thrown);
	}
	
	public static UncaughtExceptionHandler getHandler() {
		return handler;
	}
	
	public static void setHandler(UncaughtExceptionHandler newHandler) {
		handler = newHandler;
	}
}
