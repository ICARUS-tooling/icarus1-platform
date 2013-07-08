/*
 * $Revision: 39 $
 * $Date: 2013-05-17 15:19:31 +0200 (Fr, 17 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/launcher/de.ims.icarus/source/net/ikarus_systems/icarus/launcher/SplashWindow.java $
 *
 * $LastChangedDate: 2013-05-17 15:19:31 +0200 (Fr, 17 Mai 2013) $ 
 * $LastChangedRevision: 39 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.launcher;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


/**
 * 
 * @author Markus Gärtner
 * @version $Id: SplashWindow.java 39 2013-05-17 13:19:31Z mcgaerty $
 *
 */
public class SplashWindow {
	
	private static SplashDelegate splashDelegate;
	
	private static String[] delegateClasses = {
		"de.ims.icarus.launcher.NativeSplashDelegate", //$NON-NLS-1$
		"de.ims.icarus.launcher.AWTSplashDelegate", //$NON-NLS-1$
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
				// FIXME DEBUG
				//e.printStackTrace();
			}
		}
	}
	
	static void invokeMain(String jarName, String className, String[] args) {
		setText("Invoking ICARUS core");; //$NON-NLS-1$
		
		try {
			// If we can access class directly then don't bother
			// with the jar file
			Class<?> clazz = tryLoadClass(className);
			
			// Class not accessible directly, so we have to use
			// an additional class loader using the specified jar file
			if(clazz==null) {
				JarFile jarFile = new JarFile(jarName);
				
				File file = new File(jarName);
				File dir = file.getParentFile();
				
				int urlCount = 1;
				URL[] urls = new URL[10];
				urls[0] = file.toURI().toURL();
				
				try {
					Manifest manifest = jarFile.getManifest();
					if(manifest!=null) {
						Attributes attr = manifest.getMainAttributes();
						if(attr!=null && attr.containsKey(Attributes.Name.CLASS_PATH)) {
							String cp = attr.getValue(Attributes.Name.CLASS_PATH);
							if(cp!=null) {
								for(String path : cp.split(" ")) { //$NON-NLS-1$
									urls[urlCount++] = new File(dir, path).toURI().toURL();
								}
							}
						}
					}
				} finally {
					jarFile.close();
				}
				
				urls = Arrays.copyOf(urls, urlCount);
								
				@SuppressWarnings("resource")
				ClassLoader loader = new URLClassLoader(urls, SplashWindow.class.getClassLoader());
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
			// FIXME DEBUG
			//t.printStackTrace();
			
			// Load and show dialog instance
			Class.forName("de.ims.icarus.launcher.LauncherErrorDialog") //$NON-NLS-1$
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
				// FIXME DEBUG
				e.printStackTrace();
			}
		}
	}
	
	public static void setText(String text) {
		if(splashDelegate!=null) {
			try {
				splashDelegate.setText(text);
			} catch(Exception e) {
				// Ignore error
				// FIXME DEBUG
				e.printStackTrace();
			}
		}
	}
	
	public static void setMaxProgress(int maxValue) {
		if(splashDelegate!=null) {
			try {
				splashDelegate.setMaxProgress(maxValue);
			} catch(Exception e) {
				// Ignore error
				// FIXME DEBUG
				e.printStackTrace();
			}
		}
	}
	
	public static void setProgress(int value) {
		if(splashDelegate!=null) {
			try {
				splashDelegate.setProgress(value);
			} catch(Exception e) {
				// Ignore error
				// FIXME DEBUG
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
		 * Hide the underlying splash implementation and release
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
