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

import java.util.Iterator;
import java.util.List;

import de.ims.icarus.language.model.api.ContainerType;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.standard.CorpusMemberUtils;
import de.ims.icarus.language.model.standard.LookupList;
import de.ims.icarus.language.model.standard.builder.AbstractContainerBuilder;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractListContainer extends AbstractContainer {

	private final LookupList<Markable> list = new LookupList<>();

	public AbstractListContainer(long id) {
		super(id);
	}

	protected void addAllMarkables0(List<? extends Markable> markables) {
		list.addAll(markables);
	}

	protected void addAllMarkables0(Markable...markables) {
		list.addAll(markables);
	}

	protected void addMarkable0(Markable markable) {
		list.add(markable);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getBeginOffset()
	 */
	@Override
	public int getBeginOffset() {
		return list.isEmpty() ? -1 : list.get(0).getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return list.isEmpty() ? -1 : list.get(list.size()-1).getEndOffset();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		return list.iterator();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		return list.size();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		return list.get(index);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#indexOfMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		return list.indexOf(markable);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#containsMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		return list.contains(markable);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#addMarkable(int)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		checkMarkable(markable);

		execute(new ElementChange(index, markable.getId(), markable));
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		Markable markable = getMarkableAt(index);

		execute(new ElementChange(index, markable.getId(), null));

		return markable;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public Markable removeMarkable(Markable markable) {
		int index = indexOfMarkable(markable);

		execute(new ElementChange(index, markable.getId(), null));

		return markable;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		execute(new MoveChange(index0, index1));
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#moveMarkable(de.ims.icarus.language.model.api.Markable, int)
	 */
	@Override
	public void moveMarkable(Markable markable, int index) {
		int index0 = indexOfMarkable(markable);

		execute(new MoveChange(index0, index));
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

		private Markable markable;
		private final int index;
		private final long id;
		private int expectedSize;

		public ElementChange(int index, long id, Markable markable) {
			this.index = index;
			this.id = id;
			this.markable = markable;
			expectedSize = list.size();
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(list.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, list.size())); //$NON-NLS-1$

			if(markable==null) {
				if(list.get(index).getId()!=id)
					throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
							"Removing failed", id, list.get(index).getId())); //$NON-NLS-1$

				markable = list.remove(index);
				expectedSize--;
				markableRemoved(markable, index);
			} else {
				list.add(index, markable);
				expectedSize++;
				markableAdded(markable, index);
				markable = null;
			}
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractListContainer.this;
		}

	}

	private class MoveChange implements AtomicChange {

		private int indexFrom, indexTo;
		private long idFrom, idTo;
		private int expectedSize;

		public MoveChange(int indexFrom, int indexTo) {
			this.indexFrom = indexFrom;
			this.indexTo = indexTo;

			idFrom = list.get(indexFrom).getId();
			idTo = list.get(indexTo).getId();

			expectedSize = list.size();
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(list.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, list.size())); //$NON-NLS-1$

			Markable m1 = list.get(indexFrom);
			Markable m2 = list.get(indexTo);

			if(m1.getId()!=idFrom)
				throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
						"Moving failed (origin)", idFrom, m1.getId())); //$NON-NLS-1$
			if(m2.getId()!=idTo)
				throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
						"Moving failed (destination)", idTo, m2.getId())); //$NON-NLS-1$

			list.set(m2, indexFrom);
			list.set(m1, indexTo);

			markableMoved(m1, indexFrom, indexTo);

			int tmp = indexFrom;
			indexFrom = indexTo;
			indexTo = tmp;

			long idTmp = idFrom;
			idFrom = idTo;
			idTo = idTmp;
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractListContainer.this;
		}

	}

	private class ClearChange implements AtomicChange {

		private Object[] items;
		private int expectedSize = list.size();

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
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
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractListContainer.this;
		}

	}

	public static abstract class ListContainerBuilder<C extends AbstractListContainer> extends AbstractContainerBuilder<C> {

		/**
		 * @see de.ims.icarus.language.model.api.standard.builder.ContainerBuilder#addMarkable(de.ims.icarus.language.model.api.Markable)
		 */
		@Override
		public void addMarkable(Markable markable) {
			container.addMarkable0(markable);
		}

	}
}
