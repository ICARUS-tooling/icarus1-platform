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

import de.ims.icarus.model.api.edit.EditOperation;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.members.CorpusMember;
import de.ims.icarus.model.api.members.Item;
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
	private LookupList<Item> list;

	private LookupList<Item> list() {
		if(list==null) {
			synchronized (this) {
				if(list==null) {
					list = new LookupList<>();
				}
			}
		}

		return list;
	}

	public void appendAllItems(List<? extends Item> items) {
		list().addAll(items);
	}

	public void appendAllMarkables(Item...markables) {
		list().addAll(markables);
	}

	public void appendItem(Item item) {
		list().add(item);
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
	 * @see de.ims.icarus.model.api.members.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return (list==null || list.isEmpty()) ? -1 : list.get(0).getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return (list==null || list.isEmpty()) ? -1 : list.get(list.size()-1).getEndOffset();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Item> iterator() {
		return list().iterator();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		return list==null ? 0 : list.size();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getItemAt(int)
	 */
	@Override
	public Item getItemAt(int index) {
		return list().get(index);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#indexOfItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public int indexOfItem(Item item) {
		if (item == null)
			throw new NullPointerException("Invalid item");  //$NON-NLS-1$

		return list==null ? -1 : list.indexOf(item);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#containsItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public boolean containsItem(Item item) {
		if (item == null)
			throw new NullPointerException("Invalid item");  //$NON-NLS-1$

		return list==null ? false : list.contains(item);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		checkContainerAction(EditOperation.CLEAR);
		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#addMarkable(int)
	 */
	@Override
	public void addItem(int index, Item item) {
		checkItem(item);

		EditOperation operation = isRandomAccessMarkableIndex(index) ?
				EditOperation.ADD_RANDOM : EditOperation.ADD;
		checkContainerAction(operation);

		execute(new ElementChange(index, true, item));
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeItem(int)
	 */
	@Override
	public Item removeItem(int index) {
		Item item = getItemAt(index);

		EditOperation operation = isRandomAccessMarkableIndex(index) ?
				EditOperation.REMOVE_RANDOM : EditOperation.REMOVE;
		checkContainerAction(operation);

		execute(new ElementChange(index, false, item));

		return item;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		checkContainerAction(EditOperation.MOVE);
		execute(new MoveChange(index0, index1));
	}

	protected void invalidate() {
		// hook for subclasses to clear caches
	}

	protected void itemAdded(Item item, int index) {
		// hook for subclasses
	}

	protected void itemRemoved(Item item, int index) {
		// hook for subclasses
	}

	protected void itemMoved(Item item, int index0, int index1) {
		// hook for subclasses
	}

	private class ElementChange implements AtomicChange {

		private final Item item;
		private final int index;
		private final boolean add;
		private int expectedSize;

		public ElementChange(int index, boolean add, Item item) {
			this.index = index;
			this.add = add;
			this.item = item;
			expectedSize = list().size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			LookupList<Item> list = list();

			if(list.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, list.size())); //$NON-NLS-1$

			if(add) {
				if(list.indexOf(item)!=-1)
					throw new CorruptedStateException(
							"Add failed, item already contained: "+item); //$NON-NLS-1$

				// Intercept add
				itemAdded(item, index);

				list.add(index, item);
				expectedSize++;
			} else {
				if(list.get(index) != item)
					throw new CorruptedStateException(
							"Removing failed, expected "+item+" at index "+index); //$NON-NLS-1$ //$NON-NLS-2$

				// Intercept remove
				itemRemoved(item, index);

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
		private Item item;
		private int expectedSize;

		public MoveChange(int indexFrom, int indexTo) {
			this.indexFrom = indexFrom;
			this.indexTo = indexTo;

			item = list().get(indexFrom);

			expectedSize = list().size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			LookupList<Item> list = list();

			if(list.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, list.size())); //$NON-NLS-1$

			if(list.get(indexFrom)!=item)
				throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
						"Moving failed (origin)", item, list.get(indexFrom))); //$NON-NLS-1$

			// Intercept move
			itemMoved(item, indexFrom, indexTo);

			list.remove(indexFrom);
			list.add(indexTo, item);

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
			LookupList<Item> list = list();

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
