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
package de.ims.icarus.plugins.prosody.search.constraints.painte;

import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AggregationMode {

	/**
	 * Generate an aggregated value of the values each syllable in the current
	 * target node is assigned for the given key. Note that this method is never
	 * called for nodes that have no syllables!
	 */
	public abstract Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex);

	public static final AggregationMode minValue = new AggregationMode() {

		@SuppressWarnings("unchecked")
		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex) {
			@SuppressWarnings("rawtypes")
			Comparable min = null;

			for(int i=fromIndex; i<=toIndex; i++) {
				@SuppressWarnings("rawtypes")
				Comparable value = (Comparable) tree.getProperty(key, i);
				if(min==null || value.compareTo(min)<0) {
					min = value;
				}
			}

			return min;
		}
	};

	public static final AggregationMode maxValue = new AggregationMode() {

		@SuppressWarnings("unchecked")
		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex) {
			@SuppressWarnings("rawtypes")
			Comparable max = null;

			for(int i=fromIndex; i<=toIndex; i++) {
				@SuppressWarnings("rawtypes")
				Comparable value = (Comparable) tree.getProperty(key, i);
				if(max==null || value.compareTo(max)>0) {
					max = value;
				}
			}

			return max;
		}
	};

	public static final AggregationMode firstValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex) {

			return tree.getProperty(key, 0);
		}
	};

	public static final AggregationMode lastValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex) {

			return tree.getProperty(key, tree.getSyllableCount()-1);
		}
	};

	public static final AggregationMode avgIntegerValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex) {
			int sum = 0;

			for(int i=0; i<tree.getSyllableCount(); i++) {
				sum += (int) tree.getProperty(key, i);
			}

			return sum/(toIndex-fromIndex+1);
		}
	};

	public static final AggregationMode avgLongValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex) {
			long sum = 0L;

			for(int i=0; i<tree.getSyllableCount(); i++) {
				sum += (long) tree.getProperty(key, i);
			}

			return sum/(long)(toIndex-fromIndex+1);
		}
	};

	public static final AggregationMode avgFloatValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex) {
			float sum = 0F;

			for(int i=0; i<tree.getSyllableCount(); i++) {
				sum += (float) tree.getProperty(key, i);
			}

			return sum/(float)(toIndex-fromIndex+1);
		}
	};

	public static final AggregationMode avgDoubleValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex) {
			double sum = 0D;

			for(int i=0; i<tree.getSyllableCount(); i++) {
				sum += (double) tree.getProperty(key, i);
			}

			return sum/(double)(toIndex-fromIndex+1);
		}
	};

	public static class SingletonAggregation extends AggregationMode {

		private final int index;

		public SingletonAggregation(int index) {
			this.index = index;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.painte.AggregationMode#getAggregatedValue(de.ims.icarus.plugins.prosody.search.ProsodyTargetTree, java.lang.String, int, int)
		 */
		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key,
				int fromIndex, int toIndex) {
			int targetIndex = index<0 ? tree.getSyllableCount()-1-index : index;
			return tree.getProperty(key, targetIndex);
		}

	}
}
