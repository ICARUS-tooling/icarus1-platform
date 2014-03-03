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
package de.ims.icarus.language.model.standard.container;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.ContainerType;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SingletonContainer extends AbstractNestedContainer {

	private Markable singleton;

	/**
	 * @param parent
	 */
	public SingletonContainer(long id, Container parent) {
		super(id, parent);
	}

	public SingletonContainer(long id, Container parent, Markable singleton) {
		this(id, parent);

		if (singleton == null)
			throw new NullPointerException("Invalid singleton"); //$NON-NLS-1$

		this.singleton = singleton;
	}

	protected void set(Markable singleton) {
		this.singleton = singleton;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.SINGLETON;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		return singleton==null ? 0 : 1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		if(singleton!=null && index==0) {
			return singleton;
		} else
			throw new IllegalArgumentException("No element for index: "+index); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.container.AbstractContainer#getBeginOffset()
	 */
	@Override
	public int getBeginOffset() {
		return singleton==null ? -1 : singleton.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.container.AbstractContainer#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return singleton==null ? -1 : singleton.getEndOffset();
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.container.AbstractContainer#containsMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		return singleton==null ? false : singleton.equals(markable);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.container.AbstractContainer#indexOfMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		return singleton!=null && singleton.equals(markable) ? 0 : -1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		execute(new ElementChange());
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#addMarkable(int, de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$
		if(index!=0)
			throw new IndexOutOfBoundsException();

		execute(new ElementChange(markable));
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		if(index!=0 || getMarkableCount()==0)
			throw new IndexOutOfBoundsException();

		Markable markable = singleton;

		execute(new ElementChange());

		return markable;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		throw new UnsupportedOperationException("Move not supported"); //$NON-NLS-1$
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		return new SingletonItr();
	}

	private class ElementChange implements AtomicChange {
		private Markable markable;

		public ElementChange() {
			this(null);
		}

		public ElementChange(Markable markable) {
			this.markable = markable;
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(markable==null) {
				if(singleton==null)
					throw new CorruptedStateException(
							"Remove failed: singleton is null"); //$NON-NLS-1$

				markable = singleton;
				singleton = null;
			} else {
				if(singleton==null)
					throw new CorruptedStateException(
							"Add failed: singleton is already set"); //$NON-NLS-1$

				singleton = markable;
				markable = null;
			}
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return SingletonContainer.this;
		}
	}

	private class SingletonItr implements Iterator<Markable> {

		private boolean hasReturned = false;
		private final Markable item = singleton;

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return !hasReturned;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Markable next() {
			if(hasReturned)
				throw new NoSuchElementException();
			if(item!=singleton)
				throw new ConcurrentModificationException();

			hasReturned = true;

			return item;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Removing via iterator not supported"); //$NON-NLS-1$
		}

	}
}
