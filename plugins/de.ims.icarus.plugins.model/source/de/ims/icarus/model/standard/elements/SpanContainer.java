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
package de.ims.icarus.model.standard.elements;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.ContainerType;
import de.ims.icarus.model.api.members.CorpusMember;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.MemberSet;
import de.ims.icarus.model.util.CorpusMemberUtils;
import de.ims.icarus.util.CorruptedStateException;

/**
 * Implements a continuous list of markables.
 * <p>
 * Does {@code not} support the following operation:
 * <ul>
 * <li>{@code #removeMarkable(int)} with an index other then {@code 0} or {@code #getMarkableCount()-1}</li>
 * <li>{@code #addMarkable(int, Item)} with an index other then {@code 0} or {@code #getMarkableCount()}</li>
 * <li>Any of the two {@code move} methods</li>
 * </ul>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SpanContainer extends AbstractContainer {
	private int startIndex = -1;
	private int endIndex = -1;

	public void appendItem(Item item) {
		// The item has already been added to its original host container.
		// Therefore we can safely call the indexOf() method
		int index = base().indexOfItem(item);

		if(index==-1)
			throw new CorruptedStateException("Item not yet added to base container: "+item); //$NON-NLS-1$

		if(startIndex==-1) {
			startIndex = endIndex = index;
		} else if(index==startIndex-1) {
			startIndex--;
		} else if(index==endIndex+1) {
			endIndex++;
		} else
			throw new IllegalArgumentException("Item not adjacent to current span: "+item); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.AbstractContainer#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();

		startIndex = endIndex = -1;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.AbstractContainer#revive()
	 */
	@Override
	public boolean revive() {
		return super.revive() && startIndex>=0 && endIndex>=startIndex;
	}

	/**
	 * @return the startIndex
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * @return the endIndex
	 */
	public int getEndIndex() {
		return endIndex;
	}

	/**
	 * @param startIndex the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
		checkIndex(startIndex);

		this.startIndex = startIndex;
	}

	/**
	 * @param endIndex the endIndex to set
	 */
	public void setEndIndex(int endIndex) {
		checkIndex(endIndex);

		this.endIndex = endIndex;
	}

	public void setSpan(int startIndex, int endIndex) {
		checkIndex(startIndex);
		checkIndex(endIndex);

		if(startIndex>endIndex)
			throw new IllegalArgumentException("Start index must not exceed endIndex"); //$NON-NLS-1$

		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	private void checkIndex(int index) {
		if(index<0)
			throw new IllegalArgumentException("Index must not be negative: "+index); //$NON-NLS-1$
		if(index>=base().getMarkableCount())
			throw new IllegalArgumentException("Index exceeds base container size: "+index); //$NON-NLS-1$
	}

	private Container base() {
		MemberSet<Container> base = getBaseContainers();
		switch (base.size()) {
		case 0:
			throw new CorruptedStateException("Span must have exactly one base container"); //$NON-NLS-1$
		case 1: return base.elementAt(1);

		default:
			throw new CorruptedStateException("Span cannot have more than 1 base layer"); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.SPAN;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		if(startIndex==-1 || endIndex==-1) {
			return 0;
		}

		return endIndex-startIndex+1;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getItemAt(int)
	 */
	@Override
	public Item getItemAt(int index) {
		return base().getItemAt(startIndex+index);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#addItem(int, de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public void addItem(int index, Item item) {
		int size = getMarkableCount();
		if(index!=0 && index!=size)
			throw new IllegalArgumentException(CorpusMemberUtils.outOfBoundsMessage(
					"Invalid index for add", index, 0, size)); //$NON-NLS-1$

		boolean append = index==size;

		execute(new ElementChange(append, true, item));
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeItem(int)
	 */
	@Override
	public Item removeItem(int index) {
		int size = getMarkableCount();

		if(size==0)
			throw new IllegalStateException("Cannot remove from empty span"); //$NON-NLS-1$
		if(index!=0 && index!=size-1)
			throw new IllegalArgumentException(CorpusMemberUtils.outOfBoundsMessage(
					"Invalid index for removal", index, 0, size-1)); //$NON-NLS-1$

		boolean append = index==size-1;

		Item item = getItemAt(index);

		execute(new ElementChange(append, false, null));

		return item;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		throw new UnsupportedOperationException("Move not supported"); //$NON-NLS-1$
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Item> iterator() {
		return new SpanItr();
	}

	private class ElementChange implements AtomicChange {

		private final Item item;
		private final boolean append;
		private final boolean add;
		private int expectedSize;

		public ElementChange(boolean append, boolean add, Item item) {
			this.append = append;
			this.item = item;
			this.add = add;
			expectedSize = base().getMarkableCount();
		}


		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			Container base = base();

			if(base.getMarkableCount()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Element change failed", expectedSize, base.getMarkableCount())); //$NON-NLS-1$

			if(add) {
				if(append) {
					Item item = base.getItemAt(endIndex+1);
					if(this.item!=item)
						throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
								"Add failed (tail)", this.item, item)); //$NON-NLS-1$

					endIndex++;
				} else {
					Item item = base.getItemAt(startIndex-1);
					if(this.item!=item)
						throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
								"Add failed (head)", this.item, item)); //$NON-NLS-1$

					startIndex--;
				}
			} else {
				Item item = null;
				if(append) {
					item = base.getItemAt(endIndex);
					if(this.item!=item)
						throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
								"Remove failed (tail)", this.item, item)); //$NON-NLS-1$

					endIndex--;
				} else {
					item = base.getItemAt(startIndex);
					if(this.item!=item)
						throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
								"Remove failed (head)", this.item, item)); //$NON-NLS-1$

					startIndex++;
				}
			}
		}


		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return SpanContainer.this;
		}

	}

	private class ClearChange implements AtomicChange {

		private final int expectedStartIndex;
		private final int expectedEndIndex;
		private final Item first;
		private final Item last;

		private int expectedSize;

		private boolean clear = true;

		public ClearChange() {
			this.expectedStartIndex = startIndex;
			this.expectedEndIndex = endIndex;

			Container base = base();

			first = base.getItemAt(expectedStartIndex);
			last = base.getItemAt(expectedEndIndex);

			expectedSize = base.getMarkableCount();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			Container base = base();

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
				Item item = base.getItemAt(expectedStartIndex);
				if(item!=first)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Clear failed (head mismatch)", first, item)); //$NON-NLS-1$
				item = base.getItemAt(expectedEndIndex);
				if(item!=last)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Clear failed (tail mismatch)", last, item)); //$NON-NLS-1$

				startIndex = -1;
				endIndex = -1;
			} else {
				if(startIndex!=-1)
					throw new CorruptedStateException(CorpusMemberUtils.illegalOffsetMessage(
							"Fill failed (start-index)", startIndex)); //$NON-NLS-1$
				if(endIndex!=-1)
					throw new CorruptedStateException(CorpusMemberUtils.illegalOffsetMessage(
							"Fill failed (end-index)", endIndex)); //$NON-NLS-1$
				Item item = base.getItemAt(expectedStartIndex);
				if(item!=first)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Fill failed (head mismatch)", first, item)); //$NON-NLS-1$
				item = base.getItemAt(expectedEndIndex);
				if(item!=last)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Fill failed (tail mismatch)", last, item)); //$NON-NLS-1$

				startIndex = expectedStartIndex;
				endIndex = expectedEndIndex;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return SpanContainer.this;
		}

	}

	private class SpanItr implements Iterator<Item> {
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
		public Item next() {
            checkForComodification();
            if (cursor >= getMarkableCount())
                throw new NoSuchElementException();
            int i = startIndex+cursor;
            if (i >= base().getMarkableCount())
                throw new ConcurrentModificationException();
            cursor++;
            return base().getItemAt(i);
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
