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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Launcher {
	
	public static void main(String[] args) {
		//Thread.setDefaultUncaughtExceptionHandler(new CentralExceptionHandler());
		
		// TODO does EDT exception handling still require this workaround to catch exceptions when in modal dialog?
		System.setProperty("sun.awt.exception.handler",	CentralExceptionHandler.class.getName()); //$NON-NLS-1$
		
		SplashWindow.splash(Launcher.class.getResource("ICARUS_Splash.png")); //$NON-NLS-1$
		
		SplashWindow.invokeMain("lib/icarus.core.jar", "de.ims.icarus.Core", args); //$NON-NLS-1$ //$NON-NLS-2$
		
		SplashWindow.disposeSplash();
	}
}
