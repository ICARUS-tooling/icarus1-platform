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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Launcher {
	
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new CentralExceptionHandler());
		
		// TODO does EDT exception handling still require this workaround to catch exceptions when in modal dialog?
		System.setProperty("sun.awt.exception.handler",	CentralExceptionHandler.class.getName()); //$NON-NLS-1$
		
		SplashWindow.splash(Launcher.class.getResource("IMS_Splash.png")); //$NON-NLS-1$
		
		SplashWindow.invokeMain("icarus.jar", "net.ikarus_systems.icarus.Core", args); //$NON-NLS-1$ //$NON-NLS-2$
		
		SplashWindow.disposeSplash();
	}
}
