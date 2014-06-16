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
package de.ims.icarus.model.standard.index;

import de.ims.icarus.model.api.driver.IndexSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ArrayIndexSet implements IndexSet {

	private final long[] indices;
	private final int fromIndex, toIndex;

	public ArrayIndexSet(long[] indices) {
		this(indices, 0, -1);
	}

	public ArrayIndexSet(long[] indices, int numIndices) {
		this(indices, 0, numIndices-1);
	}

	public ArrayIndexSet(long[] indices, int fromIndex, int toIndex) {
		if (indices == null)
			throw new NullPointerException("Invalid indices"); //$NON-NLS-1$
		if(fromIndex<0)
			throw new IllegalArgumentException("Begin index is negative: "+fromIndex); //$NON-NLS-1$

		if(toIndex<0) {
			toIndex = indices.length-1;
		}

		if(fromIndex>toIndex)
			throw new IllegalArgumentException("Begin index exceeds end index: "+toIndex); //$NON-NLS-1$

		this.indices = indices;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.IndexSet#getSize()
	 */
	@Override
	public int size() {
		return toIndex-fromIndex+1;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		return indices[fromIndex+index];
	}

	/**
	 * @see de.ims.icarus.model.api.driver.IndexSet#split(int)
	 */
	@Override
	public IndexSet[] split(int chunkSize) {
		int chunks = (int)Math.ceil((double)size()/chunkSize);

		IndexSet[] result = new IndexSet[chunks];

		int fromIndex = this.fromIndex;
		int toIndex;
		for(int i=0; i<chunks; i++) {
			toIndex = Math.min(fromIndex+chunkSize, indices.length)-1;
			result[i] = new ArrayIndexSet(indices, fromIndex, toIndex);
			fromIndex = toIndex+1;
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		return indices[fromIndex];
	}

	/**
	 * @see de.ims.icarus.model.api.driver.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		return indices[toIndex];
	}
}
