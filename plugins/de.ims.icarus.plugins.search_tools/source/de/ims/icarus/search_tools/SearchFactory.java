/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools;

import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchFactory {

	/**
	 * Constructs a new {@code Search} object that can be scheduled
	 * to run the <i>search-constraints</i> encoded within the
	 * {@code query} argument. Implementations are required to support
	 * all the parameters defined in {@code SearchParameters} when passed
	 * as part of the {@code options} argument.
	 * 
	 * @throws UnsupportedFormatException in case the {@code query} contains
	 * illegal {@code SearchConstraint} instances or is of an unsupported
	 * structure (e.g. a factory for tree-structures would reject a full
	 * grown graph object)
	 */
	Search createSearch(SearchQuery query, Object target, Options options) throws UnsupportedFormatException;
	
	ConstraintContext getConstraintContext();
	
	/**
	 * Creates an empty query usable for setting up
	 * a search.
	 */
	SearchQuery createQuery();
	
	Editor<Options> createParameterEditor();
}
