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

import java.util.Iterator;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.MemberType;
import de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.manifest.ContainerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RootContainer extends AbstractContainer {

	private final MarkableLayer layer;
	private final ContainerManifest manifest;

	private final LookupList<Markable> list = new LookupList<>();

	public RootContainer(MarkableLayer layer, ContainerManifest manifest) {
		if (layer == null)
			throw new NullPointerException("Invalid layer");  //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest");  //$NON-NLS-1$

		this.layer = layer;
		this.manifest = manifest;
	}

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
		return list.size();
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return layer.getCorpus();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		return list.iterator();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return manifest;
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
		return list.size();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		return list.get(index);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#indexOfMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		return list.indexOf(markable);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#containsMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		return list.contains(markable);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Container#addMarkable()
	 */
	@Override
	public Markable addMarkable() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#addMarkable(int)
	 */
	@Override
	public Markable addMarkable(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable removeMarkable(Markable markable) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Container#moveMarkable(de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public void moveMarkable(Markable markable, int index) {
		// TODO Auto-generated method stub

	}

	private class ElementChange implements AtomicChange {

		private Markable markable;
		private final int index;

		public ElementChange(int index, Markable markable) {
			this.index = index;
			this.markable = markable;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(markable==null) {
				markable = list.remove(index);
			} else {
				list.add(index, markable);
				markable = null;
			}
		}

	}

	private class MoveChange implements AtomicChange {

		private int indexFrom, indexTo;

		public MoveChange(int indexFrom, int indexTo) {
			this.indexFrom = indexFrom;
			this.indexTo = indexTo;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			Markable m1 = list.get(indexFrom);
			Markable m2 = list.get(indexTo);

			list.set(m2, indexFrom);
			list.set(m1, indexTo);

			int tmp = indexFrom;
			indexFrom = indexTo;
			indexTo = tmp;
		}

	}

	public static class BaseMarkable extends AbstractMarkable {

		private final int index;

		/**
		 * @param id
		 * @param container
		 */
		public BaseMarkable(Container container, int index) {
			super(container);

			this.index = index;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getBeginOffset()
		 */
		@Override
		public int getBeginOffset() {
			return index;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getEndOffset()
		 */
		@Override
		public int getEndOffset() {
			return index;
		}

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.MARKABLE;
		}

	}
}
