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
package de.ims.icarus.model.standard.elements.dummy;

import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.util.CorpusUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyMarkable implements Item {

	/**
	 * @see de.ims.icarus.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.MARKABLE;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#getContainer()
	 */
	@Override
	public Container getContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#getIndex()
	 */
	@Override
	public long getIndex() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#setIndex(long)
	 */
	@Override
	public void setIndex(long newIndex) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#compareTo(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public int compareTo(Item o) {
		return CorpusUtils.compare(this, o);
	}

}
