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
package de.ims.icarus.model.api.edit;

import javax.swing.Icon;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum EditOperation implements Identity {

	/**
	 * Describes the adding of a new element either
	 * at the end of a collection or as its first
	 * element. This is essentially the operation of
	 * appending an element to either the head or tail
	 * of a list.
	 */
	ADD("add"), //$NON-NLS-1$

	/**
	 * Adding an element is supported at every random
	 * position within the collection.
	 */
	ADD_RANDOM("addRandom"), //$NON-NLS-1$

	/**
	 * Removal of an element is only possible on one of
	 * the two ends of a the list.
	 */
	REMOVE("remove"), //$NON-NLS-1$

	/**
	 * Any element in the list can be removed at any time
	 */
	REMOVE_RANDOM("removeRandom"), //$NON-NLS-1$

	/**
	 * All elements can be removed with one atomic operation
	 */
	CLEAR("clear"), //$NON-NLS-1$

	/**
	 * An element can be moved within the collection between
	 * random positions.
	 */
	MOVE("move"), //$NON-NLS-1$

	/**
	 * A special kind of operation only affecting edges.
	 * Allows to change the source or target terminal of an edge.
	 */
	LINK("link"); //$NON-NLS-1$

	private final String key;

	private EditOperation(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$
		this.key = key;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return name();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.model.editOperations."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.model.editOperations."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}
}
