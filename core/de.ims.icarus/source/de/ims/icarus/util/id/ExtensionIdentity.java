/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/id/ExtensionIdentity.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.util.id;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;


import org.java.plugin.registry.Extension;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.DefaultResourceLoader;
import de.ims.icarus.resources.ManagedResource;
import de.ims.icarus.resources.ResourceLoader;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.Exceptions;

/**
 * @author Markus Gärtner 
 * @version $Id: ExtensionIdentity.java 23 2013-04-17 12:39:04Z mcgaerty $
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
			ClassLoader classLoader = PluginUtil.getClassLoader(extension);
			String basename = param.valueAsString();
			ResourceLoader loader = new DefaultResourceLoader(classLoader);
			resources = ResourceManager.getInstance().addManagedResource(basename, loader);
		}
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		Extension.Parameter param = extension.getParameter("id"); //$NON-NLS-1$
		
		return param==null ? extension.getId() : param.valueAsString();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
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
	 * @see de.ims.icarus.util.id.Identity#getDescription()
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
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		Extension.Parameter param = extension.getParameter("icon"); //$NON-NLS-1$
		if(icon==null && param!=null) {
			ClassLoader loader = PluginUtil.getClassLoader(extension);
			URL iconLocation = loader.getResource(param.valueAsString());
			
			if(iconLocation!=null) {
				icon = new ImageIcon(iconLocation);
			}
		}
		
		return icon;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return extension;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Identity) {
			Identity other = (Identity)obj;
			return getId().equals(other.getId());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getId();
	}
}
