/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools.view.results;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.util.data.Content;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchResultSentenceDataListWrapper 
	extends SearchResultListWrapper<SentenceData> implements SentenceDataList, Content {

	public SearchResultSentenceDataListWrapper(SearchResult searchResult) {
		super(searchResult);
		if(!(searchResult instanceof SentenceDataList))
			throw new IllegalArgumentException("Search result is not a sentence data list!"); //$NON-NLS-1$
	}
	
	public SentenceDataList getWrappedList() {
		return (SentenceDataList) getSearchResult();
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return getWrappedList().supportsType(type);
	}

	@Override
	public SentenceData get(int index) {
		return get(index, DataType.SYSTEM, null);
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
		
		// First make sure the data is available before 
		// trying to fetch an annotated entry
		SentenceData data = getWrappedList().get(index, type, observer);
		if(data==null) {
			return null;
		}
		
		SearchResult searchResult = getSearchResult();
		
		ResultEntry entry = searchResult.getRawEntry(index);
		SentenceData result = (SentenceData) searchResult.getAnnotatedEntry(entry);
		if(result==null) {
			result = (SentenceData) searchResult.getPlainEntry(entry);
		}
		
		return result; 
	}

	/**
	 * @see de.ims.icarus.util.data.Content#getEnclosingType()
	 */
	@Override
	public ContentType getEnclosingType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(SentenceDataList.class);
	}

}
