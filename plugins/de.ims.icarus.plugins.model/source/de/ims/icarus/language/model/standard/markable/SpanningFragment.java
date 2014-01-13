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

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.CorpusMember;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SpanningFragment extends AbstractFragment {

	private int beginIndex = -1;
	private int endIndex = -1;

	public SpanningFragment(long id, Container container, Markable markable,
			int beginIndex, int endIndex) {
		super(id, container, markable);

		setBeginIndex0(beginIndex);
		setEndIndex0(endIndex);
	}

	protected void setBeginIndex0(int index) {
		if(index<0 || (endIndex!=-1 && index>endIndex))
			throw new IndexOutOfBoundsException("Begin offset out of bounds: "+index); //$NON-NLS-1$

		//TODO add index verification based on textual markable size (might be expensive?)

		beginIndex = index;
	}

	protected void setEndIndex0(int index) {
		if(index<0 || (beginIndex!=-1 && index<beginIndex))
			throw new IndexOutOfBoundsException("End offset out of bounds: "+index); //$NON-NLS-1$

		//TODO add index verification based on textual markable size (might be expensive?)

		endIndex = index;
	}

	/**
	 * @see de.ims.icarus.language.model.Fragment#getFragmentBeginIndex()
	 */
	@Override
	public int getFragmentBeginIndex() {
		return beginIndex;
	}

	/**
	 * @see de.ims.icarus.language.model.Fragment#getFragmentEndIndex()
	 */
	@Override
	public int getFragmentEndIndex() {
		return endIndex;
	}

	/**
	 * @see de.ims.icarus.language.model.Fragment#setFragmentBeginIndex(int)
	 */
	@Override
	public void setFragmentBeginIndex(int index) {
		execute(new IndexChange(true, index));
	}

	/**
	 * @see de.ims.icarus.language.model.Fragment#setFragmentEndIndex(int)
	 */
	@Override
	public void setFragmentEndIndex(int index) {
		execute(new IndexChange(false, index));
	}

	private class IndexChange implements AtomicChange {

		private final boolean isBegin;
		private int index;

		public IndexChange(boolean isBegin, int index) {
			this.isBegin = isBegin;
			this.index = index;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			int currentIndex = isBegin ? getFragmentBeginIndex() : getFragmentEndIndex();

			if(isBegin) {
				setBeginIndex0(index);
			} else {
				setEndIndex0(index);
			}

			index = currentIndex;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return SpanningFragment.this;
		}

	}
}
