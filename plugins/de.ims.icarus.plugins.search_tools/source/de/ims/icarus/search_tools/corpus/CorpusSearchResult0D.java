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
package de.ims.icarus.search_tools.corpus;

import java.util.AbstractList;
import java.util.List;

import javax.swing.event.ChangeListener;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.result.DefaultSearchResult0D;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusSearchResult0D extends DefaultSearchResult0D implements SentenceDataList {


	public CorpusSearchResult0D(Search search, int size) {
		super(search, size);
	}

	public CorpusSearchResult0D(Search search, List<ResultEntry> entries) {
		super(search, entries);
	}

	public CorpusSearchResult0D(Search search) {
		super(search);
	}

	@Override
	public int size() {
		return getTotalMatchCount();
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		// no-op
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		// no-op
	}

	@Override
	public boolean supportsType(DataType type) {
		return getTarget().supportsType(type);
	}

	@Override
	public SentenceData get(int index) {
		return get(index, DataType.SYSTEM, null);
	}

	@Override
	public SentenceData get(int index, DataType type) {
		return get(index, type, null);
	}

	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		ResultEntry entry = getRawEntry(index);
		return getTarget().get(entry.getIndex(), type, observer);
	}

	@Override
	public SentenceDataList getTarget() {
		return (SentenceDataList) super.getTarget();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getEntryList(int[])
	 */
	@Override
	public SentenceDataList getEntryList(int... groupIndices) {
		if(wrapper==null) {
			wrapper = new EntryList();
		}
		
		return (SentenceDataList) wrapper;
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
		 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
		 */
		@Override
		public boolean supportsType(DataType type) {
			return type==DataType.SYSTEM;
		}

		/**
		 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
		 */
		@Override
		public SentenceData get(int index, DataType type) {
			return get(index, type, null);
		}

		/**
		 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
		 */
		@Override
		public SentenceData get(int index, DataType type,
				AvailabilityObserver observer) {
			ResultEntry entry = entries.get(index);
			return getTarget().get(entry.getIndex(), type, observer);
		}
	}
}
