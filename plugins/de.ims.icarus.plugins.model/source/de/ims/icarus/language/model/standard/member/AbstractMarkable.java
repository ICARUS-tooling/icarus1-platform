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
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.util.CorpusUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractMarkable implements Markable {

	private final long id;
	private final Container container;

	public AbstractMarkable(long id, Container container) {
		if(container==null)
			throw new NullPointerException("Invalid container"); //$NON-NLS-1$

		this.id = id;
		this.container = container;
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
		return container.getCorpus();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Markable o) {
		return CorpusUtils.compare(this, o);
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		return container;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return container.getLayer();
	}
}
