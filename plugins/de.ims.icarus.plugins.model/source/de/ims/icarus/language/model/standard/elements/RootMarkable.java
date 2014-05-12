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
package de.ims.icarus.language.model.standard.elements;

import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.Structure;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class RootMarkable implements Markable {

	@Reference(ReferenceType.UPLINK)
	private final Structure owner;

	public RootMarkable(Structure owner) {
		if (owner == null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		this.owner = owner;
	}

	/**
	 * @see de.ims.icarus.language.model.api.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getContainer().getCorpus();
	}

	/**
	 * @see de.ims.icarus.language.model.api.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.MARKABLE;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Markable o) {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getContainer()
	 */
	@Override
	public Structure getContainer() {
		return owner;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return getContainer().getLayer();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getIndex()
	 */
	@Override
	public long getIndex() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#setIndex(long)
	 */
	@Override
	public void setIndex(long newIndex) {
		throw new UnsupportedOperationException("ROOT nodes cannot have index values assigned"); //$NON-NLS-1$
	}

}
