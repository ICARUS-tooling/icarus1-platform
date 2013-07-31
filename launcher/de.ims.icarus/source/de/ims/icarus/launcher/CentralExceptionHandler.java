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
package de.ims.icarus.launcher;

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
