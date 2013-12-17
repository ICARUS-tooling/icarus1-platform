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
import de.ims.icarus.language.model.manifest.ContainerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractContainer extends AbstractMarkable implements Container {

	/**
	 * @param id
	 * @param container
	 */
	public AbstractContainer(long id, Container container) {
		super(id, container);
		// TODO Auto-generated constructor stub
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
	 * @see de.ims.icarus.language.model.Markable#getText()
	 */
	@Override
	public String getText() {
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

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Markable o) {
		// TODO Auto-generated method stub
		return 0;
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
	 * @see de.ims.icarus.language.model.Container#containsMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		// TODO Auto-generated method stub
		return false;
	}

}
