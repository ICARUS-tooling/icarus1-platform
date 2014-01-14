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
package de.ims.icarus.language.model.standard.structure;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.ContainerType;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.Structure;
import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.standard.container.AbstractContainer;

/**
 * Implements a bridge between containers and structures that allows edges
 * of a structure to be accessed as members of a container. This is necessary
 * for example when a structure builds its edges atop of another structure's
 * edge collection.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EdgeProxyContainer extends AbstractContainer {

	private final Structure structure;

	public EdgeProxyContainer(long id, Structure structure) {
		super(id);

		if (structure == null)
			throw new NullPointerException("Invalid structure"); //$NON-NLS-1$

		this.structure = structure;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return structure.getManifest();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		return structure.getEdgeCount();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		return structure.getEdgeAt(index);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#addMarkable(int, de.ims.icarus.language.model.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		return structure.getContainer();
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return structure.getLayer();
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return structure.getCorpus();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		return new EdgeItr();
	}

	private class EdgeItr implements Iterator<Markable> {
		private final int expectedSize = getMarkableCount();

		private int cursor;

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return cursor<getMarkableCount();
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Markable next() {
            checkForComodification();
            int i = cursor;
            if (i >= getMarkableCount())
                throw new NoSuchElementException();
            cursor++;
            return structure.getEdgeAt(i);
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported"); //$NON-NLS-1$
		}

        final void checkForComodification() {
            if (getMarkableCount()!=expectedSize)
                throw new ConcurrentModificationException();
        }
	}
}