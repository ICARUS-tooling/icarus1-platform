/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
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
 * @version $Id$
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
