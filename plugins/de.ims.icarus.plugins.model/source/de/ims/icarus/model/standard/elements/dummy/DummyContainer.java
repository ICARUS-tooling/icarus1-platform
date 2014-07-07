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

import de.ims.icarus.model.api.Container;
import de.ims.icarus.model.api.ContainerType;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.MemberSet;
import de.ims.icarus.model.api.manifest.ContainerManifest;

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
	public Iterator<Markable> iterator() {
		List<Markable> list = Collections.emptyList();
		return list.iterator();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getBaseContainers()
	 */
	@Override
	public MemberSet<Container> getBaseContainers() {
		return Container.EMPTY_BASE_SET;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		throw new IndexOutOfBoundsException();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#indexOfMarkable(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		return -1;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#containsMarkable(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		return false;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#addMarkable(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public void addMarkable(Markable markable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#addMarkable(int, de.ims.icarus.model.api.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeMarkable(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public Markable removeMarkable(Markable markable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#moveMarkable(de.ims.icarus.model.api.Markable, int)
	 */
	@Override
	public void moveMarkable(Markable markable, int index) {
		throw new UnsupportedOperationException();
	}

}
