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
package de.ims.icarus.language.model.standard.markable;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SingletonFragment extends AbstractFragment {

	private int index;

	/**
	 * @param id
	 * @param container
	 * @param markable
	 */
	public SingletonFragment(long id, Container container, Markable markable, int index) {
		super(id, container, markable);

		setIndex0(index);
	}

	protected void setIndex0(int index) {
		if(index<0)
			throw new IndexOutOfBoundsException("Fragment index must not be negative: "+index); //$NON-NLS-1$

		//TODO add index verification based on textual markable size (might be expensive?)

		this.index = index;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Fragment#getFragmentBeginIndex()
	 */
	@Override
	public int getFragmentBeginIndex() {
		return index;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Fragment#getFragmentEndIndex()
	 */
	@Override
	public int getFragmentEndIndex() {
		return index;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Fragment#setFragmentBeginIndex(int)
	 */
	@Override
	public void setFragmentBeginIndex(int index) {
		execute(new IndexChange(index));
	}

	/**
	 * @see de.ims.icarus.language.model.api.Fragment#setFragmentEndIndex(int)
	 */
	@Override
	public void setFragmentEndIndex(int index) {
		execute(new IndexChange(index));
	}

	private class IndexChange implements AtomicChange {

		private int index;

		public IndexChange(int index) {
			this.index = index;
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			int currentIndex = getFragmentBeginIndex();

			setIndex0(index);

			index = currentIndex;
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return SingletonFragment.this;
		}

	}
}
