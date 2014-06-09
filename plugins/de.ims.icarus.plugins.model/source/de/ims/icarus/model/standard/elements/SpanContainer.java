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

 * $Revision: 244 $
 * $Date: 2014-04-10 14:09:12 +0200 (Do, 10 Apr 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/source/de/ims/icarus/language/model/standard/container/SpanContainer.java $
 *
 * $LastChangedDate: 2014-04-10 14:09:12 +0200 (Do, 10 Apr 2014) $
 * $LastChangedRevision: 244 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.model.standard.elements;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.ims.icarus.model.api.Container;
import de.ims.icarus.model.api.ContainerType;
import de.ims.icarus.model.api.CorpusMember;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.MemberSet;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.util.CorpusMemberUtils;
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
 * @version $Id: SpanContainer.java 244 2014-04-10 12:09:12Z mcgaerty $
 *
 */
public class SpanContainer extends AbstractContainer {
	private int startIndex = -1;
	private int endIndex = -1;

	public void appendMarkable(Markable markable) {
		// The markable has already been added to its original host container.
		// Therefore we can safely call the indexOf() method
		int index = base().indexOfMarkable(markable);

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
	 * @see de.ims.icarus.model.api.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.SPAN;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		if(startIndex==-1 || endIndex==-1) {
			return 0;
		}

		return endIndex-startIndex+1;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		return base().getMarkableAt(startIndex+index);
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.model.api.Container#addMarkable(int, de.ims.icarus.model.api.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		int size = getMarkableCount();
		if(index!=0 && index!=size)
			throw new IllegalArgumentException(CorpusMemberUtils.outOfBoundsMessage(
					"Invalid index for add", index, 0, size)); //$NON-NLS-1$

		boolean append = index==size;

		execute(new ElementChange(append, true, markable));
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeMarkable(int)
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

		execute(new ElementChange(append, false, null));

		return markable;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#moveMarkable(int, int)
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

		private final Markable markable;
		private final boolean append;
		private final boolean add;
		private int expectedSize;

		public ElementChange(boolean append, boolean add, Markable markable) {
			this.append = append;
			this.markable = markable;
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
					Markable markable = base.getMarkableAt(endIndex+1);
					if(this.markable!=markable)
						throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
								"Add failed (tail)", this.markable, markable)); //$NON-NLS-1$

					endIndex++;
				} else {
					Markable markable = base.getMarkableAt(startIndex-1);
					if(this.markable!=markable)
						throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
								"Add failed (head)", this.markable, markable)); //$NON-NLS-1$

					startIndex--;
				}
			} else {
				Markable markable = null;
				if(append) {
					markable = base.getMarkableAt(endIndex);
					if(this.markable!=markable)
						throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
								"Remove failed (tail)", this.markable, markable)); //$NON-NLS-1$

					endIndex--;
				} else {
					markable = base.getMarkableAt(startIndex);
					if(this.markable!=markable)
						throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
								"Remove failed (head)", this.markable, markable)); //$NON-NLS-1$

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
		private final Markable first;
		private final Markable last;

		private int expectedSize;

		private boolean clear = true;

		public ClearChange() {
			this.expectedStartIndex = startIndex;
			this.expectedEndIndex = endIndex;

			Container base = base();

			first = base.getMarkableAt(expectedStartIndex);
			last = base.getMarkableAt(expectedEndIndex);

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
				Markable markable = base.getMarkableAt(expectedStartIndex);
				if(markable!=first)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Clear failed (head mismatch)", first, markable)); //$NON-NLS-1$
				markable = base.getMarkableAt(expectedEndIndex);
				if(markable!=last)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Clear failed (tail mismatch)", last, markable)); //$NON-NLS-1$

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
				if(markable!=first)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Fill failed (head mismatch)", first, markable)); //$NON-NLS-1$
				markable = base.getMarkableAt(expectedEndIndex);
				if(markable!=last)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Fill failed (tail mismatch)", last, markable)); //$NON-NLS-1$

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
            if (i >= base().getMarkableCount())
                throw new ConcurrentModificationException();
            cursor++;
            return base().getMarkableAt(i);
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
