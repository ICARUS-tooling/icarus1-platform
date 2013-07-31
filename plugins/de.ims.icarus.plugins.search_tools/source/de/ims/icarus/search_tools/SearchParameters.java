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
package de.ims.icarus.search_tools;

import de.ims.icarus.util.Orientation;

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
