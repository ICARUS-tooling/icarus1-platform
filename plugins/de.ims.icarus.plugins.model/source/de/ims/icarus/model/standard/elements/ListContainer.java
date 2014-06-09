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

import java.util.Iterator;
import java.util.List;

import de.ims.icarus.model.api.CorpusMember;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.edit.EditOperation;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.util.CorpusMemberUtils;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.LookupList;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class ListContainer extends AbstractContainer {

	// Internal storage, will be created lazily, but never
	// destroyed outside of a recycle() call !
	@Reference
	private LookupList<Markable> list;

	private LookupList<Markable> list() {
		if(list==null) {
			synchronized (this) {
				if(list==null) {
					list = new LookupList<>();
				}
			}
		}

		return list;
	}

	public void appendAllMarkables(List<? extends Markable> markables) {
		list().addAll(markables);
	}

	public void appendAllMarkables(Markable...markables) {
		list().addAll(markables);
	}

	public void appendMarkable(Markable markable) {
		list().add(markable);
	}

	public void clear() {
		list().clear();
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.AbstractContainer#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();

		if(list!=null); {
			list.clear();
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.AbstractContainer#revive()
	 */
	@Override
	public boolean revive() {
		if(!super.revive()) {
			return false;
		}

		// Trim down memory footprint of internal storage
		if(list!=null) {
			list.trim();
		}

		return true;
	}

	/**
	 * @see de.ims.icarus.model.api.Markable#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return (list==null || list.isEmpty()) ? -1 : list.get(0).getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.model.api.Markable#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return (list==null || list.isEmpty()) ? -1 : list.get(list.size()-1).getEndOffset();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		return list().iterator();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		return list==null ? 0 : list.size();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		return list().get(index);
	}

	/**
	 * @see de.ims.icarus.model.api.Container#indexOfMarkable(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		return list==null ? -1 : list.indexOf(markable);
	}

	/**
	 * @see de.ims.icarus.model.api.Container#containsMarkable(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		return list==null ? false : list.contains(markable);
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		checkContainerAction(EditOperation.CLEAR);
		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.model.api.Container#addMarkable(int)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		checkMarkable(markable);

		EditOperation operation = isRandomAccessMarkableIndex(index) ?
				EditOperation.ADD_RANDOM : EditOperation.ADD;
		checkContainerAction(operation);

		execute(new ElementChange(index, true, markable));
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		Markable markable = getMarkableAt(index);

		EditOperation operation = isRandomAccessMarkableIndex(index) ?
				EditOperation.REMOVE_RANDOM : EditOperation.REMOVE;
		checkContainerAction(operation);

		execute(new ElementChange(index, false, markable));

		return markable;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		checkContainerAction(EditOperation.MOVE);
		execute(new MoveChange(index0, index1));
	}

	protected void invalidate() {
		// hook for subclasses to clear caches
	}

	protected void markableAdded(Markable markable, int index) {
		// hook for subclasses
	}

	protected void markableRemoved(Markable markable, int index) {
		// hook for subclasses
	}

	protected void markableMoved(Markable markable, int index0, int index1) {
		// hook for subclasses
	}

	private class ElementChange implements AtomicChange {

		private final Markable markable;
		private final int index;
		private final boolean add;
		private int expectedSize;

		public ElementChange(int index, boolean add, Markable markable) {
			this.index = index;
			this.add = add;
			this.markable = markable;
			expectedSize = list().size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			LookupList<Markable> list = list();

			if(list.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, list.size())); //$NON-NLS-1$

			if(add) {
				if(list.indexOf(markable)!=-1)
					throw new CorruptedStateException(
							"Add failed, markable already contained: "+markable); //$NON-NLS-1$

				// Intercept add
				markableAdded(markable, index);

				list.add(index, markable);
				expectedSize++;
			} else {
				if(list.get(index) != markable)
					throw new CorruptedStateException(
							"Removing failed, expected "+markable+" at index "+index); //$NON-NLS-1$ //$NON-NLS-2$

				// Intercept remove
				markableRemoved(markable, index);

				list.remove(index);
				expectedSize--;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ListContainer.this;
		}

	}

	private class MoveChange implements AtomicChange {

		private int indexFrom, indexTo;
		private Markable markable;
		private int expectedSize;

		public MoveChange(int indexFrom, int indexTo) {
			this.indexFrom = indexFrom;
			this.indexTo = indexTo;

			markable = list().get(indexFrom);

			expectedSize = list().size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			LookupList<Markable> list = list();

			if(list.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, list.size())); //$NON-NLS-1$

			if(list.get(indexFrom)!=markable)
				throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
						"Moving failed (origin)", markable, list.get(indexFrom))); //$NON-NLS-1$

			// Intercept move
			markableMoved(markable, indexFrom, indexTo);

			list.remove(indexFrom);
			list.add(indexTo, markable);

			int tmp = indexFrom;
			indexFrom = indexTo;
			indexTo = tmp;
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ListContainer.this;
		}

	}

	private class ClearChange implements AtomicChange {

		private Object[] items;
		private int expectedSize = list().size();

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			LookupList<Markable> list = list();

			if(list.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear failed", expectedSize, list.size())); //$NON-NLS-1$

			if(items==null) {
				items = list.toArray();
				list.clear();
				expectedSize = 0;
			} else {
				list.set(items);
				expectedSize = items.length;
				items = null;
			}

			invalidate();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ListContainer.this;
		}

	}
}
