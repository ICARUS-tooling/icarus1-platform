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

import static de.ims.icarus.search_tools.util.SearchUtils.checkResultEntry;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;

import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.io.SearchWriter;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultSearchResult0D extends AbstractSearchResult {

	protected List<ResultEntry> entries;
	protected int hitCount = 0;
	protected DataList<?> wrapper;

	public static final int DEFAULT_START_SIZE = 1000;


	public DefaultSearchResult0D(Search search) {
		this(search, DEFAULT_START_SIZE);
	}

	public DefaultSearchResult0D(Search search, int size) {
		super(search, null);

		entries = new ArrayList<>(size);
	}

	public DefaultSearchResult0D(Search search, List<ResultEntry> entries) {
		super(search, null);

		if(entries==null)
			throw new NullPointerException("Invalid entry list"); //$NON-NLS-1$

		this.entries = new ArrayList<>(entries);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getEntry(int)
	 */
	@Override
	public Object getEntry(int index) {
		ResultEntry entry = entries.get(index);
		return getTarget().get(entry.getIndex());
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getEntryAt(int, int[])
	 */
	@Override
	public Object getEntryAt(int index, int... groupIndices) {
		return getEntry(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getEntryList(int[])
	 */
	@Override
	public DataList<? extends Object> getEntryList(int... groupIndices) {
		if(wrapper==null) {
			wrapper = new EntryList();
		}

		return wrapper;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntry(int)
	 */
	@Override
	public ResultEntry getRawEntry(int index) {
		return entries.get(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntryAt(int, int[])
	 */
	@Override
	public ResultEntry getRawEntryAt(int index, int... groupIndices) {
		return getRawEntry(index);
	}


	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getTotalMatchCount()
	 */
	@Override
	public int getTotalMatchCount() {
		return entries.size();
	}


	/**
	 * @see de.ims.icarus.search_tools.corpus.AbstractCorpusSearchResult#createCache()
	 */
	@Override
	public GroupCache createCache() {
		return new Result0DCache();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getMatchCount(int[])
	 */
	@Override
	public int getMatchCount(int... groupIndices) {
		return entries.size();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntryList(int[])
	 */
	@Override
	public List<ResultEntry> getRawEntryList(int... groupIndices) {
		return entries;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupMatchCount(int, int)
	 */
	@Override
	public int getGroupMatchCount(int groupId, int index) {
		return 0;
	}

	protected synchronized void commit(ResultEntry entry) {
		addEntry(entry);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#clear()
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

	/**
	 * @see de.ims.icarus.search_tools.result.AbstractSearchResult#writeEntries(de.ims.icarus.search_tools.io.SearchWriter)
	 */
	@Override
	public void writeEntries(SearchWriter writer) throws XMLStreamException {
		for(ResultEntry entry : entries) {
			writer.writeEntry(entry);
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.result.AbstractSearchResult#addEntry(de.ims.icarus.search_tools.result.ResultEntry, int[])
	 */
	@Override
	public void addEntry(ResultEntry entry, int... groupIndices) {
		checkResultEntry(entry);

		entries.add(entry);
		hitCount += entry.getHitCount();
	}

	protected class Result0DCache implements GroupCache {

		/**
		 * @see de.ims.icarus.search_tools.standard.GroupCache#cacheGroupInstance(int, java.lang.Object)
		 */
		@Override
		public void cacheGroupInstance(int id, Object value, boolean replace) {
			// no-op
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.GroupCache#lock()
		 */
		@Override
		public void lock() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.GroupCache#reset()
		 */
		@Override
		public void reset() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.GroupCache#commit(de.ims.icarus.search_tools.result.ResultEntry)
		 */
		@Override
		public void commit(ResultEntry entry) {
			DefaultSearchResult0D.this.commit(entry);
		}

	}

	protected class EntryList extends AbstractList<Object> implements DataList<Object> {

		@Override
		public Object get(int index) {
			ResultEntry entry = entries.get(index);
			return getTarget().get(entry.getIndex());
		}

		@Override
		public int size() {
			return getTotalMatchCount();
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
	}
}
