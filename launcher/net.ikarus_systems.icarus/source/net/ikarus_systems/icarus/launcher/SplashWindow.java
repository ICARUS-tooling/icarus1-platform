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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SplashWindow {
	
	private static SplashDelegate splashDelegate;
	
	private static String[] delegateClasses = {
		"net.ikarus_systems.icarus.launcher.NativeSplashDelegate", //$NON-NLS-1$
		"net.ikarus_systems.icarus.launcher.AWTSplashDelegate", //$NON-NLS-1$
	};
	
	static void splash(URL imageURL) {
		for(String className : delegateClasses) {
			try {
				// Try to instantiate the delegate, ignore any exceptions
				splashDelegate = (SplashDelegate) Class.forName(className)
						.getConstructor(URL.class).newInstance(imageURL);
				
				// Use the first successfully created and visible delegate
				if(splashDelegate.isVisible()) {
					break;
				}
			} catch (Exception e) {
				// Ignore exception
				// DEBUG
				e.printStackTrace();
			}
		}
	}
	
	static void invokeMain(String jarName, String className, String[] args) {
		try {
			// If we can access class directly then don't bother
			// with the jar file
			Class<?> clazz = tryLoadClass(className);
			
			// Class not accessible directly, so we have to use
			// an additional class loader using the specified jar file
			if(clazz==null) {
				File jarFile = new File(jarName);
				URL jarURL = jarFile.toURI().toURL();
				
				@SuppressWarnings("resource")
				ClassLoader loader = new URLClassLoader(new URL[]{jarURL}, 
						SplashWindow.class.getClassLoader());
				clazz = loader.loadClass(className);
			}
			
			clazz.getMethod("main", new Class[] {String[].class}) //$NON-NLS-1$
				.invoke(null, new Object[] {args});
		} catch (Exception e) {
			Error err = new InternalError("Failed to invoke main method: "+className); //$NON-NLS-1$
			err.initCause(e);
			showError(err);
		}
	}
	
	private static Class<?> tryLoadClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	private static void showError(Throwable t) {
		try {
			// DEBUG
			t.printStackTrace();
			
			// Load and show dialog instance
			Class.forName("net.ikarus_systems.icarus.launcher.LauncherErrorDialog") //$NON-NLS-1$
				.getConstructor(Throwable.class).newInstance(t);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	static void disposeSplash() {
		if(splashDelegate!=null) {
			try {
				splashDelegate.dispose();
			} catch(Exception e) {
				// Ignore error
				// DEBUG
				e.printStackTrace();
			}
		}
	}
	
	public void setText(String text) {
		if(splashDelegate!=null) {
			try {
				splashDelegate.setText(text);
			} catch(Exception e) {
				// Ignore error
				// DEBUG
				e.printStackTrace();
			}
		}
	}
	
	public void setMaxProgress(int maxValue) {
		if(splashDelegate!=null) {
			try {
				splashDelegate.setMaxProgress(maxValue);
			} catch(Exception e) {
				// Ignore error
				// DEBUG
				e.printStackTrace();
			}
		}
	}
	
	public void setProgress(int value) {
		if(splashDelegate!=null) {
			try {
				splashDelegate.setProgress(value);
			} catch(Exception e) {
				// Ignore error
				// DEBUG
				e.printStackTrace();
			}
		}
	}
	
	public interface SplashDelegate {
		
		/**
		 * Checks whether the underlying splash object is visible
		 */
		boolean isVisible();
		
		/**
		 * Hide the unterlying splash implementation and release
		 * all associated resources.
		 */
		void dispose();
		
		/**
		 * Set the current progress value to the provided argument
		 * and refresh the progress bar.
		 * @throws IllegalArgumentException if {@code value}&lt;0
		 * or {@code value}&gt;{@code maxValue}
		 */
		void setProgress(int value);
		
		/**
		 * Change the maximum progress value to the given argument
		 * and refresh the progress bar.
		 * @throws IllegalArgumentException if {@code maxValue}&lt;
		 * {@code value}
		 */
		void setMaxProgress(int maxValue);
		
		/**
		 * Display the given {@code text} next to the progress bar.
		 */
		void setText(String text);
	}
}
