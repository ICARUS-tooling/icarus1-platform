/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import net.ikarus_systems.icarus.util.Orientation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchParameters {

	public static final String SEARCH_MODE = "searchMode"; //$NON-NLS-1$

	public static final String SEARCH_ORIENTATION = "searchOrientation"; //$NON-NLS-1$

	public static final String SEARCH_CASESENSITIVE = "searchCaseSensitive"; //$NON-NLS-1$

	public static final String OPTIMIZE_SEARCH = "optimizeSearch"; //$NON-NLS-1$

	public static final String SEARCH_RESULT_LIMIT = "searchResultLimit"; //$NON-NLS-1$
	
	public static final SearchMode DEFAULT_SEARCH_MODE = SearchMode.MATCHES;
	public static final Orientation DEFAULT_SEARCH_ORIENTATION = Orientation.LEFT_TO_RIGHT;
	public static final boolean DEFAULT_SEARCH_CASESENSITIVE = true;
	public static final boolean DEFAULT_OPTIMIZE_SEARCH = false;
	public static final int DEFAULT_SEARCH_RESULT_LIMIT = 0;
}
