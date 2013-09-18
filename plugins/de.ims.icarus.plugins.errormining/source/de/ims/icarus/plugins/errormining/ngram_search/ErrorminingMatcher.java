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
package de.ims.icarus.plugins.errormining.ngram_search;

import java.util.List;

import de.ims.icarus.search_tools.result.EntryBuilder;
import de.ims.icarus.search_tools.standard.GroupCache;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */

public interface ErrorminingMatcher {

	/**
	 * @param helferList
	 */
	void setSentenceList(List<MappedNGramResult> helferList);

	/**
	 * @param cache
	 */
	void setCache(GroupCache cache);

	/**
	 * @param entryBuilder
	 */
	void setEntryBuilder(EntryBuilder entryBuilder);

	/**
	 * @param index
	 */
	void matches(int index);

}
