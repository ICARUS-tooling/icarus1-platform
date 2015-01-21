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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.model.standard.manifest;

import javax.swing.Icon;

import de.ims.icarus.model.api.manifest.ModifiableIdentity;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultModifiableIdentity implements ModifiableIdentity {

	private String id;
	private String name;
	private String description;
	private Icon icon;

	public DefaultModifiableIdentity() {
		// default constructor
	}

	public DefaultModifiableIdentity(String id, String name, String description, Icon icon) {
		setId(id);
		setName(name);
		setDescription(description);
		setIcon(icon);
	}

	public DefaultModifiableIdentity(String id, String description, Icon icon) {
		this(id, null, description, icon);
	}

	public DefaultModifiableIdentity(String id, String description) {
		this(id, null, description, null);
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return the icon
	 */
	@Override
	public Icon getIcon() {
		return icon;
	}

	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if(!CorpusUtils.isValidId(id))
			throw new IllegalArgumentException("Id format not supported: "+id); //$NON-NLS-1$

		this.id = id;
	}

	/**
	 * @param name the name to set
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param icon the icon to set
	 */
	@Override
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id==null ? 0 : id.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Identity) {
			Identity other = (Identity) obj;
			return id==null ? other.getId()==null :
				id.equals(other.getId());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ModifiableIdentity@"+(id==null ? "<unnamed>" : id); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
