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
			if(data!=null) {
				annotatedData = annotator.annotate(searchResult, data, entry);
				cache.put(entry, annotatedData);
			}
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
