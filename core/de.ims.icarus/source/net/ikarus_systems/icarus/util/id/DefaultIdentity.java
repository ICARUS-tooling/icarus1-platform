/*
 * $Revision: 17 $
 * $Date: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/util/id/DefaultIdentity.java $
 *
 * $LastChangedDate: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $ 
 * $LastChangedRevision: 17 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.util.id;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;

/**
 * @author Markus Gärtner 
 * @version $Id: DefaultIdentity.java 17 2013-03-25 00:44:03Z mcgaerty $
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
		if(base==null && id==null)
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		if(owner==null)
			throw new IllegalArgumentException("Invalid owner"); //$NON-NLS-1$
		
		if(domain==null) {
			domain = ResourceManager.getInstance().getGlobalDomain();
		}
		
		this.base = base;
		this.id = id;
		this.owner = owner;
		this.resourceDomain = domain;
	}


	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return id;
	}


	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
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
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
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
	 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
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
	 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
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
}
