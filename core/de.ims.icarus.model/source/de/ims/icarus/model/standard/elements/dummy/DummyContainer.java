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
package de.ims.icarus.model.standard.elements.dummy;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.ContainerType;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.MemberSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyContainer extends DummyMarkable implements Container {

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Item> iterator() {
		List<Item> list = Collections.emptyList();
		return list.iterator();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getBaseContainers()
	 */
	@Override
	public MemberSet<Container> getBaseContainers() {
		return Container.EMPTY_BASE_SET;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getItemAt(int)
	 */
	@Override
	public Item getItemAt(int index) {
		throw new IndexOutOfBoundsException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#indexOfItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public int indexOfItem(Item item) {
		return -1;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#containsItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public boolean containsItem(Item item) {
		return false;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#addItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public void addItem(Item item) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#addItem(int, de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public void addItem(int index, Item item) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeItem(int)
	 */
	@Override
	public Item removeItem(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public Item removeItem(Item item) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#moveItem(de.ims.icarus.model.api.members.Item, int)
	 */
	@Override
	public void moveItem(Item item, int index) {
		throw new UnsupportedOperationException();
	}

}
