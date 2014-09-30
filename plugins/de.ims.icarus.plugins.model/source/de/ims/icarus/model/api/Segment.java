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
package de.ims.icarus.model.api;

import java.util.Set;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.iql.Query;

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

	// Destruction support

	/**
	 * Attempts to acquire shared ownership of this segment by the given {@code owner}.
	 * If the given owner already holds shared ownership if this segment, the method
	 * simply returns.
	 *
	 * @param owner
	 * @throws NullPointerException if the {@code owner} argument is {@code null}.
	 * @throws ModelException if {@link #close()} has already been called on this
	 * 			segment and it's in the process of releasing its data.
	 */
	void acquire(SegmentOwner owner) throws ModelException;

	/**
	 * Removes the given {@code owner}'s shared ownership on this segment. If no
	 * more owners are registered to this segment, a subsequent call to {@link #closable()}
	 * will return {@code true}.
	 *
	 * @param owner
	 * @throws NullPointerException if the {@code owner} argument is {@code null}.
	 * @throws ModelException if {@link #close()} has already been called on this
	 * 			segment and it's in the process of releasing its data.
	 * @throws IllegalArgumentException if the given owner does not hold shared ownership
	 * 			of this segment.
	 */
	void release(SegmentOwner owner) throws ModelException;

	Set<SegmentOwner> getOwners();

	/**
	 *
	 * @return
	 */
	boolean closable();

	/**
	 * Checks whether this segment is allowed to be closed and if so, releases
	 * all currently held data. Note that if there are still {@code SegmentOwner}s holding
	 * on to this segment, they will be asked to release their ownership. If after this
	 * initial release phase there is still at least one ownership pending, the call will
	 * fail with an {@code IllegalStateException}.
	 * Otherwise the segment will release its data and disconnect any links to the hosting
	 * corpus.
	 *
	 * @throws ModelException
	 * @throws InterruptedException
	 * @throws IllegalStateException in case there are still owners that could not be made to
	 * 			release their partial ownership of this segment
	 */
	void close() throws ModelException, InterruptedException;

	// Page support

	/**
	 * Returns the maximum number of elements per page, or {@code -1} if this
	 * segment only contains 1 page and that page's size is therefore determined by
	 * the container of this segment's <i>primary layer</i>.
	 * @return
	 */
	int getPageSize();

	/**
	 * Returns the number of available pages for this segment, at least {@code 1}.
	 *
	 * @return
	 */
	int getPageCount();

	/**
	 * Returns the index of the current page, initially {@code -1}
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
	 * @throws ModelException if there was an IO error or other problem encountered
	 * 			while loading data
	 * @throws UnsupportedOperationException if this segment does not support paging
	 * 			(this is the case when {@code #getPageCount()} returns {@code 0})
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getPageCount()</tt>)
	 */
	boolean loadPage(int index) throws ModelException;
}
