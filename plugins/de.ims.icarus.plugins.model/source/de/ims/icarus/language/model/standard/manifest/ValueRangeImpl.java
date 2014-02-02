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
package de.ims.icarus.language.model.standard.manifest;

import de.ims.icarus.language.model.manifest.ValueRange;

public class ValueRangeImpl implements ValueRange {

	private Object lower, upper;
	private final boolean lowerIncluded, upperIncluded;

	public ValueRangeImpl(boolean lowerIncluded,
			boolean upperIncluded) {
		this.lowerIncluded = lowerIncluded;
		this.upperIncluded = upperIncluded;
	}

	public ValueRangeImpl(Object lower, Object upper, boolean lowerIncluded,
			boolean upperIncluded) {
		this(lowerIncluded, upperIncluded);

		this.lower = lower;
		this.upper = upper;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ValueRange#getLowerBound()
	 */
	@Override
	public Object getLowerBound() {
		return lower;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ValueRange#getUpperBound()
	 */
	@Override
	public Object getUpperBound() {
		return upper;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ValueRange#isLowerBoundInclusive()
	 */
	@Override
	public boolean isLowerBoundInclusive() {
		return lowerIncluded;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ValueRange#isUpperBoundInclusive()
	 */
	@Override
	public boolean isUpperBoundInclusive() {
		return upperIncluded;
	}

	/**
	 * @param lower the lower to set
	 */
	public void setLowerBound(Object lower) {
		this.lower = lower;
	}

	/**
	 * @param upper the upper to set
	 */
	public void setUpperBound(Object upper) {
		this.upper = upper;
	}

}