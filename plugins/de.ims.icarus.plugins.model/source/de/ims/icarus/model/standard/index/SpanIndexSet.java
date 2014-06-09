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
public class SpanIndexSet implements IndexSet {

	private final long minValue, maxValue;

	public SpanIndexSet(long minValue, long maxValue) {
		if(minValue<0)
			throw new IllegalArgumentException("Min value is negative: "+minValue); //$NON-NLS-1$
		if(maxValue<0)
			throw new IllegalArgumentException("Max value is negative: "+maxValue); //$NON-NLS-1$
		if(minValue>maxValue)
			throw new IllegalArgumentException("Min value exceeds max value: "+maxValue); //$NON-NLS-1$

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.IndexSet#getSize()
	 */
	@Override
	public int size() {
		return (int) (maxValue-minValue+1);
	}

	/**
	 * @see de.ims.icarus.model.api.driver.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		return minValue+index;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.IndexSet#split(int)
	 */
	@Override
	public IndexSet[] split(int chunkSize) {
		int chunks = (int)Math.ceil((double)size()/chunkSize);

		IndexSet[] result = new IndexSet[chunks];

		long minValue = this.minValue;
		long maxValue;
		for(int i=0; i<chunks; i++) {
			maxValue = Math.min(minValue+chunkSize-1, this.maxValue);
			result[i] = new SpanIndexSet(minValue, maxValue);
			minValue = maxValue+1;
		}

		return result;
	}
}
