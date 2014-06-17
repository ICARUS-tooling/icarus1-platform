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
package de.ims.icarus.model.api.driver;

import java.util.Arrays;
import java.util.Comparator;

import de.ims.icarus.model.standard.index.SingletonIndexSet;
import de.ims.icarus.model.standard.index.SpanIndexSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IndexUtils {

	public static final IndexSet[] EMPTY = new IndexSet[0];

	public static boolean isContinuous(IndexSet indices) {
		return indices.lastIndex()-indices.firstIndex()==indices.size()-1;
	}

	public static boolean isContinuous(IndexSet[] indices) {
		for(int i=1; i<indices.length; i++) {
			if(!isContinuous(indices[i]) || indices[i].firstIndex()!=indices[i-1].lastIndex()+1) {
				return false;
			}
		}

		return true;
	}

	public static IndexSet[] wrap(long index) {
		return index==-1L ? EMPTY : new IndexSet[]{new SingletonIndexSet(index)};
	}

	public static long unwrap(IndexSet[] indices) {
		return indices.length==1 && indices[0].size()==1 ? firstIndex(indices) : -1L;
	}

	public static final Comparator<IndexSet> INDEX_SET_SORTER = new Comparator<IndexSet>() {

		@Override
		public int compare(IndexSet o1, IndexSet o2) {
			long result = o1.firstIndex()-o2.firstIndex();
			if(result==0L) {
				result = o1.lastIndex()-o2.lastIndex();
			}
			if(result>Integer.MAX_VALUE || result<=Integer.MIN_VALUE) {
				result >>= 32;
			}
			return (int) result;
		}
	};

	public static void sort(IndexSet[] indices) {
		Arrays.sort(indices, INDEX_SET_SORTER);
	}

	public static long firstIndex(IndexSet[] indices) {
		return indices[0].firstIndex();
	}

	public static long lastIndex(IndexSet[] indices) {
		return indices[indices.length-1].lastIndex();
	}

	//TODO add utility methods for merging etc of index sets

	public static IndexSet[] merge(IndexSet...indices) {

	}

	public static IndexSet[] merge(long indexFrom, long indexTo, IndexSet...indices) {

	}

	public static IndexSet[] merge(IndexSet[]...indices) {

	}

	public static IndexSet intersect(IndexSet set1, IndexSet set2) {

	}

	public static IndexSet[] intersect(long indexFrom, long indexTo, IndexSet...indices) {

	}

	public static IndexSet[] intersect(IndexSet...indices) {

	}

	public static IndexSet[] intersect(IndexSet[]...indices) {
	}

	public static IndexSet[] intersect(long from1, long to1, long from2, long to2) {
		long from = Math.max(from1, from2);
		long to = Math.min(from1, to2);

		if(from>to) {
			return EMPTY;
		}

		long count = to-from+1;
		int chunks = (int) Math.ceil(count/(double)Integer.MAX_VALUE);

		IndexSet[] result = new IndexSet[chunks];

		for(int i=0; i<count; i++) {
			long begin = from;
			long end = Math.min(begin+Integer.MAX_VALUE, to);

			result[i] = new SpanIndexSet(begin, end);

			from = end+1;
		}

		return result;
	}

	public static boolean forEachSpan(IndexSet[] indices, IndexProcedure procedure) throws InterruptedException {
		if(isContinuous(indices)) {
			return procedure.process(firstIndex(indices), lastIndex(indices));
		} else {

			boolean result = false;

			for(IndexSet set : indices) {
				boolean b= forEachSpan(set, procedure);
				result |= b;

				if(!b) {
					break;
				}
			}

			return result;
		}
	}

	public static boolean forEachSpan(IndexSet indices, IndexProcedure procedure) throws InterruptedException {
		if(isContinuous(indices)) {
			return procedure.process(indices.firstIndex(), indices.lastIndex());
		} else {
			long from = indices.firstIndex();
			long last = from;

			boolean result = false;

			for(int i=1; i<indices.size(); i++) {
				long val = indices.indexAt(i);

				if(val>last+1) {
					boolean b = procedure.process(from, last);
					result |= b;

					if(!b) {
						break;
					}

					from = val;
				}

				last = val;
			}

			result |= procedure.process(from, last);

			return result;
		}
	}

	public interface IndexProcedure {

		boolean process(long from, long to) throws InterruptedException;
	}
}
