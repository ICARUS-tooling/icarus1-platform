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
import java.util.List;
import java.util.NoSuchElementException;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.ContainerType;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.Edge;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.Structure;
import de.ims.icarus.language.model.api.StructureType;
import de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.api.manifest.StructureManifest;
import de.ims.icarus.language.model.standard.CorpusMemberUtils;
import de.ims.icarus.language.model.standard.LookupList;
import de.ims.icarus.language.model.standard.container.AbstractNestedContainer;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Primitive;

/**
 * Implements an empty structure.
 * <p>
 * This implementation supports adding and removing of {@code Markable} objects
 * but throws an {@code UnsupportedOperationException} for any attempt to
 * add or remove an {@code Edge}.
 * <p>
 * As defined in the {@link Container} specification an optional <i>boundary container</i>
 * can be set at construction time. When a non-null container is set as boundary then all
 * {@code Markable} objects to be added will first be checked for possible boundary violation.
 * Note that no boundary checks are performed for <i>virtual</i> markables
 * (see {@link CorpusUtils#isVirtual(Markable)}).
 * <p>
 * To reduce memory footprint an additional flag can be set to use the members of the <i>boundary
 * container</i> as base markables for this structure, obsoleting the need to maintain a separate
 * list. If the structure needs to define additional markables (as is the case for phrase structure trees)
 * it has to set the {@code augment} flag at construction time. This will enable the internal buffer
 * for new markables. Note that this implementation will fail for every attempt to add or remove markables
 * when augmentation is disabled, since it is not able to modify the underlying <i>boundary container</i>!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class EmptyStructure extends AbstractNestedContainer implements Structure {

	@Link
	private final LookupList<Markable> augmentation;
	@Primitive
	private final boolean boundaryAsBase;
	@Primitive
	private final boolean augment;

	/**
	 * Creates an empty structure with no boundary container
	 * @param id
	 * @param parent
	 */
	public EmptyStructure(long id, Container parent) {
		this(id, parent, null, true, false);
	}

	public EmptyStructure(long id, Container parent, Container boundary, boolean augment, boolean boundaryAsBase) {
		super(id, parent);

		if(!augment && !boundaryAsBase)
			throw new IllegalArgumentException("Must declare either boundary as base or activate augmentation"); //$NON-NLS-1$
		if(boundaryAsBase && boundary==null)
			throw new IllegalArgumentException("Cannot declare null boundary as base"); //$NON-NLS-1$

		setBoundaryContainer(boundary);

		this.boundaryAsBase = boundaryAsBase;

		this.augment = augment;
		augmentation = augment ? new LookupList<Markable>() : null;
	}

	protected void addAllMarkables0(List<? extends Markable> markables) {
		if(!augment)
			throw new UnsupportedOperationException();

		augmentation.addAll(markables);
	}

	protected void addAllMarkables0(Markable...markables) {
		if(!augment)
			throw new UnsupportedOperationException();

		augmentation.addAll(markables);
	}

	protected void addMarkable0(Markable markable) {
		if(!augment)
			throw new UnsupportedOperationException();

		augmentation.add(markable);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.SET;
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.container.AbstractNestedContainer#getManifest()
	 */
	@Override
	public StructureManifest getManifest() {
		return (StructureManifest) super.getManifest();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#isMultiRoot()
	 */
	@Override
	public boolean isMultiRoot() {
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#getEdgeAt(int)
	 */
	@Override
	public Edge getEdgeAt(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#indexOfEdge(de.ims.icarus.language.model.api.Edge)
	 */
	@Override
	public int indexOfEdge(Edge edge) {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#containsEdge(de.ims.icarus.language.model.api.Edge)
	 */
	@Override
	public boolean containsEdge(Edge edge) {
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#getEdgeCount(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public int getEdgeCount(Markable node) {
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#getEdgeAt(de.ims.icarus.language.model.api.Markable, int)
	 */
	@Override
	public Edge getEdgeAt(Markable node, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#getParent(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public Markable getParent(Markable node) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#getRoot()
	 */
	@Override
	public Markable getRoot() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#isRoot(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public boolean isRoot(Markable markable) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#removeAllEdges()
	 */
	@Override
	public void removeAllEdges() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#addEdge(de.ims.icarus.language.model.api.Edge)
	 */
	@Override
	public Edge addEdge(Edge edge) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#addEdge(de.ims.icarus.language.model.api.Edge, int)
	 */
	@Override
	public Edge addEdge(Edge edge, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#addEdge(de.ims.icarus.language.model.api.Markable, de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public Edge addEdge(Markable source, Markable target) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#addEdge(de.ims.icarus.language.model.api.Markable, de.ims.icarus.language.model.api.Markable, int)
	 */
	@Override
	public Edge addEdge(Markable source, Markable target, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#removeEdge(int)
	 */
	@Override
	public Edge removeEdge(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#removeEdge(de.ims.icarus.language.model.api.Edge)
	 */
	@Override
	public Edge removeEdge(Edge edge) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#moveEdge(int, int)
	 */
	@Override
	public void moveEdge(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#moveEdge(de.ims.icarus.language.model.api.Edge, int)
	 */
	@Override
	public void moveEdge(Edge edge, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#setTerminal(de.ims.icarus.language.model.api.Edge, de.ims.icarus.language.model.api.Markable, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Markable markable, boolean isSource) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		int size = 0;
		if(boundaryAsBase) {
			size += getBoundaryContainer().getMarkableCount();
		}
		if(augment) {
			size += augmentation.size();
		}
		return size;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		if(boundaryAsBase) {
			Container boundary = getBoundaryContainer();
			if(index<boundary.getMarkableCount()) {
				return boundary.getMarkableAt(index);
			} else {
				index -= boundary.getMarkableCount();
			}
		}
		if(augment) {
			return augmentation.get(index);
		}

		throw new IndexOutOfBoundsException();
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.container.AbstractContainer#indexOfMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		int result = -1;
		int offset = 0;

		if(boundaryAsBase) {
			offset = getBoundaryContainer().getMarkableCount();
			result = getBoundaryContainer().indexOfMarkable(markable);
		}
		if(result==-1 && augment) {
			result = augmentation.indexOf(markable);
		}

		if(result!=-1) {
			result += offset;
		}

		return result;
	}

	/**
	 * Checks whether or not the given markable is allowed to be added
	 * to this structure. If either the <i>boundary-container</i> is {@code null}
	 * or the given {@code markable} is virtual as determined by
	 * {@link CorpusUtils#isVirtual(Markable)} then this method does nothing.
	 * Otherwise it compares the begin and end offset of the markable with those
	 * of the <i>boundary-container</i>. If one of those indices lays outside the
	 * boundary it will throw an {@code IllegalArgumentException}.
	 * <p>
	 * Subclasses are encouraged to implement their own verification logic as
	 * they see fit.
	 *
	 * @param markable
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code markable} violates the boundary
	 */
	@Override
	protected void checkMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		if(getBoundaryContainer()==null || CorpusUtils.isVirtual(markable)) {
			return;
		}

		if(markable.getBeginOffset()<getBoundaryContainer().getBeginOffset()
				|| markable.getEndOffset()>getBoundaryContainer().getEndOffset())
			throw new IllegalArgumentException("Markable not within boundary: "+markable); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		if(!augment)
			throw new UnsupportedOperationException();

		execute(new ClearMarkablesChange());
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#addMarkable(int, de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		if(!augment)
			throw new UnsupportedOperationException();

		checkMarkable(markable);

		if(boundaryAsBase) {
			index -= getBoundaryContainer().getMarkableCount();
		}

		execute(new MarkableChange(index, markable.getId(), markable));
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		if(!augment)
			throw new UnsupportedOperationException();

		if(boundaryAsBase) {
			index -= getBoundaryContainer().getMarkableCount();
		}

		Markable markable = augmentation.get(index);

		execute(new MarkableChange(index, markable.getId(), null));

		return markable;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		// We cannot move markables belonging to the boundary container!
		if(!augment)
			throw new UnsupportedOperationException();

		if(boundaryAsBase) {
			index0 -= getBoundaryContainer().getMarkableCount();
			index1 -= getBoundaryContainer().getMarkableCount();
		}

		execute(new MoveMarkableChange(index0, index1));
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		return new ComboItr();
	}

	/**
	 * Called when a structural change occurred that makes all
	 * cached data invalid. Subclasses should override this method
	 * and clear internal data structures when called.
	 */
	protected void invalidate() {
		// hook for subclasses to clear caches
	}

	/**
	 * Allows subclasses to intercept execution of an {@code add} change.
	 * This method is intended to be both a notification for the container
	 * to refresh its internal data and an opportunity to verify the correctness
	 * of the change.
	 * <p>
	 * If this method throws an exception no changes will be carried out by
	 * the model!
	 *
	 * @param markable The {@code Markable} that will be added to the container
	 * @param index The position in the list the markable should be placed at
	 */
	protected void markableAdded(Markable markable, int index) {
		// hook for subclasses
	}

	/**
	 * Allows subclasses to intercept execution of an {@code remove} change.
	 * This method is intended to be both a notification for the container
	 * to refresh its internal data and an opportunity to verify the correctness
	 * of the change.
	 * <p>
	 * If this method throws an exception no changes will be carried out by
	 * the model!
	 *
	 * @param markable The {@code Markable} that will be added to the container
	 * @param index The position in the list the markable should be placed at
	 */
	protected void markableRemoved(Markable markable, int index) {
		// hook for subclasses
	}

	/**
	 * Allows subclasses to intercept execution of an {@code move} change.
	 * This method is intended to be both a notification for the container
	 * to refresh its internal data and an opportunity to verify the correctness
	 * of the change.
	 * <p>
	 * If this method throws an exception no changes will be carried out by
	 * the model!
	 *
	 * @param markable The {@code Markable} that will be moved
	 * @param index0 The current position of the markable
	 * @param index1 The position the markable will be moved to
	 */
	protected void markableMoved(Markable markable, int index0, int index1) {
		// hook for subclasses
	}

	private class MarkableChange implements AtomicChange {

		private Markable markable;
		private final int index;
		private final long id;
		private int expectedSize;

		public MarkableChange(int index, long id, Markable markable) {
			this.index = index;
			this.id = id;
			this.markable = markable;
			expectedSize = getMarkableCount();
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(getMarkableCount()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, getMarkableCount())); //$NON-NLS-1$

			if(markable==null) {
				if(augmentation.get(index).getId()!=id)
					throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
							"Removing failed", id, augmentation.get(index).getId())); //$NON-NLS-1$

				markableRemoved(augmentation.get(index), index);

				markable = augmentation.remove(index);
				expectedSize--;
			} else {
				markableAdded(markable, index);

				augmentation.add(index, markable);
				expectedSize++;
				markable = null;
			}
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return EmptyStructure.this;
		}

	}

	private class MoveMarkableChange implements AtomicChange {

		private int indexFrom, indexTo;
		private long idFrom, idTo;
		private int expectedSize;

		public MoveMarkableChange(int indexFrom, int indexTo) {
			this.indexFrom = indexFrom;
			this.indexTo = indexTo;

			idFrom = augmentation.get(indexFrom).getId();
			idTo = augmentation.get(indexTo).getId();

			expectedSize = getMarkableCount();
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(getMarkableCount()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, getMarkableCount())); //$NON-NLS-1$

			Markable m1 = augmentation.get(indexFrom);
			Markable m2 = augmentation.get(indexTo);

			if(m1.getId()!=idFrom)
				throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
						"Moving failed (origin)", idFrom, m1.getId())); //$NON-NLS-1$
			if(m2.getId()!=idTo)
				throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
						"Moving failed (destination)", idTo, m2.getId())); //$NON-NLS-1$

			markableMoved(m1, indexFrom, indexTo);

			augmentation.set(m2, indexFrom);
			augmentation.set(m1, indexTo);

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
			return EmptyStructure.this;
		}

	}

	private class ClearMarkablesChange implements AtomicChange {

		private Object[] items;
		private int expectedSize = getMarkableCount();

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(getMarkableCount()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear failed", expectedSize, getMarkableCount())); //$NON-NLS-1$

			if(items==null) {
				items = augmentation.toArray();
				augmentation.clear();
				expectedSize = 0;
			} else {
				augmentation.set(items);
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
			return EmptyStructure.this;
		}

	}

	private class ComboItr implements Iterator<Markable> {

		private final int expectedSize = getMarkableCount();
		private final Iterator<Markable> baseItr = boundaryAsBase ? getBoundaryContainer().iterator() : null;
		private final Iterator<Markable> augmentItr = augment ? augmentation.iterator() : null;

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			checkModification();

			return (baseItr!=null && baseItr.hasNext())
					|| (augmentItr!=null && augmentItr.hasNext());
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Markable next() {
			checkModification();

			if(baseItr!=null && baseItr.hasNext()) {
				return baseItr.next();
			} else if(augmentItr!=null && augmentItr.hasNext()) {
				return augmentItr.next();
			} else
				throw new NoSuchElementException();
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported"); //$NON-NLS-1$
		}

		private void checkModification() {
			if(getMarkableCount()!=expectedSize)
				throw new ConcurrentModificationException();
		}
	}
}
