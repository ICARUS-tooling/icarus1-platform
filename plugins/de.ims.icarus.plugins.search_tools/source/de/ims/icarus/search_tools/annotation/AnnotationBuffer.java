/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.annotation;

import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.AnnotationContainer;
import de.ims.icarus.util.cache.LRUCache;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationBuffer implements AnnotationContainer {
	
	private final SearchResult searchResult;
	private final ResultAnnotator annotator;
	private final LRUCache<ResultEntry, AnnotatedData> cache;

	public AnnotationBuffer(SearchResult searchResult, ResultAnnotator annotator, int cacheSize) {
		if(searchResult==null)
			throw new IllegalArgumentException("Invalid search result"); //$NON-NLS-1$
		if(annotator==null)
			throw new IllegalArgumentException("Invalid result annotator"); //$NON-NLS-1$
		
		this.searchResult = searchResult;
		this.annotator = annotator;
		cache = new LRUCache<>(cacheSize);
	}

	public void setCacheSize(int cacheSize) {
		cache.setMaxSize(cacheSize);
	}
	
	public AnnotatedData getAnnotatedData(ResultEntry entry) {
		AnnotatedData annotatedData = cache.get(entry);
		
		if(annotatedData==null) {
			Object data = searchResult.getPlainEntry(entry);
			annotatedData = annotator.annotate(searchResult, data, entry);
			cache.put(entry, annotatedData);
		}
		
		return annotatedData;
	}
	
	public void clear() {
		cache.clear();
	}

	/**
	 * @see de.ims.icarus.util.annotation.AnnotationContainer#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		return annotator.getAnnotationType();
	}
}
