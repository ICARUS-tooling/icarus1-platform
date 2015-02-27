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
	public abstract Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB);

	public static final AggregationMode minValue = new AggregationMode() {

		@SuppressWarnings("unchecked")
		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			@SuppressWarnings("rawtypes")
			Comparable min = null;

			for(int i=fromIndex; i<=toIndex; i++) {
				if(ignoreUnstressed && !tree.isSyllableStressed(i)) {
					continue;
				}

				double b = tree.getPainteB(i);
				if(b<minB || b>maxB) {
					continue;
				}

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
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			@SuppressWarnings("rawtypes")
			Comparable max = null;

			for(int i=fromIndex; i<=toIndex; i++) {
				if(ignoreUnstressed && !tree.isSyllableStressed(i)) {
					continue;
				}

				double b = tree.getPainteB(i);
				if(b<minB || b>maxB) {
					continue;
				}

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
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			if(ignoreUnstressed && !tree.isSyllableStressed(0)) {
				return null;
			}

			double b = tree.getPainteB(0);
			if(b<minB || b>maxB) {
				return null;
			}

			return tree.getProperty(key, 0);
		}
	};

	public static final AggregationMode lastValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			int index = tree.getSyllableCount()-1;

			if(ignoreUnstressed && !tree.isSyllableStressed(index)) {
				return null;
			}

			double b = tree.getPainteB(index);
			if(b<minB || b>maxB) {
				return null;
			}

			return tree.getProperty(key, index);
		}
	};

	public static final AggregationMode avgIntegerValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			int sum = 0;

			for(int i=0; i<tree.getSyllableCount(); i++) {
				if(ignoreUnstressed && !tree.isSyllableStressed(i)) {
					continue;
				}

				double b = tree.getPainteB(i);
				if(b<minB || b>maxB) {
					continue;
				}

				sum += (int) tree.getProperty(key, i);
			}

			return sum/(toIndex-fromIndex+1);
		}
	};

	public static final AggregationMode avgLongValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			long sum = 0L;

			for(int i=0; i<tree.getSyllableCount(); i++) {
				if(ignoreUnstressed && !tree.isSyllableStressed(i)) {
					continue;
				}

				double b = tree.getPainteB(i);
				if(b<minB || b>maxB) {
					continue;
				}

				sum += (long) tree.getProperty(key, i);
			}

			return sum/(long)(toIndex-fromIndex+1);
		}
	};

	public static final AggregationMode avgFloatValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			float sum = 0F;

			for(int i=0; i<tree.getSyllableCount(); i++) {
				if(ignoreUnstressed && !tree.isSyllableStressed(i)) {
					continue;
				}

				double b = tree.getPainteB(i);
				if(b<minB || b>maxB) {
					continue;
				}

				sum += (float) tree.getProperty(key, i);
			}

			return sum/(float)(toIndex-fromIndex+1);
		}
	};

	public static final AggregationMode avgDoubleValue = new AggregationMode() {

		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key, int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			double sum = 0D;

			for(int i=0; i<tree.getSyllableCount(); i++) {
				if(ignoreUnstressed && !tree.isSyllableStressed(i)) {
					continue;
				}

				double b = tree.getPainteB(i);
				if(b<minB || b>maxB) {
					continue;
				}

				sum += (double) tree.getProperty(key, i);
			}

			return sum/(double)(toIndex-fromIndex+1);
		}
	};

	public static class SingletonAggregation extends AggregationMode {

		private final int index;

		public SingletonAggregation(int index) {
			if(index<0)
				throw new IllegalArgumentException("Index must not be negative: "+index); //$NON-NLS-1$

			this.index = index;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.painte.AggregationMode#getAggregatedValue(de.ims.icarus.plugins.prosody.search.ProsodyTargetTree, java.lang.String, int, int)
		 */
		@Override
		public Object getAggregatedValue(ProsodyTargetTree tree, String key,
				int fromIndex, int toIndex, boolean ignoreUnstressed, double minB, double maxB) {
			int targetIndex = index<0 ? tree.getSyllableCount()-1-index : index;

			if(ignoreUnstressed && !tree.isSyllableStressed(targetIndex)) {
				return null;
			}

			double b = tree.getPainteB(targetIndex);
			if(b<minB || b>maxB) {
				return null;
			}

			return tree.getProperty(key, targetIndex);
		}

	}
}
