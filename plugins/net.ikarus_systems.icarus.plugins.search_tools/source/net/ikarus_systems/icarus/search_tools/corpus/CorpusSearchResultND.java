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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.result.ResultEntry;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusSearchResultND extends AbstractCorpusSearchResult {

	protected Map<String, List<ResultEntry>> entries;
	protected List<ResultEntry> totalEntries;
	protected final int[] indexBuffer;
	
	protected int[][] groupMatchCounts;
	
	public static final int DEFAULT_START_SIZE = 200;
	
	/**
	 * Keeps track of the total permutation to be applied
	 * on entries stored in {@link ResultNDCache} objects created 
	 * from this result.
	 */
	protected final int[] indexPermutator;

	public CorpusSearchResultND(Search search,
			SearchConstraint[] groupConstraints) {
		this(search, groupConstraints, DEFAULT_START_SIZE);
	}

	public CorpusSearchResultND(Search search,
			SearchConstraint[] groupConstraints, int size) {
		super(search, groupConstraints);
		
		indexPermutator = new int[getDimension()];
		indexBuffer = new int[getDimension()];
		groupMatchCounts = new int[getDimension()][];
		
		entries = new HashMap<>(size);
		totalEntries = new ArrayList<>(size);
	}

	StringBuilder keyBuilder = new StringBuilder(10);

	protected String getKey(int... indices) {
		if(indices.length!=indexBuffer.length)
			throw new IllegalArgumentException("Illegal indices count: expected "+indexBuffer.length+" - got "+indices.length); //$NON-NLS-1$ //$NON-NLS-2$
		
		keyBuilder.setLength(0);
		int last = indices.length - 1;
		for (int i = 0; i < last; i++) {
			keyBuilder.append(indices[i]).append('_');
		}
		keyBuilder.append(indices[last]);

		return keyBuilder.toString();
	}

	@Override
	public SearchResult getSubResult(int... groupInstances) {
		int dif = getDimension()-groupInstances.length;
		if(dif<0)
			throw new IllegalArgumentException("Number of instances for sub-result exceeds current dimension: "+groupInstances.length); //$NON-NLS-1$
		
		if(dif==0) {
			List<ResultEntry> list = getRawEntryList(groupInstances);
			return new CorpusSearchResult0D(getSource(), list);
		} else {
			return new SubResult(this, groupInstances);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getTotalMatchCount()
	 */
	@Override
	public int getTotalMatchCount() {
		return totalEntries.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getMatchCount(int[])
	 */
	@Override
	public int getMatchCount(int... groupIndices) {
		String key = getKey(groupIndices);
		List<ResultEntry> list = entries.get(key);
		
		return list==null ? 0 : list.size(); 
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntry(int)
	 */
	@Override
	public Object getEntry(int index) {
		ResultEntry entry = totalEntries.get(index);
		return getTarget().get(entry.getIndex());
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntry(int)
	 */
	@Override
	public ResultEntry getRawEntry(int index) {
		return totalEntries.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntryList(int[])
	 */
	@Override
	public DataList<? extends Object> getEntryList(int... groupIndices) {
		String key = getKey(groupIndices);
		List<ResultEntry> list = entries.get(key);
		
		return list==null ? null : new EntryList(key); 
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntryList(int[])
	 */
	@Override
	public List<ResultEntry> getRawEntryList(int... groupIndices) {
		String key = getKey(groupIndices);
		return entries.get(key);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getEntryAt(int, int[])
	 */
	@Override
	public Object getEntryAt(int index, int... groupIndices) {
		String key = getKey(groupIndices);
		List<ResultEntry> list = entries.get(key);
		if(list==null) {
			return null;
		}
		ResultEntry entry = list.get(index);
		return getTarget().get(entry.getIndex());
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getRawEntryAt(int, int[])
	 */
	@Override
	public ResultEntry getRawEntryAt(int index, int... groupIndices) {
		String key = getKey(groupIndices);
		List<ResultEntry> list = entries.get(key);
		return list==null ? null : list.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearchResult#createCache()
	 */
	@Override
	public GroupCache createCache() {
		return new ResultNDCache();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearchResult#commit(net.ikarus_systems.icarus.search_tools.result.ResultEntry, net.ikarus_systems.icarus.search_tools.corpus.GroupCache)
	 */
	@Override
	public synchronized void commit(ResultEntry entry, GroupCache cache) {
		ResultNDCache c = (ResultNDCache) cache;
		
		for (int i = 0; i < indexBuffer.length; i++) {
			int index = groupInstances[i].substitute(c.instanceBuffer[indexPermutator[i]]);;
			indexBuffer[i] = index; 
			
			int[] counts = groupMatchCounts[i];
			if(counts==null) {
				counts = new int[Math.max(index*2, 100)];
				groupMatchCounts[i] = counts;
			} else if(counts.length<=index) {
				counts = Arrays.copyOf(counts, index*2);
				groupMatchCounts[i] = counts;
			}
			counts[index]++;
		}

		// Generate key and ensure valid result list
		String key = getKey(indexBuffer);
		List<ResultEntry> list = entries.get(key);
		if (list == null) {
			list = new ArrayList<>(30);
			entries.put(key, list);
		}

		// finally add the currently processed entry to the result list
		list.add(entry);
		totalEntries.add(entry);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getGroupMatchCount(int, int)
	 */
	@Override
	public int getGroupMatchCount(int groupId, int index) {
		int[] counts = groupMatchCounts[groupId];
		return (counts==null || counts.length<=index) ? 0 : counts[index];
	}

	protected class ResultNDCache implements GroupCache {

		protected final String[] instanceBuffer = new String[getDimension()];
		
		protected boolean locked = false;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.corpus.GroupCache#cacheGroupInstance(int, java.lang.Object)
		 */
		@Override
		public void cacheGroupInstance(int id, Object value) {
			if(!locked) {
				instanceBuffer[id] = String.valueOf(value);
			}
		}
		
		@Override
		public String toString() {
			return "Cache: "+Arrays.toString(instanceBuffer); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.corpus.GroupCache#lock()
		 */
		@Override
		public void lock() {
			locked = true;
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.corpus.GroupCache#reset()
		 */
		@Override
		public void reset() {
			locked = false;
			Arrays.fill(instanceBuffer, null);
		}
		
	}

	protected class EntryList extends AbstractList<SentenceData> implements SentenceDataList {

		final String key;

		protected EntryList(String key) {
			this.key = key;
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
			List<ResultEntry> list = key==null ? totalEntries : entries.get(key);
			
			if(list==null) {
				return null;
			}
			
			ResultEntry entry = list.get(index);
			
			return getTarget().get(entry.getIndex(), type, observer);
		}

		/**
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public SentenceData get(int index) {
			return get(index, DataType.SYSTEM, null);
		}

		/**
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			List<ResultEntry> list = key==null ? totalEntries : entries.get(key);
			return list==null ? 0 : list.size();
		}
		
	}
}
