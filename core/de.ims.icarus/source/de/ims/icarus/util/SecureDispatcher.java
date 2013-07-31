/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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
