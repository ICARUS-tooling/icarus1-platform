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
package de.ims.icarus.language.model.standard;

import javax.swing.Icon;

import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProxyIdentity implements Identity {

	private Identity identity;

	/**
	 * @return the identity
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @param identity the identity to set
	 */
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	/**
	 * @return
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return identity.getId();
	}

	/**
	 * @return
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return identity.getName();
	}

	/**
	 * @return
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return identity.getDescription();
	}

	/**
	 * @return
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return identity.getIcon();
	}

	/**
	 * @return
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return identity.getOwner();
	};
}