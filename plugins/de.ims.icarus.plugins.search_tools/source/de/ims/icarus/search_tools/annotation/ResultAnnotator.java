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
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ResultAnnotator {

	AnnotatedData annotate(SearchResult searchResult, Object data, ResultEntry entry);
	
	ContentType getAnnotationType();
}
