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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StaticIdentity implements Identity {
	
	protected final String id;
	protected final Object owner;
	protected String name;
	protected String description;
	protected URL iconLocation;
	protected Icon icon;
	
	/**
	 * 
	 */
	public StaticIdentity(String id, Object owner) {
		if(id==null)
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		if(owner==null)
			throw new IllegalArgumentException("Invalid owner"); //$NON-NLS-1$

		this.id = id;
		this.owner = owner;
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
		return name;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
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
		
		return icon;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return owner;
	}

	/**
	 * @return the iconLocation
	 */
	public URL getIconLocation() {
		return iconLocation;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param iconLocation the iconLocation to set
	 */
	public void setIconLocation(URL iconLocation) {
		this.iconLocation = iconLocation;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

}
