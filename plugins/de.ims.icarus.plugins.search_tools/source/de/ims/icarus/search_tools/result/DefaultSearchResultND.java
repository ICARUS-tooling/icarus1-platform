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
package de.ims.icarus.search_tools.result;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.event.ChangeListener;

import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultSearchResultND extends AbstractSearchResult {

	protected Map<String, List<ResultEntry>> entries;
	protected List<ResultEntry> totalEntries;
	protected int hitCount = 0;
	protected final int[] indexBuffer;
	
	protected int[][] groupMatchCounts;
	
	public static final int DEFAULT_START_SIZE = 200;
	
	/**
	 * Keeps track of the total permutation to be applied
	 * on entries stored in {@link ResultNDCache} objects created 
	 * from this result.
	 */
	protected final int[] indexPermutator;

	public DefaultSearchResultND(Search search,
			SearchConstraint[] groupConstraints) {
		this(search, groupConstraints, DEFAULT_START_SIZE);
	}

	public DefaultSearchResultND(Search search,
			SearchConstraint[] groupConstraints, int size) {
		super(search, groupConstraints);
		
		indexPermutator = new int[getDimension()];
		CollectionUtils.fillAscending(indexPermutator);
		
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
			DefaultSearchResult0D subResult = new DefaultSearchResult0D(getSource(), list);
			subResult.setAnnotationBuffer(getAnnotationBuffer());
			
			return subResult;
		} else {
			return new SubResult(this, groupInstances);
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getTotalMatchCount()
	 */
	@Override
	public int getTotalMatchCount() {
		return totalEntries.size();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getMatchCount(int[])
	 */
	@Override
	public int getMatchCount(int... groupIndices) {
		String key = getKey(groupIndices);
		List<ResultEntry> list = entries.get(key);
		
		return list==null ? 0 : list.size(); 
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getEntry(int)
	 */
	@Override
	public Object getEntry(int index) {
		ResultEntry entry = totalEntries.get(index);
		return getTarget().get(entry.getIndex());
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntry(int)
	 */
	@Override
	public ResultEntry getRawEntry(int index) {
		return totalEntries.get(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getEntryList(int[])
	 */
	@Override
	public DataList<? extends Object> getEntryList(int... groupIndices) {
		String key = getKey(groupIndices);
		List<ResultEntry> list = entries.get(key);
		
		return list==null ? null : new EntryList(key);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntryList(int[])
	 */
	@Override
	public List<ResultEntry> getRawEntryList(int... groupIndices) {
		String key = getKey(groupIndices);
		return entries.get(key);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getEntryAt(int, int[])
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
	 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntryAt(int, int[])
	 */
	@Override
	public ResultEntry getRawEntryAt(int index, int... groupIndices) {
		String key = getKey(groupIndices);
		List<ResultEntry> list = entries.get(key);
		return list==null ? null : list.get(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.corpus.AbstractCorpusSearchResult#createCache()
	 */
	@Override
	public GroupCache createCache() {
		return new ResultNDCache();
	}

	protected synchronized void commit(ResultEntry entry, ResultNDCache cache) {
		
		for (int i = 0; i < indexBuffer.length; i++) {
			String value = cache.instanceBuffer[indexPermutator[i]];
			if(value==null || "".equals(value)) { //$NON-NLS-1$
				value = DUMMY_INSTANCE;
			}
			int index = groupInstances[i].substitute(value);
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
		
		hitCount += entry.getHitCount();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#clear()
	 */
	@Override
	public void clear() {
		if(finalized)
			throw new IllegalStateException("Result is already final - clearing not possible"); //$NON-NLS-1$
		
		entries.clear();
		totalEntries.clear();
	}

	@Override
	public int getTotalHitCount() {
		return hitCount;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupMatchCount(int, int)
	 */
	@Override
	public int getGroupMatchCount(int groupId, int index) {
		int[] counts = groupMatchCounts[groupId];
		return (counts==null || counts.length<=index) ? 0 : counts[index];
	}

	@Override
	public boolean canReorder() {
		return getDimension()>2;
	}

	@Override
	public synchronized boolean reorder(int[] permutation) {
		if(permutation==null || permutation.length==0)
			throw new NullPointerException("Invalid permutation"); //$NON-NLS-1$
		
		/*System.out.printf("reorder call: perm=%s dim=%d\n",  //$NON-NLS-1$
				Arrays.toString(permutation), getDimension());*/
		
		int dimension = getDimension();
		if(permutation.length!=dimension)
			throw new IllegalArgumentException();
		
		// we only allow reordering from 3 dimensions on 
		if(dimension<3) {
			return false;
		}
		
		// nothing to do when permutation array is sorted
		if(CollectionUtils.isAscending(permutation))
			return true;
		
		// generate an easy way to reshape key strings
		int[] tmp = new int[dimension];
		CollectionUtils.fillAscending(tmp);
		CollectionUtils.permutate(tmp, permutation);
		String pattern = "", replacement = ""; //$NON-NLS-1$ //$NON-NLS-2$
		for(int i=0; i<dimension; i++) {
			pattern += "(\\d+)"; //$NON-NLS-1$
			replacement += "$"+(tmp[i]+1); //$NON-NLS-1$
			if(i==dimension-1)
				break;
			pattern += "_"; //$NON-NLS-1$
			replacement += "_"; //$NON-NLS-1$
		}
		
		/*System.out.printf("reordering result: pattern='%s' replacement='%s'\n", //$NON-NLS-1$
				pattern, replacement);*/
		
		// Entry buffer, preserves order
		Map<String, List<ResultEntry>> buffer = 
			new LinkedHashMap<String, List<ResultEntry>>(entries.size());
		
		// Relink all entry lists
		String key;
		for(Entry<String, List<ResultEntry>> entry : entries.entrySet()) {
			key = entry.getKey().replaceAll(pattern, replacement);
			buffer.put(key, entry.getValue());
		}
		
		// Copy them back
		entries.clear();
		entries.putAll(buffer);
		
		// Apply permutation to the internal arrays
		CollectionUtils.permutate(indexPermutator, permutation);
		CollectionUtils.permutate(groupConstraints, permutation);
		CollectionUtils.permutate(groupMatchCounts, permutation);
		CollectionUtils.permutate(groupInstances, permutation);
		CollectionUtils.permutate(groupTokens, permutation);
		
		return true;
	}

	protected class ResultNDCache implements GroupCache {

		protected final String[] instanceBuffer = new String[getDimension()];
		
		protected boolean locked = false;

		/**
		 * @see de.ims.icarus.search_tools.standard.GroupCache#cacheGroupInstance(int, java.lang.Object)
		 */
		@Override
		public void cacheGroupInstance(int id, Object value) {
			if(!locked) {
				instanceBuffer[groupIndexMap[id]] = String.valueOf(value);
			}
		}
		
		@Override
		public String toString() {
			return "Cache: "+Arrays.toString(instanceBuffer); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.GroupCache#lock()
		 */
		@Override
		public void lock() {
			locked = true;
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.GroupCache#reset()
		 */
		@Override
		public void reset() {
			locked = false;
			Arrays.fill(instanceBuffer, null);
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.GroupCache#commit(de.ims.icarus.search_tools.result.ResultEntry)
		 */
		@Override
		public void commit(ResultEntry entry) {
			DefaultSearchResultND.this.commit(entry, this);
		}
		
	}

	protected class EntryList extends AbstractList<Object> implements DataList<Object> {

		final String key;

		protected EntryList(String key) {
			this.key = key;
		}

		/**
		 * @see de.ims.icarus.util.data.DataList#getContentType()
		 */
		@Override
		public ContentType getContentType() {
			return getTarget().getContentType();
		}

		/**
		 * @see de.ims.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
		 */
		@Override
		public void addChangeListener(ChangeListener listener) {
			// no-op
		}

		/**
		 * @see de.ims.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
		 */
		@Override
		public void removeChangeListener(ChangeListener listener) {
			// no-op
		}

		/**
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Object get(int index) {
			List<ResultEntry> list = key==null ? totalEntries : entries.get(key);
			
			if(list==null) {
				return null;
			}
			
			ResultEntry entry = list.get(index);
			
			return getTarget().get(entry.getIndex());
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
