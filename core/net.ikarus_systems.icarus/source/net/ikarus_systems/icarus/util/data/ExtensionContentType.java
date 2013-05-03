/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

import java.util.Collections;
import java.util.Map;

import javax.swing.Icon;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.util.id.Identity;

import org.java.plugin.registry.Extension;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionContentType implements ContentType {
	
	private final Extension extension;
	
	private Identity identity;
	
	private Map<String, Object> properties;
	
	private final Class<?> contentClass;
	
	public ExtensionContentType(Extension extension) throws ClassNotFoundException {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		this.extension = extension;
		
		// Normally we would lazily load the content class the first
		// time it is requested. But since that would not be the right
		// time to throw a ClassNotFoundException we need to do it here.
		String contentClassName = extension.getParameter("contentClass").valueAsString(); //$NON-NLS-1$
		ClassLoader loader = PluginUtil.getClassLoader(extension);
		contentClass = loader.loadClass(contentClassName);
	}
	
	protected Identity getIdentity() {
		if(identity==null) {
			identity = PluginUtil.getIdentity(extension);
		}
		return identity;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getIdentity().getId();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return getIdentity().getName();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return getIdentity().getDescription();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return getIdentity().getIcon();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.ContentType#getContentClass()
	 */
	@Override
	public Class<?> getContentClass() {
		return contentClass;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.ContentType#getProperties()
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
	
}