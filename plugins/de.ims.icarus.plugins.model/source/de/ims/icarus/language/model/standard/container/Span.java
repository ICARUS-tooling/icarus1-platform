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

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.ContainerType;
import de.ims.icarus.language.model.CorpusMember;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.standard.CorpusMemberUtils;
import de.ims.icarus.language.model.standard.builder.AbstractContainerBuilder;
import de.ims.icarus.util.CorruptedStateException;

/**
 * Implements a continuous list of markables.
 * <p>
 * Does {@code not} support the following operation:
 * <ul>
 * <li>{@code #removeMarkable(int)} with an index other then {@code 0} or {@code #getMarkableCount()-1}</li>
 * <li>{@code #addMarkable(int, Markable)} with an index other then {@code 0} or {@code #getMarkableCount()}</li>
 * <li>Any of the two {@code move} methods</li>
 * </ul>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Span extends AbstractNestedContainer {

	private final Container base;
	private int startIndex = -1;
	private int endIndex = -1;

	/**
	 * @param parent
	 */
	public Span(long id, Container parent, Container base, int startIndex, int endIndex) {
		super(id, parent);

		if (base == null)
			throw new NullPointerException("Invalid base"); //$NON-NLS-1$
		if(/*startIndex<0 || endIndex<0 || */startIndex>endIndex)
			throw new IndexOutOfBoundsException("Invalid bounds: start="+startIndex+", end="+endIndex); //$NON-NLS-1$ //$NON-NLS-2$

		this.base = base;

		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	protected void addMarkable0(Markable markable) {
		// The markable has already been added to its original host container.
		// Therefore we can safely call the indexOf() method
		int index = base.indexOfMarkable(markable);

		if(index==-1)
			throw new CorruptedStateException("Markable not yet added to base container: "+markable); //$NON-NLS-1$

		if(startIndex==-1) {
			startIndex = endIndex = index;
		} else if(index==startIndex-1) {
			startIndex--;
		} else if(index==endIndex+1) {
			endIndex++;
		} else
			throw new IllegalArgumentException("Markable not adjacent to current span: "+markable); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.SPAN;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.container.AbstractContainer#getBaseContainer()
	 */
	@Override
	public Container getBaseContainer() {
		return base;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		if(startIndex==-1 || endIndex==-1) {
			return 0;
		}

		return endIndex-startIndex+1;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		return base.getMarkableAt(startIndex+index);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.language.model.Container#addMarkable(int, de.ims.icarus.language.model.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		int size = getMarkableCount();
		if(index!=0 && index!=size)
			throw new IllegalArgumentException(CorpusMemberUtils.outOfBoundsMessage(
					"Invalid index for add", index, 0, size)); //$NON-NLS-1$

		boolean append = index==size;

		execute(new ElementChange(append, markable.getId(), markable));
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		int size = getMarkableCount();

		if(size==0)
			throw new IllegalStateException("Cannot remove from empty span"); //$NON-NLS-1$
		if(index!=0 && index!=size-1)
			throw new IllegalArgumentException(CorpusMemberUtils.outOfBoundsMessage(
					"Invalid index for removal", index, 0, size-1)); //$NON-NLS-1$

		boolean append = index==size-1;

		Markable markable = getMarkableAt(index);

		execute(new ElementChange(append, markable.getId(), null));

		return markable;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#moveMarkable(int, int)
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
		return new SpanItr();
	}

	private class ElementChange implements AtomicChange {

		private Markable markable;
		private final boolean append;
		private final long id;
		private int expectedSize;

		public ElementChange(boolean append, long id, Markable markable) {
			this.append = append;
			this.markable = markable;
			this.id = id;
			expectedSize = base.getMarkableCount();
		}


		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(base.getMarkableCount()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Element change failed", expectedSize, base.getMarkableCount())); //$NON-NLS-1$

			if(markable==null) {
				Markable markable = null;
				if(append) {
					markable = base.getMarkableAt(endIndex-1);
					if(markable.getId()!=id)
						throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
								"Remove failed (tail)", id, markable.getId())); //$NON-NLS-1$

					endIndex--;
				} else {
					markable = base.getMarkableAt(startIndex+1);
					if(markable.getId()!=id)
						throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
								"Remove failed (head)", id, markable.getId())); //$NON-NLS-1$

					startIndex++;
				}
				this.markable = markable;
			} else {
				if(append) {
					Markable markable = base.getMarkableAt(endIndex+1);
					if(markable.getId()!=id)
						throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
								"Add failed (tail)", id, markable.getId())); //$NON-NLS-1$

					endIndex++;
				} else {
					Markable markable = base.getMarkableAt(startIndex-1);
					if(markable.getId()!=id)
						throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
								"Add failed (head)", id, markable.getId())); //$NON-NLS-1$

					startIndex--;
				}
				markable = null;
			}
		}


		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return Span.this;
		}

	}

	private class ClearChange implements AtomicChange {

		private final int expectedStartIndex;
		private final int expectedEndIndex;
		private final long startId;
		private final long endId;

		private int expectedSize;

		private boolean clear = true;

		public ClearChange() {
			this.expectedStartIndex = startIndex;
			this.expectedEndIndex = endIndex;

			startId = base.getMarkableAt(startIndex).getId();
			endId = base.getMarkableAt(endIndex).getId();

			expectedSize = base.getMarkableCount();
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(base.getMarkableCount()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear change failed", expectedSize, base.getMarkableCount())); //$NON-NLS-1$

			if(clear) {
				if(startIndex!=expectedStartIndex)
					throw new CorruptedStateException(CorpusMemberUtils.offsetMismatchMessage(
							"Clear failed (start-index)", expectedStartIndex, startIndex)); //$NON-NLS-1$
				if(endIndex!=expectedEndIndex)
					throw new CorruptedStateException(CorpusMemberUtils.offsetMismatchMessage(
							"Clear failed (end-index)", expectedEndIndex, endIndex)); //$NON-NLS-1$
				Markable markable = base.getMarkableAt(expectedStartIndex);
				if(markable.getId()!=startId)
					throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
							"Clear failed (head mismatch)", startId, markable.getId())); //$NON-NLS-1$
				markable = base.getMarkableAt(expectedEndIndex);
				if(markable.getId()!=endId)
					throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
							"Clear failed (tail mismatch)", endId, markable.getId())); //$NON-NLS-1$

				startIndex = -1;
				endIndex = -1;
			} else {
				if(startIndex!=-1)
					throw new CorruptedStateException(CorpusMemberUtils.illegalOffsetMessage(
							"Fill failed (start-index)", startIndex)); //$NON-NLS-1$
				if(endIndex!=-1)
					throw new CorruptedStateException(CorpusMemberUtils.illegalOffsetMessage(
							"Fill failed (end-index)", endIndex)); //$NON-NLS-1$
				Markable markable = base.getMarkableAt(expectedStartIndex);
				if(markable.getId()!=startId)
					throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
							"Fill failed (head mismatch)", startId, markable.getId())); //$NON-NLS-1$
				markable = base.getMarkableAt(expectedEndIndex);
				if(markable.getId()!=endId)
					throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
							"Fill failed (tail mismatch)", endId, markable.getId())); //$NON-NLS-1$

				startIndex = expectedStartIndex;
				endIndex = expectedEndIndex;
			}
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return Span.this;
		}

	}

	private class SpanItr implements Iterator<Markable> {
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
            if (cursor >= getMarkableCount())
                throw new NoSuchElementException();
            int i = startIndex+cursor;
            if (i >= base.getMarkableCount())
                throw new ConcurrentModificationException();
            cursor++;
            return base.getMarkableAt(i);
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

	public static class SpanContainerBuilder extends AbstractContainerBuilder<Span> {

		/**
		 * @see de.ims.icarus.language.model.standard.builder.ContainerBuilder#addMarkable(de.ims.icarus.language.model.Markable)
		 */
		@Override
		public void addMarkable(Markable markable) {
			container.addMarkable0(markable);
		}

		/**
		 * @see de.ims.icarus.language.model.standard.builder.AbstractContainerBuilder#createContainer()
		 */
		@Override
		protected Span createContainer() {
			return new Span(newId(), parent, base, 0, 0);
		}

	}
}
