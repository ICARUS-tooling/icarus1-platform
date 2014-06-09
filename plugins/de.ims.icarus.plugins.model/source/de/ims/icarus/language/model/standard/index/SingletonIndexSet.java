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
package de.ims.icarus.language.model.standard.index;

import de.ims.icarus.language.model.api.driver.IndexSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SingletonIndexSet implements IndexSet {

	private final long index;

	public SingletonIndexSet(long index) {
		if(index<0)
			throw new IllegalArgumentException("Index is negative: "+index); //$NON-NLS-1$

		this.index = index;
	}

	/**
	 * @see de.ims.icarus.language.model.api.driver.IndexSet#size()
	 */
	@Override
	public int size() {
		return 1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.driver.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		if(index!=0)
			throw new IndexOutOfBoundsException();

		return this.index;
	}

	/**
	 * This implementation wraps itself into a new array of size {@code 1} and returns
	 * that array.
	 *
	 * @see de.ims.icarus.language.model.api.driver.IndexSet#split(int)
	 */
	@Override
	public IndexSet[] split(int chunkSize) {
		return new IndexSet[]{this};
	}

}
