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

import de.ims.icarus.model.api.Container;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.CorpusMember;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.model.util.Recyclable;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Primitive;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public abstract class AbstractMarkable implements Markable, Recyclable {

	@Primitive
	private long index = -1;
	@Reference(ReferenceType.UPLINK)
	private Container container;

	/**
	 * @see de.ims.icarus.model.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		index = -1;
		container = null;
	}

	/**
	 * @see de.ims.icarus.model.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return true;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(Container container) {
		if(container==null)
			throw new NullPointerException("Invalid container"); //$NON-NLS-1$
		this.container = container;
	}

	/**
	 * @see de.ims.icarus.model.api.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getContainer().getCorpus();
	}

	/**
	 * @see de.ims.icarus.model.api.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		return container;
	}

	/**
	 * @see de.ims.icarus.model.api.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return getContainer().getLayer();
	}

	/**
	 * Helper method to check whether or not the enclosing corpus is editable
	 * and to forward an atomic change to the edit model.
	 *
	 * @param change
	 * @throws UnsupportedOperationException if the corpus is not editable
	 */
	protected void execute(AtomicChange change) {
		CorpusUtils.dispatchChange(this, change);
	}


	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) getIndex();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this==obj;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return CorpusUtils.toString(this);
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Markable o) {
		return CorpusUtils.compare(this, o);
	}

	/**
	 * @see de.ims.icarus.model.api.Markable#getIndex()
	 */
	@Override
	public long getIndex() {
		return index;
	}

	/**
	 * Public way of bypassing the edit framework in order to change this
	 * markable's global index value.
	 *
	 * @param newIndex
	 */
	public void changeIndex(long newIndex) {
		if(newIndex<-1)
			throw new IllegalArgumentException("Index must be greater or equal to -1: "+newIndex); //$NON-NLS-1$

		this.index = newIndex;
	}

	/**
	 * @see de.ims.icarus.model.api.Markable#setIndex(long)
	 */
	@Override
	public void setIndex(long newIndex) {
		if(newIndex==index) {
			return;
		}

		execute(new IndexChange(newIndex));
	}

	private class IndexChange implements AtomicChange {
		private long expectedIndex = index;
		private long newIndex;

		private IndexChange(long newIndex) {
			this.newIndex = newIndex;
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(index!=expectedIndex)
				throw new CorruptedStateException("Expected index "+expectedIndex+" - got "+index); //$NON-NLS-1$ //$NON-NLS-2$

			// Fail-fast for invalid index values
			changeIndex(newIndex);

			long tmp = expectedIndex;
			expectedIndex = newIndex;
			newIndex = tmp;
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractMarkable.this;
		}
	}
}
