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
package de.ims.icarus.language.model.standard.member;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.ContainerType;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.MemberType;
import de.ims.icarus.language.model.edit.ContainerMutator;
import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.collections.LongIntHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RootContainer implements Container {

	private final long id;
	private final MarkableLayer layer;

	private final List<Markable> markables = new ArrayList<>();
	private final LongIntHashMap indexLookup = new LongIntHashMap();

	/**
	 * @see de.ims.icarus.language.model.Markable#getText()
	 */
//	@Override
//	public String getText() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	/**
	 * Since this implementation represents the top-level container
	 * of a corpus there is no enclosing container. Therefore this
	 * method always returns {@code null}.
	 *
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return layer;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getBeginOffset()
	 */
	@Override
	public int getBeginOffset() {
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return markables.size();
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getId()
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return layer.getCorpus();
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.CONTAINER;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Markable o) {
		return CorpusUtils.compare(this, o);
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		return markables.iterator();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getBaseContainer()
	 */
	@Override
	public Container getBaseContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		return markables.size();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		return markables.get(index);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#indexOfMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		return m;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#containsMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMutator()
	 */
	@Override
	public ContainerMutator getMutator() {
		// TODO Auto-generated method stub
		return null;
	}

	private static class BaseMarkable implements Markable {

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getId()
		 */
		@Override
		public long getId() {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Markable o) {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getContainer()
		 */
		@Override
		public Container getContainer() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getLayer()
		 */
		@Override
		public MarkableLayer getLayer() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getBeginOffset()
		 */
		@Override
		public int getBeginOffset() {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getEndOffset()
		 */
		@Override
		public int getEndOffset() {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	private static class RootContainerMutator implements ContainerMutator {

		/**
		 * @see de.ims.icarus.language.model.edit.Mutator#getSubject()
		 */
		@Override
		public Container getSubject() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.batch.BatchMutator#beginBatch()
		 */
		@Override
		public boolean beginBatch() {
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.batch.BatchMutator#discardBatch()
		 */
		@Override
		public void discardBatch() {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.batch.BatchMutator#executeBatch()
		 */
		@Override
		public boolean executeBatch() {
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#setContainerType(de.ims.icarus.language.model.ContainerType)
		 */
		@Override
		public void setContainerType(ContainerType containerType) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#removeAllMarkables()
		 */
		@Override
		public void removeAllMarkables() {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#addMarkable()
		 */
		@Override
		public Markable addMarkable() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#addMarkable(int)
		 */
		@Override
		public Markable addMarkable(int index) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#removeMarkable(int)
		 */
		@Override
		public Markable removeMarkable(int index) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#removeMarkable(de.ims.icarus.language.model.Markable)
		 */
		@Override
		public Markable removeMarkable(Markable markable) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#moveMarkable(int, int)
		 */
		@Override
		public void moveMarkable(int index0, int index1) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#moveMarkable(de.ims.icarus.language.model.Markable, int)
		 */
		@Override
		public void moveMarkable(Markable markable, int index) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#batchSetContainerType(de.ims.icarus.language.model.ContainerType)
		 */
		@Override
		public void batchSetContainerType(ContainerType containerType) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#batchRemoveAll()
		 */
		@Override
		public void batchRemoveAll() {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#batchAddMarkable()
		 */
		@Override
		public void batchAddMarkable() {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#batchAddMarkable(int)
		 */
		@Override
		public void batchAddMarkable(int index) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#batchRemoveMarkable(int)
		 */
		@Override
		public void batchRemoveMarkable(int index) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#batchRemoveMarkable(de.ims.icarus.language.model.Markable)
		 */
		@Override
		public void batchRemoveMarkable(Markable markable) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#batchMoveMarkable(int, int)
		 */
		@Override
		public void batchMoveMarkable(int index0, int index1) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.language.model.edit.ContainerMutator#batchMoveMarkable(de.ims.icarus.language.model.Markable, int)
		 */
		@Override
		public void batchMoveMarkable(Markable markable, int index1) {
			// TODO Auto-generated method stub

		}

	}
}
