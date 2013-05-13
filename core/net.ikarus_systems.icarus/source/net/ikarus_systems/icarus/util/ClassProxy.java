/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.java.plugin.registry.Extension;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class ClassProxy {
	
	private final String className;
	
	private final ClassLoader classLoader;
	
	private Map<String, Object> properties;

	public ClassProxy(String className, ClassLoader classLoader) {
		Exceptions.testNullArgument(className, "className"); //$NON-NLS-1$
		Exceptions.testNullArgument(classLoader, "classLoader"); //$NON-NLS-1$
		
		this.className = className;
		this.classLoader = classLoader;
	}
	
	public ClassProxy(Extension extension) {
		Exceptions.testNullArgument(extension, "extension"); //$NON-NLS-1$
		
		className = extension.getParameter("class").valueAsString(); //$NON-NLS-1$
		classLoader = PluginUtil.getClassLoader(extension);
	}

	public Object loadObject() {
		try {
			Class<?> clazz = classLoader.loadClass(className);
			
			return clazz.newInstance();
		} catch (ClassNotFoundException e) {
			LoggerFactory.log(this, Level.SEVERE, "ClassProxy: Could not find class: "+className, e); //$NON-NLS-1$
		} catch (InstantiationException e) {
			LoggerFactory.log(this, Level.SEVERE, "ClassProxy: Unable to instantiate class: "+className, e); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			LoggerFactory.log(this, Level.SEVERE, "ClassProxy: Unable to access default constructor: "+className, e); //$NON-NLS-1$
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return "Proxy: "+className; //$NON-NLS-1$
	}
	
	/**
	 * Two {@code ClassProxy} instances are considered equal if
	 * they both refer to the same {@code Class} as by their 
	 * {@code className} field and both use the same {@code ClassLoader}
	 * to load the final object.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ClassProxy) {
			ClassProxy other = (ClassProxy) obj;
			return className.equals(other.className)
					&& classLoader.equals(other.classLoader);
		}
		return false;
	}
	
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}
	
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new HashMap<>();
		}
			
		properties.put(key, value);
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
}
