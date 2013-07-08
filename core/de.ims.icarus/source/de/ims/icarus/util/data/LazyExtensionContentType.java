/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.data;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Icon;


import org.java.plugin.PluginLifecycleException;
import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.id.Identity;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LazyExtensionContentType implements ContentType {
	
	private final Extension extension;
	
	private Identity identity;
	
	private Map<String, Object> properties;
	
	private final String filterClass;
	private Filter filter;
	
	private Class<?> contentClass;
	
	public LazyExtensionContentType(Extension extension) {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		this.extension = extension;
		
		Extension.Parameter filterParam = null;
		try {
			filterParam = extension.getParameter("filter"); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			// ignore
		}
		
		filterClass = filterParam==null ? null : filterParam.valueAsString();
	}
	
	protected Identity getIdentity() {
		if(identity==null) {
			identity = PluginUtil.getIdentity(extension);
		}
		return identity;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return ContentTypeRegistry.getContentTypeId(extension);
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return getIdentity().getName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return getIdentity().getDescription();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return getIdentity().getIcon();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	/**
	 * @see de.ims.icarus.util.data.ContentType#getContentClass()
	 */
	@Override
	public Class<?> getContentClass() {
		if(contentClass==null) {

			try {
				PluginUtil.activatePlugin(extension);
			} catch (PluginLifecycleException ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to activate plugin: "+extension.getUniqueId(), ex); //$NON-NLS-1$
				
				throw new IllegalStateException(ex);
			}
			String contentClassName = extension.getParameter("contentClass").valueAsString(); //$NON-NLS-1$
			ClassLoader loader = PluginUtil.getClassLoader(extension);
			try {
				contentClass = loader.loadClass(contentClassName);
			} catch (ClassNotFoundException ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to load content class: "+extension.getUniqueId(), ex); //$NON-NLS-1$
				
				throw new IllegalStateException(ex);
			}
		}
		return contentClass;
	}

	/**
	 * @see de.ims.icarus.util.data.ContentType#getProperties()
	 */
	@Override
	public Map<String, Object> getProperties() {
		if(properties==null) {
			properties = PluginUtil.getProperties(extension);
			if(properties==null) {
				properties = Collections.emptyMap();
			}
		}
		
		return properties;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ContentType) {
			return ((ContentType)obj).getId().equals(getId());
		}
		return false;
	}

	@Override
	public String toString() {
		return getId();
	}
	
	protected Filter getFilter() {
		if(filterClass==null) {
			return null;
		}
		if(filter==null) {
			try {
				PluginUtil.activatePlugin(extension);
				ClassLoader loader = PluginUtil.getClassLoader(extension);
				Class<?> clazz = loader.loadClass(filterClass);
				filter = (Filter) clazz.newInstance();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to instantiate filter for content type '"+getId()+"': "+filterClass, e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return filter;
	}

	/**
	 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object obj) {
		Filter filter = getFilter();
		if(filter!=null) {
			return filter.accepts(obj);
		}
		
		if(ContentTypeRegistry.isStrictType(this)) {
			return getContentClass().equals(obj);
		} else {
			return getContentClass().isAssignableFrom((Class<?>) obj);
		}
	}

	/**
	 * @see de.ims.icarus.util.data.ContentType#getContentClassName()
	 */
	@Override
	public String getContentClassName() {
		return extension.getParameter("contentClass").valueAsString(); //$NON-NLS-1$
	}
	
}