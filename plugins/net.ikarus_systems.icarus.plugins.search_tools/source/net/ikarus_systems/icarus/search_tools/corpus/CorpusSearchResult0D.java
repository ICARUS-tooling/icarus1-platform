/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.corpus;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.result.ResultEntry;
import net.ikarus_systems.icarus.search_tools.standard.GroupCache;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusSearchResult0D extends AbstractCorpusSearchResult {
	
	protected List<ResultEntry> entries;
	protected int hitCount = 0;
	protected EntryList wrapper;
	
	public static final int DEFAULT_START_SIZE = 1000;

	public CorpusSearchResult0D(Search search) {
		this(search, DEFAULT_START_SIZE);
	}

	public CorpusSearchResult0D(Search search, int size) {
		super(search, null);
		
		entries = new ArrayList<>(size);
	}

	public CorpusSearchResult0D(Search search, List<ResultEntry> entries) {
		super(search, null);
		
		if(entries==null)
			throw new IllegalArgumentException("Invalid entry list"); //$NON-NLS-1$
		
		this.entries = new ArrayList<>(entries);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntry(int)
	 */
	@Override
	public SentenceData getEntry(int index) {
		ResultEntry entry = entries.get(index);
		return getTarget().get(entry.getIndex());
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntryAt(int, int[])
	 */
	@Override
	public SentenceData getEntryAt(int index, int... groupIndices) {
		return getEntry(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntryList(int[])
	 */
	@Override
	public DataList<? extends Object> getEntryList(int... groupIndices) {
		if(wrapper==null) {
			wrapper = new EntryList();
		}
		
		return wrapper;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntry(int)
	 */
	@Override
	public ResultEntry getRawEntry(int index) {
		return entries.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntryAt(int, int[])
	 */
	@Override
	public ResultEntry getRawEntryAt(int index, int... groupIndices) {
		return getRawEntry(index);
	}


	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getTotalMatchCount()
	 */
	@Override
	public int getTotalMatchCount() {
		return entries.size();
	}


	/**
	 * @see net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearchResult#createCache()
	 */
	@Override
	public GroupCache createCache() {
		return new Result0DCache();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getMatchCount(int[])
	 */
	@Override
	public int getMatchCount(int... groupIndices) {
		return entries.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntryList(int[])
	 */
	@Override
	public List<ResultEntry> getRawEntryList(int... groupIndices) {
		return entries;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getGroupMatchCount(int, int)
	 */
	@Override
	public int getGroupMatchCount(int groupId, int index) {
		return 0;
	}
	
	private synchronized void commit(ResultEntry entry) {
		entries.add(entry);
		hitCount += entry.getHitCount();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#clear()
	 */
	@Override
	public void clear() {
		if(finalized)
			throw new IllegalStateException("Result is already final - clearing not possible"); //$NON-NLS-1$
		
		entries.clear();
	}

	@Override
	public int getTotalHitCount() {
		return hitCount;
	}
	
	protected class Result0DCache implements GroupCache {

		/**
		 * @see net.ikarus_systems.icarus.search_tools.standard.GroupCache#cacheGroupInstance(int, java.lang.Object)
		 */
		@Override
		public void cacheGroupInstance(int id, Object value) {
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.standard.GroupCache#lock()
		 */
		@Override
		public void lock() {
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.standard.GroupCache#reset()
		 */
		@Override
		public void reset() {
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.standard.GroupCache#commit(net.ikarus_systems.icarus.search_tools.result.ResultEntry)
		 */
		@Override
		public void commit(ResultEntry entry) {
			CorpusSearchResult0D.this.commit(entry);
		}
		
	}

	protected class EntryList extends AbstractList<SentenceData> implements SentenceDataList {

		@Override
		public SentenceData get(int index) {
			return get(index, DataType.SYSTEM, null);
		}

		@Override
		public int size() {
			return getTotalMatchCount();
		}

		/**
		 * @see net.ikarus_systems.icarus.util.data.DataList#getContentType()
		 */
		@Override
		public ContentType getContentType() {
			return getTarget().getContentType();
		}

		/**
		 * @see net.ikarus_systems.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
		 */
		@Override
		public void addChangeListener(ChangeListener listener) {
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
		 */
		@Override
		public void removeChangeListener(ChangeListener listener) {
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataList#supportsType(net.ikarus_systems.icarus.language.DataType)
		 */
		@Override
		public boolean supportsType(DataType type) {
			return type==DataType.SYSTEM;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType)
		 */
		@Override
		public SentenceData get(int index, DataType type) {
			return get(index, type, null);
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType, net.ikarus_systems.icarus.language.AvailabilityObserver)
		 */
		@Override
		public SentenceData get(int index, DataType type,
				AvailabilityObserver observer) {
			ResultEntry entry = entries.get(index);
			return getTarget().get(entry.getIndex(), type, observer);
		}
	}
}
