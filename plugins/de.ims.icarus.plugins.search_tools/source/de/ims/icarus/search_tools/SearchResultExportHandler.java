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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools;

import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.util.ToolException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchResultExportHandler {

	/**
	 * Initiate export of the given {@code SearchResult}. It is up to
	 * the export handler to ask the user for target location and the
	 * details on how to format/organize the result. Note that this
	 * method is called on the <i>event-dispatch-thread</i>.
	 */
	void exportResult(SearchResult searchResult) throws ToolException;
}
