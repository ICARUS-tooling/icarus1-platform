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

import java.util.List;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.tree.AbstractTreeSearch;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractCorpusSearch extends AbstractTreeSearch {
	
	protected AvailabilityObserver observer;
	protected final DataType dataType;
	
	protected AbstractCorpusSearch(SearchFactory factory, SearchQuery query, 
			Options parameters,	Object target) {
		super(factory, query, parameters, target);
		
		observer = createObserver();
		dataType = getDefaultDataType();
	}
	
	protected DataType getDefaultDataType() {
		return DataType.SYSTEM;
	}
	
	protected AvailabilityObserver createObserver() {
		return new CorpusObserver();
	}
	
	@Override
	protected SearchResult createResult(List<SearchConstraint> groupConstraints) {
		
		/* Only distinguish between 0D and ND where N>0 since 0D
		 * can be implemented efficiently by using a simple list storage.
		 */
		if(groupConstraints.isEmpty()) {
			return new CorpusSearchResult0D(this);
		} else {
			return new CorpusSearchResultND(this, 
					groupConstraints.toArray(new SearchConstraint[0]));
		}
		
	}

	@Override
	protected abstract SentenceDataList createSource(Object target);

	@Override
	protected Object getTargetItem(int index) {
		return ((SentenceDataList)source).get(index, dataType, observer);
	}
	
	protected class CorpusObserver implements AvailabilityObserver {

		/**
		 * @see de.ims.icarus.language.AvailabilityObserver#dataAvailable(int, de.ims.icarus.language.SentenceData)
		 */
		@Override
		public void dataAvailable(int index, SentenceData item) {
			offerItem(index, item);
		}
	}
}
