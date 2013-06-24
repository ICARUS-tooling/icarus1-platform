/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.result;

import java.util.List;

import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.standard.GroupCache;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.DataList;

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
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getDimension()
		 */
		@Override
		public int getDimension() {
			return dimension;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getSource()
		 */
		@Override
		public Search getSource() {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getTotalMatchCount()
		 */
		@Override
		public int getTotalMatchCount() {
			return 0;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getMatchCount(int[])
		 */
		@Override
		public int getMatchCount(int... groupIndices) {
			return 0;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getGroupMatchCount(int, int)
		 */
		@Override
		public int getGroupMatchCount(int groupId, int index) {
			return 0;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getInstanceCount(int)
		 */
		@Override
		public int getInstanceCount(int groupId) {
			return 0;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getGroupConstraint(int)
		 */
		@Override
		public SearchConstraint getGroupConstraint(int groupId) {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getContentType()
		 */
		@Override
		public ContentType getContentType() {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getGroupLabel(int)
		 */
		@Override
		public Object getGroupLabel(int groupId) {
			return "group"+groupId; //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getInstanceLabel(int, int)
		 */
		@Override
		public Object getInstanceLabel(int groupId, int index) {
			return String.format("group%d_item%d", groupId, index); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getIndexOf(int, java.lang.Object)
		 */
		@Override
		public int getIndexOf(int groupId, Object label) {
			return -1;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntry(int)
		 */
		@Override
		public Object getEntry(int index) {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntry(int)
		 */
		@Override
		public ResultEntry getRawEntry(int index) {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#reorder(int[])
		 */
		@Override
		public boolean reorder(int[] permutation) {
			return false;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntryList(int[])
		 */
		@Override
		public DataList<? extends Object> getEntryList(int... groupIndices) {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntryList(int[])
		 */
		@Override
		public List<ResultEntry> getRawEntryList(int... groupIndices) {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntryAt(int, int[])
		 */
		@Override
		public Object getEntryAt(int index, int... groupIndices) {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntryAt(int, int[])
		 */
		@Override
		public ResultEntry getRawEntryAt(int index, int... groupIndices) {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getSubResult(int[])
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
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#isFinal()
		 */
		@Override
		public boolean isFinal() {
			return true;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#createCache()
		 */
		@Override
		public GroupCache createCache() {
			return GroupCache.dummyCache;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#clear()
		 */
		@Override
		public void clear() {
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#finish()
		 */
		@Override
		public void finish() {
			// no-op
		}
	}
}
