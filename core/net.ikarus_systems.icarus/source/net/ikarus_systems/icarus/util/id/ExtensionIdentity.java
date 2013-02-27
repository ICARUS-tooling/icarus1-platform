/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.id;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.resources.DefaultResourceLoader;
import net.ikarus_systems.icarus.resources.ManagedResource;
import net.ikarus_systems.icarus.resources.ResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.util.Exceptions;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class ExtensionIdentity implements Identity {
	
	protected final Extension extension;
	protected ManagedResource resources;
	protected Icon icon = null;

	/**
	 * 
	 */
	public ExtensionIdentity(Extension extension) {
		Exceptions.testNullArgument(extension, "extension"); //$NON-NLS-1$
		
		this.extension = extension;
		
		Extension.Parameter param = extension.getParameter("resources"); //$NON-NLS-1$
		if(param!=null) {
			ClassLoader classLoader = PluginUtil.getPluginManager().getPluginClassLoader(
					extension.getDeclaringPluginDescriptor());
			String basename = param.valueAsString();
			ResourceLoader loader = new DefaultResourceLoader(classLoader);
			resources = ResourceManager.getInstance().addManagedResource(basename, loader);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		Extension.Parameter param = extension.getParameter("id"); //$NON-NLS-1$
		
		return param==null ? extension.getId() : param.valueAsString();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		Extension.Parameter param = extension.getParameter("name"); //$NON-NLS-1$
		
		if(param!=null && resources!=null) {
			return resources.getResource(param.valueAsString());
		}
		
		return getId();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		Extension.Parameter param = extension.getParameter("description"); //$NON-NLS-1$
		
		if(param!=null && resources!=null) {
			return resources.getResource(param.valueAsString());
		}
		
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		Extension.Parameter param = extension.getParameter("icon"); //$NON-NLS-1$
		if(icon==null && param!=null) {
			ClassLoader loader = PluginUtil.getPluginManager().getPluginClassLoader(extension.getDeclaringPluginDescriptor());
			URL iconLocation = loader.getResource(param.valueAsString());
			
			if(iconLocation!=null) {
				icon = new ImageIcon(iconLocation);
			}
		}
		
		return icon;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return extension;
	}
}
