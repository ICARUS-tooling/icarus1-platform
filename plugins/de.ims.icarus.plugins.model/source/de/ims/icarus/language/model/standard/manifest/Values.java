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

import java.util.Iterator;
import java.util.List;

import de.ims.icarus.language.model.manifest.ValueIterator;
import de.ims.icarus.language.model.manifest.ValueRange;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Values {

	public interface ValueIteratorFactory {
		ValueIterator newIterator();
	}

	static class FixedValueIteratorFactory implements ValueIteratorFactory {

		private final List<Object> values;

		FixedValueIteratorFactory(List<Object> values) {
			if (values == null)
				throw new NullPointerException("Invalid values"); //$NON-NLS-1$
			if( values.isEmpty() )
				throw new IllegalArgumentException("Empty value list"); //$NON-NLS-1$

			this.values = values;
		}

		/**
		 * @see de.ims.icarus.language.model.standard.manifest.Values.ValueIteratorFactory#newIterator()
		 */
		@Override
		public ValueIterator newIterator() {
			return new FixedValueIterator(values.iterator());
		}
	}

	static class FixedValueIterator implements ValueIterator {

		private final Iterator<Object> cursor;

		FixedValueIterator(Iterator<Object> cursor) {
			if (cursor == null)
				throw new NullPointerException("Invalid cursor"); //$NON-NLS-1$

			this.cursor = cursor;
		}


		/**
		 * @see de.ims.icarus.language.model.manifest.ValueIterator#hasMoreValues()
		 */
		@Override
		public boolean hasMoreValues() {
			return cursor.hasNext();
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.ValueIterator#nextValue()
		 */
		@Override
		public Object nextValue() {
			return cursor.next();
		}
	}

	public ValueIteratorFactory newFixedSetFactory(List<Object> values) {
		return new FixedValueIteratorFactory(values);
	}

	static class IntegerValueFactory implements ValueIteratorFactory {
		private final int min;
		private final int max;
		private final int stepSize;

		IntegerValueFactory(int min, int max, int stepSize) {
			if(min>max-stepSize)
				throw new IllegalArgumentException("Invalid range values: min=" //$NON-NLS-1$
						+min+" max="+max+" stepSize="+stepSize); //$NON-NLS-1$ //$NON-NLS-2$

			this.min = min;
			this.max = max;
			this.stepSize = stepSize;
		}

		/**
		 * @see de.ims.icarus.language.model.standard.manifest.Values.ValueIteratorFactory#newIterator()
		 */
		@Override
		public ValueIterator newIterator() {
			return new IntegerValueIterator(max, min, stepSize);
		}
	}

	static class IntegerValueIterator implements ValueIterator {
		private final int max;
		private int current;
		private final int stepSize;

		IntegerValueIterator(int max, int current, int stepSize) {
			this.max = max;
			this.current = current;
			this.stepSize = stepSize;
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.ValueIterator#hasMoreValues()
		 */
		@Override
		public boolean hasMoreValues() {
			return current<max-stepSize;
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.ValueIterator#nextValue()
		 */
		@Override
		public Object nextValue() {
			int result = current;

			current += stepSize;

			return result;
		}
	}

	public static ValueIteratorFactory newIntegerRangeFactory(int min, int max, int stepSize) {
		return new IntegerValueFactory(min, max, stepSize);
	}

	public static ValueIteratorFactory newIntegerRangeFactory(int min, int max) {
		return new IntegerValueFactory(min, max, 1);
	}

	public static ValueIteratorFactory newIntegerRangeFactory(int max) {
		return new IntegerValueFactory(0, max, 1);
	}

	static class DoubleValueFactory implements ValueIteratorFactory {
		private final double min;
		private final double max;
		private final double stepSize;

		DoubleValueFactory(double min, double max, double stepSize) {
			if(min>max-stepSize)
				throw new IllegalArgumentException("Invalid range values: min=" //$NON-NLS-1$
						+min+" max="+max+" stepSize="+stepSize); //$NON-NLS-1$ //$NON-NLS-2$

			this.min = min;
			this.max = max;
			this.stepSize = stepSize;
		}

		/**
		 * @see de.ims.icarus.language.model.standard.manifest.Values.ValueIteratorFactory#newIterator()
		 */
		@Override
		public ValueIterator newIterator() {
			return new DoubleValueIterator(max, min, stepSize);
		}
	}

	static class DoubleValueIterator implements ValueIterator {
		private final double max;
		private double current;
		private final double stepSize;

		DoubleValueIterator(double max, double current, double stepSize) {
			this.max = max;
			this.current = current;
			this.stepSize = stepSize;
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.ValueIterator#hasMoreValues()
		 */
		@Override
		public boolean hasMoreValues() {
			return current<max-stepSize;
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.ValueIterator#nextValue()
		 */
		@Override
		public Object nextValue() {
			double result = current;

			current += stepSize;

			return result;
		}
	}

	public static ValueIteratorFactory newDoubleRangeFactory(double min, double max, double stepSize) {
		return new DoubleValueFactory(min, max, stepSize);
	}

	public static ValueIteratorFactory newDoubleRangeFactory(double min, double max) {
		return new DoubleValueFactory(min, max, 1);
	}

	public static ValueIteratorFactory newDoubleRangeFactory(double max) {
		return new DoubleValueFactory(0, max, 1);
	}

	static class ValueRangeImpl implements ValueRange {

		private final Object lower, upper;
		private final boolean lowerIncluded, upperIncluded;

		public ValueRangeImpl(Object lower, Object upper, boolean lowerIncluded,
				boolean upperIncluded) {
			this.lower = lower;
			this.upper = upper;
			this.lowerIncluded = lowerIncluded;
			this.upperIncluded = upperIncluded;
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

	}

	public static ValueRange newValueRange(Object lower, Object upper) {
		return new ValueRangeImpl(lower, upper, true, true);
	}

	public static ValueRange newValueRange(Object lower, Object upper, boolean lowerIncluded, boolean upperIncluded) {
		return new ValueRangeImpl(lower, upper, lowerIncluded, upperIncluded);
	}
}
