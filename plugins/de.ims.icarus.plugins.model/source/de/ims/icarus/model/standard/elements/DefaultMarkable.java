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

import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.util.Recyclable;
import de.ims.icarus.util.mem.HeapMember;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class DefaultMarkable extends AbstractMarkable implements Recyclable {

	public DefaultMarkable() {
		// no-op
	}

	public DefaultMarkable(Container container) {
		setContainer(container);
	}

	public DefaultMarkable(Container container, long offset) {
		setContainer(container);
		setOffset(offset);
	}

	/**
	 * @see de.ims.icarus.model.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return super.revive() && getIndex()>-1 && getContainer()!=null;
	}

	protected void checkOffset(long offset) {
		if(offset<-1)
			throw new IllegalArgumentException("Offset must be greater or equal to 0: "+offset); //$NON-NLS-1$
	}

	/**
	 * @param offset the offset to set
	 */
	public void setOffset(long offset) {
		checkOffset(offset);
		changeIndex(offset);
	}

	/**
	 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.MARKABLE;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Markable#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return getIndex();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Markable#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return getIndex();
	}
}
