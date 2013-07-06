/*
 * $Revision: 17 $
 * $Date: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/resources/DefaultResourceLoader.java $
 *
 * $LastChangedDate: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $ 
 * $LastChangedRevision: 17 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.resources;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import de.ims.icarus.util.Exceptions;


/**
 * A {@code ResourceLoader} that uses the {@code ClassLoader}
 * of a certain {@code Class} to load resources.
 * 
 * @author Markus Gärtner 
 * @version $Id: DefaultResourceLoader.java 17 2013-03-25 00:44:03Z mcgaerty $
 *
 */
public class DefaultResourceLoader implements ResourceLoader {
	
	private static Control control;
	
	protected ClassLoader classLoader;

	/**
	 * @param clazz
	 */
	public DefaultResourceLoader(ClassLoader classLoader) {
		setLoader(classLoader);
	}
	
	protected Control getControl() {
		synchronized (DefaultResourceLoader.class) {
			if(control==null) {
				control = new UTF8Control();
			}
		}
		return control;
	}
	
	public ClassLoader getLoader() {
		return classLoader;
	}
	
	public void setLoader(ClassLoader classLoader) {
		Exceptions.testNullArgument(classLoader, "classLoader"); //$NON-NLS-1$
		
		this.classLoader = classLoader;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DefaultResourceLoader) {
			return ((DefaultResourceLoader)obj).classLoader==classLoader;
		}
		return false;
	}

	/**
	 * @see de.ims.icarus.resources.ResourceLoader#loadResource(java.lang.String, java.util.Locale)
	 */
	@Override
	public ResourceBundle loadResource(String name, Locale locale) {
		return PropertyResourceBundle.getBundle(name, locale, getLoader(), getControl());
	}

}
