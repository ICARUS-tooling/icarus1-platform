/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.result;

import net.ikarus_systems.icarus.util.annotation.AnnotatedData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ResultAnnotator {

	AnnotatedData annotate(SearchResult searchResult, Object data, ResultEntry entry);
}
