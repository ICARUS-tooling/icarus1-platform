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
