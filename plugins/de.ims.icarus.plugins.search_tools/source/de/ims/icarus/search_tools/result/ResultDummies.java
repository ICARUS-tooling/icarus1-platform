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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.result;

import java.util.List;

import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ResultDummies {

	private ResultDummies() {
		// no-op
	}
	
	public static final SearchResult dummyResult0D = new ResultDummy(0);
	public static final SearchResult dummyResult1D = new ResultDummy(1);
	public static final SearchResult dummyResult2D = new ResultDummy(2);
	public static final SearchResult dummyResult3D = new ResultDummy(3);

	public static class ResultDummy implements SearchResult {
		private final int dimension;
		
		ResultDummy(int dimension) {
			this.dimension = dimension;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getDimension()
		 */
		@Override
		public int getDimension() {
			return dimension;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getSource()
		 */
		@Override
		public Search getSource() {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getTotalMatchCount()
		 */
		@Override
		public int getTotalMatchCount() {
			return 0;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getMatchCount(int[])
		 */
		@Override
		public int getMatchCount(int... groupIndices) {
			return 0;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupMatchCount(int, int)
		 */
		@Override
		public int getGroupMatchCount(int groupId, int index) {
			return 0;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getInstanceCount(int)
		 */
		@Override
		public int getInstanceCount(int groupId) {
			return 0;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupConstraint(int)
		 */
		@Override
		public SearchConstraint getGroupConstraint(int groupId) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getContentType()
		 */
		@Override
		public ContentType getContentType() {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupLabel(int)
		 */
		@Override
		public Object getGroupLabel(int groupId) {
			return "group"+groupId; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getInstanceLabel(int, int)
		 */
		@Override
		public Object getInstanceLabel(int groupId, int index) {
			return String.format("group%d_item%d", groupId, index); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getIndexOf(int, java.lang.Object)
		 */
		@Override
		public int getIndexOf(int groupId, Object label) {
			return -1;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getEntry(int)
		 */
		@Override
		public Object getEntry(int index) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntry(int)
		 */
		@Override
		public ResultEntry getRawEntry(int index) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#reorder(int[])
		 */
		@Override
		public boolean reorder(int[] permutation) {
			return false;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getEntryList(int[])
		 */
		@Override
		public DataList<? extends Object> getEntryList(int... groupIndices) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntryList(int[])
		 */
		@Override
		public List<ResultEntry> getRawEntryList(int... groupIndices) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getEntryAt(int, int[])
		 */
		@Override
		public Object getEntryAt(int index, int... groupIndices) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntryAt(int, int[])
		 */
		@Override
		public ResultEntry getRawEntryAt(int index, int... groupIndices) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getSubResult(int[])
		 */
		@Override
		public SearchResult getSubResult(int... groupInstances) {
			if(groupInstances==null) {
				return null;
			}
			
			switch (dimension-groupInstances.length) {
			case 0:
				return dummyResult0D;
			case 1:
				return dummyResult1D;
			case 2:
				return dummyResult2D;
			case 3:
				return dummyResult3D;

			default:
				return null;
			}
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#isFinal()
		 */
		@Override
		public boolean isFinal() {
			return true;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#createCache()
		 */
		@Override
		public GroupCache createCache() {
			return GroupCache.dummyCache;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#clear()
		 */
		@Override
		public void clear() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#finish()
		 */
		@Override
		public void finish() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getPlainEntry(de.ims.icarus.search_tools.result.ResultEntry)
		 */
		@Override
		public Object getPlainEntry(ResultEntry entry) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getAnnotatedEntry(de.ims.icarus.search_tools.result.ResultEntry)
		 */
		@Override
		public AnnotatedData getAnnotatedEntry(ResultEntry entry) {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.annotation.AnnotationContainer#getAnnotationType()
		 */
		@Override
		public ContentType getAnnotationType() {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getTotalHitCount()
		 */
		@Override
		public int getTotalHitCount() {
			return 0;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#setProperty(java.lang.String, java.lang.Object)
		 */
		@Override
		public void setProperty(String key, Object value) {
			// no-op
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#getProperty(java.lang.String)
		 */
		@Override
		public Object getProperty(String key) {
			return null;
		}

		/**
		 * @see de.ims.icarus.search_tools.result.SearchResult#canReorder()
		 */
		@Override
		public boolean canReorder() {
			return false;
		}
	}
}
