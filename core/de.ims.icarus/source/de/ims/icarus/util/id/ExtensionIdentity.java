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
		
		if(param!=null) {
			String name = param.valueAsString();
			return resources==null ? name : resources.getResource(name);
		}
		
		return getId();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		Extension.Parameter param = extension.getParameter("description"); //$NON-NLS-1$
		
		if(param!=null) {
			String desc = param.valueAsString();
			return resources==null ? desc : resources.getResource(desc);
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
