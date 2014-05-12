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
package de.ims.icarus.language.model.api.seg;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.CorpusException;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.iql.Query;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Segment {

	/**
	 * Returns the {@code Corpus} that is backing this segment.
	 * @return
	 */
	Corpus getCorpus();

	/**
	 * Returns the {@code scope} that was used to limit the contexts
	 * and layers involved in this segment or {@code null} if no vertical
	 * filtering was performed.
	 */
	Scope getScope();

	/**
	 * Returns the {@code Query} that was used to narrow down the
	 * elements in this segment or {@code null} if no horizontal
	 * filtering was performed.
	 */
	Query getQuery();

	// Content access

	/**
	 * Maps shared {@code MarkableLayer}s to their respective {@code Container}
	 * for this segment.
	 */
	Container getContainer(MarkableLayer layer);

	/**
	 * If this segment involved vertical filtering than the container assigned to be the
	 * <i>primary container</i> of this segment's scope will be returned. Otherwise this
	 * method will return the container mapped to the <i>base layer</i> of the backing
	 * corpus.
	 */
	Container getBaseContainer();

	// Page support

	int getPageSize();

	/**
	 * Returns the number of available pages for this segment, or {@code 0}
	 * in case the segment does not support paging.
	 *
	 * @return
	 */
	int getPageCount();

	/**
	 * Returns the index of the current page
	 * @return
	 */
	int getPageIndex();

	/**
	 * Synchronously attempts to load the specified page of data
	 * into this segment.
	 *
	 * @param index the index of the page to load
	 * @return {@code true} iff loading the requested page succeeded without errors
	 * and the content of this segment changed as a result.
	 * @throws CorpusException if there was an IO error or other problem encountered
	 * 			while loading data
	 * @throws UnsupportedOperationException if this segment does not support paging
	 * 			(this is the case when {@code #getPageCount()} returns {@code 0})
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getPageCount()</tt>)
	 */
	boolean loadPage(int index) throws CorpusException;
}
