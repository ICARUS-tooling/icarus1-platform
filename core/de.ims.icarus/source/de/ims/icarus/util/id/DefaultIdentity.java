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

import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultIdentity implements Identity {

	protected final ResourceDomain resourceDomain;

	protected final Identity base;

	protected final String id;
	protected final Object owner;
	protected String nameKey;
	protected String descriptionKey;
	protected URL iconLocation;
	protected Icon icon;

	protected boolean locked = false;

	public DefaultIdentity(String id, Object owner) {
		this(null, id, owner, null);
	}

	public DefaultIdentity(Identity base, Object owner) {
		this(base, null, owner, null);
	}

	public DefaultIdentity(String id, Object owner, ResourceDomain domain) {
		this(null, id, owner, domain);
	}

	public DefaultIdentity(Identity base, Object owner, ResourceDomain domain) {
		this(base, null, owner, domain);
	}

	protected DefaultIdentity(Identity base, String id, Object owner, ResourceDomain domain) {
		if((base==null || base.getId()==null) && id==null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if(owner==null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		if(domain==null) {
			domain = ResourceManager.getInstance().getGlobalDomain();
		}

		if(id==null) {
			id = base.getId();
		}

		this.base = base;
		this.id = id;
		this.owner = owner;
		this.resourceDomain = domain;
	}


	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return id;
	}


	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		String name = null;

		if(nameKey!=null) {
			name = resourceDomain.get(nameKey);
		}

		if(name==null && base!=null) {
			name = base.getName();
		}

		if(name==null) {
			name = getId();
		}

		return name;
	}


	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		String description = null;

		if(descriptionKey!=null) {
			description = resourceDomain.get(descriptionKey);
		}

		if(description==null && base!=null) {
			description = base.getDescription();
		}

		return description;
	}


	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		Icon icon = this.icon;

		if(icon==null && iconLocation!=null) {
			icon = new ImageIcon(iconLocation);
		}

		if(icon==null && base!=null) {
			icon = base.getIcon();
		}

		return icon;
	}


	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return owner;
	}

	public boolean isLocked() {
		return locked;
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

	public void lock() {
		locked = true;
	}

	/**
	 * @return the nameKey
	 */
	public String getNameKey() {
		return nameKey;
	}


	/**
	 * @param nameKey the nameKey to set
	 */
	public void setNameKey(String nameKey) {
		if(locked)
			throw new IllegalArgumentException("Name key already set"); //$NON-NLS-1$

		this.nameKey = nameKey;
	}


	/**
	 * @return the descriptionKey
	 */
	public String getDescriptionKey() {
		return descriptionKey;
	}


	/**
	 * @param descriptionKey the descriptionKey to set
	 */
	public void setDescriptionKey(String descriptionKey) {
		if(locked)
			throw new IllegalArgumentException("Description key already set"); //$NON-NLS-1$

		this.descriptionKey = descriptionKey;
	}


	/**
	 * @return the iconLocation
	 */
	public URL getIconLocation() {
		return iconLocation;
	}


	/**
	 * @param iconLocation the iconLocation to set
	 */
	public void setIconLocation(URL iconLocation) {
		if(locked)
			throw new IllegalArgumentException("Icon location key already set"); //$NON-NLS-1$

		this.iconLocation = iconLocation;
		icon = null;
	}


	/**
	 * @return the resourceDomain
	 */
	public ResourceDomain getResourceDomain() {
		return resourceDomain;
	}


	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Icon icon) {
		if(locked)
			throw new IllegalArgumentException("Icon already set"); //$NON-NLS-1$

		this.icon = icon;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
