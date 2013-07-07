/*
 * $Revision: 56 $
 * $Date: 2013-07-03 18:16:44 +0200 (Mi, 03 Jul 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/launcher/de.ims.icarus/source/net/ikarus_systems/icarus/launcher/Launcher.java $
 *
 * $LastChangedDate: 2013-07-03 18:16:44 +0200 (Mi, 03 Jul 2013) $ 
 * $LastChangedRevision: 56 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.launcher;

/**
 * @author Markus Gärtner
 * @version $Id: Launcher.java 56 2013-07-03 16:16:44Z mcgaerty $
 *
 */
public class Launcher {
	
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new CentralExceptionHandler());
		
		// TODO does EDT exception handling still require this workaround to catch exceptions when in modal dialog?
		System.setProperty("sun.awt.exception.handler",	CentralExceptionHandler.class.getName()); //$NON-NLS-1$
		
		SplashWindow.splash(Launcher.class.getResource("ICARUS_Splash.png")); //$NON-NLS-1$
		
		SplashWindow.invokeMain("icarus.jar", "de.ims.icarus.Core", args); //$NON-NLS-1$ //$NON-NLS-2$
		
		SplashWindow.disposeSplash();
	}
}
