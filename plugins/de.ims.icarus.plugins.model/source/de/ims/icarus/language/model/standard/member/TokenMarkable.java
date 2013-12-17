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

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MemberType;

/**
 * Implements a markable of the bottom-most level in a corpus.
 * It only holds a piece of text (the <i>token</i> it represents)
 * and the link to its container.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TokenMarkable extends AbstractMarkable implements Markable {

	private final int index;
	private final String text;

	public TokenMarkable(long id, int index, Container container, String text) {
		super(id, container);

		if(text==null)
			throw new NullPointerException("Invalid text"); //$NON-NLS-1$

		this.index = index;
		this.text = text;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.MARKABLE;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getText()
	 */
	@Override
	public String getText() {
		return text;
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) getId() * text.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Markable) {
			Markable other = (Markable)obj;
			return getId()==other.getId() &&
					getCorpus()==other.getCorpus();
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return text;
	}

}
