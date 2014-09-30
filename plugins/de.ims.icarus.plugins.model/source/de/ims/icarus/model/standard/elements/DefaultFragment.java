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

import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.layer.FragmentLayer;
import de.ims.icarus.model.api.members.CorpusMember;
import de.ims.icarus.model.api.members.Fragment;
import de.ims.icarus.model.api.members.Markable;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.api.raster.Position;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class DefaultFragment extends AbstractMarkable implements Fragment {

	@Reference(ReferenceType.UPLINK)
	private Markable markable;
	@Reference(ReferenceType.DOWNLINK)
	private Position fragmentBegin;
	@Reference(ReferenceType.DOWNLINK)
	private Position fragmentEnd;

	/**
	 * @param markable the markable to set
	 */
	public void setMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$
		this.markable = markable;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fragmentBegin.hashCode()*fragmentEnd.hashCode()+1;
	}

	public void setBegin(Position begin) {
		if (begin == null)
			throw new NullPointerException("Invalid begin");  //$NON-NLS-1$

		CorpusUtils.checkFragmentPositions(this, begin, null);

		fragmentBegin = begin;
	}

	public void setEnd(Position end) {
		if (end == null)
			throw new NullPointerException("Invalid end");  //$NON-NLS-1$

		CorpusUtils.checkFragmentPositions(this, null, end);

		fragmentEnd = end;
	}

	public void setSpan(Position begin, Position end) {
		if (begin == null)
			throw new NullPointerException("Invalid begin");  //$NON-NLS-1$
		if (end == null)
			throw new NullPointerException("Invalid end");  //$NON-NLS-1$

		CorpusUtils.checkFragmentPositions(this, begin, end);

		fragmentBegin = begin;
		fragmentEnd = end;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Markable#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return markable.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Markable#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return markable.getEndOffset();
	}

	/**
	 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.FRAGMENT;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Fragment#getMarkable()
	 */
	@Override
	public Markable getMarkable() {
		return markable;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultMarkable.markable.AbstractMarkable#compareTo(de.ims.icarus.model.api.members.Markable)
	 */
	@Override
	public int compareTo(Markable o) {
		return o instanceof Fragment ? CorpusUtils.compare(this, (Fragment)o)
				: CorpusUtils.compare(this, o);
	}

	/**
	 * @see de.ims.icarus.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getContainer().getCorpus();
	}

	/**
	 * @see de.ims.icarus.model.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();

		markable = null;
		fragmentBegin = fragmentEnd = null;
	}

	/**
	 * @see de.ims.icarus.model.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return super.revive() && markable!=null
				&& fragmentBegin!=null && fragmentEnd!=null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Fragment#getLayer()
	 */
	@Override
	public FragmentLayer getLayer() {
		return (FragmentLayer) getContainer().getLayer();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Fragment#getFragmentBegin()
	 */
	@Override
	public Position getFragmentBegin() {
		return fragmentBegin;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Fragment#getFragmentEnd()
	 */
	@Override
	public Position getFragmentEnd() {
		return fragmentEnd;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Fragment#setFragmentBegin(de.ims.icarus.model.api.raster.Position)
	 */
	@Override
	public void setFragmentBegin(Position fragmentBegin) {
		if (fragmentBegin == null)
			throw new NullPointerException("Invalid fragmentBegin");  //$NON-NLS-1$
		execute(new PositionChange(true, fragmentEnd));
	}

	/**
	 * @see de.ims.icarus.model.api.members.Fragment#setFragmentEnd(de.ims.icarus.model.api.raster.Position)
	 */
	@Override
	public void setFragmentEnd(Position fragmentEnd) {
		if (fragmentEnd == null)
			throw new NullPointerException("Invalid fragmentEnd");  //$NON-NLS-1$
		execute(new PositionChange(false, fragmentEnd));
	}

	private class PositionChange implements AtomicChange {

		private final boolean isBegin;
		private Position position;

		public PositionChange(boolean isBegin, Position position) {
			this.isBegin = isBegin;
			this.position = position;
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			Position currentPosition = isBegin ? getFragmentBegin() : getFragmentEnd();

			if(isBegin) {
				setBegin(position);
			} else {
				setEnd(position);
			}

			position = currentPosition;
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return DefaultFragment.this;
		}

	}
}
