/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.results;

import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.search_tools.result.ResultEntry;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.util.annotation.AnnotatedData;
import net.ikarus_systems.icarus.util.annotation.AnnotationContainer;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchResultListWrapper implements DataList<Object>, AnnotationContainer {

	private final SearchResult searchResult;
	
	public SearchResultListWrapper(SearchResult searchResult) {
		if(searchResult==null)
			throw new IllegalArgumentException("Invalid search result"); //$NON-NLS-1$
		
		this.searchResult = searchResult;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return searchResult.getTotalMatchCount();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#get(int)
	 */
	@Override
	public Object get(int index) {
		ResultEntry entry = searchResult.getRawEntry(index);
		AnnotatedData annotatedData = searchResult.getAnnotatedEntry(entry);
		
		return annotatedData==null ? searchResult.getPlainEntry(entry) : annotatedData;
	}
	
	public Object getPlain(int index) {
		return searchResult.getEntry(index);
	}
	
	public ResultEntry getRaw(int index) {
		return searchResult.getRawEntry(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return searchResult.getContentType();
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
	 * @see net.ikarus_systems.icarus.util.annotation.AnnotationContainer#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		return searchResult.getAnnotationType();
	}

}
