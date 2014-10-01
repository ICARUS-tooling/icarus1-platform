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
import de.ims.icarus.model.iql.Query;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SubCorpus {

	/**
	 * Returns the {@code Corpus} that is backing this sub corpus.
	 * @return
	 */
	Corpus getCorpus();

	/**
	 * Returns the {@code scope} that was used to limit the contexts
	 * and layers involved in this sub corpus or {@code null} if no vertical
	 * filtering was performed.
	 */
	Scope getScope();

	/**
	 * Returns the {@code Query} that was used to narrow down the
	 * elements in this sub corpus or {@code null} if no horizontal
	 * filtering was performed.
	 */
	Query getQuery();

	/**
	 * Returns the number of element in this sub corpus, i.e. the number of markables
	 * contained in the <i>primary-layer</i> of this sub corpuses' {@code Scope}.
	 *
	 * @return
	 */
	int getSize();

	// Destruction support

	/**
	 * Attempts to acquire shared ownership of this sub corpus by the given {@code owner}.
	 * If the given owner already holds shared ownership if this sub corpus, the method
	 * simply returns.
	 *
	 * @param owner
	 * @throws NullPointerException if the {@code owner} argument is {@code null}.
	 * @throws ModelException if {@link #close()} has already been called on this
	 * 			sub corpus and it's in the process of releasing its data.
	 */
	void acquire(CorpusOwner owner) throws ModelException;

	/**
	 * Removes the given {@code owner}'s shared ownership on this sub corpus. If no
	 * more owners are registered to this sub corpus, a subsequent call to {@link #closable()}
	 * will return {@code true}.
	 *
	 * @param owner
	 * @throws NullPointerException if the {@code owner} argument is {@code null}.
	 * @throws ModelException if {@link #close()} has already been called on this
	 * 			sub corpus and it's in the process of releasing its data.
	 * @throws IllegalArgumentException if the given owner does not hold shared ownership
	 * 			of this sub corpus.
	 */
	void release(CorpusOwner owner) throws ModelException;

	/**
	 * Returns an immutable set view of all the owners currently registered with this sub-corpus.
	 */
	Set<CorpusOwner> getOwners();

	/**
	 * Checks whether or not the sub-corpus is currently closable, i.e.
	 * @return
	 */
	boolean closable();

	/**
	 * Checks whether this sub corpus is allowed to be closed and if so, releases
	 * all currently held data. Note that if there are still {@code CorpusOwner}s holding
	 * on to this sub corpus, they will be asked to release their ownership. If after this
	 * initial release phase there is still at least one ownership pending, the call will
	 * fail with an {@code ModelException}.
	 * Otherwise the sub corpus will release its data and disconnect any links to the hosting
	 * corpus.
	 *
	 * @throws ModelException
	 * @throws InterruptedException
	 * @throws IllegalStateException in case there are still owners that could not be made to
	 * 			release their partial ownership of this sub corpus
	 */
	void close() throws ModelException, InterruptedException;

	// Page support

	/**
	 * Returns the maximum number of elements per page, or {@code -1} if this
	 * sub corpus only contains {@code 1} page and that page's size is therefore determined by
	 * the size of this sub corpus's <i>primary layer</i>.
	 * @return
	 */
	int getPageSize();

	/**
	 * Returns the number of available pages for this sub corpus.
	 * A value of {@code -1} indicates that the sub corpus contains the entire corpus it
	 * originated from. In this case the {@link #loadPage(int)} method will ignore any
	 * {@code index} parameter and attempt to load the entire corpus.
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
	 * into this sub-corpus.
	 * Note that the {@code index} parameter will be effectively ignored in case this sub corpus
	 * does not contain any "real" pages (indicated by the {@link #getPageCount()} method returning
	 * {@code 0}. Instead the entire corpus will be loaded as a whole!
	 *
	 * @param index the index of the page to load
	 * @return {@code true} iff loading the requested page succeeded without errors
	 * and the content of this sub corpus changed as a result.
	 * @throws ModelException if there was an IO error or other problem encountered
	 * 			while loading data (like memory shortage, ...)
     * @throws IndexOutOfBoundsException if this sub corpus supports pagin and
     * 			 the index is out of range (<tt>index &lt; 0 || index &gt;= getPageCount()</tt>)
	 */
	boolean loadPage(int index) throws ModelException;

	/**
	 * Checks whether or not the data for the current page has been loaded.
	 * @return
	 */
	boolean isPageLoaded();

	/**
	 * Fetches the shared corpus model view on the data represented by this sub corpus.
	 * Note that for this method to succeed the data for the current page has to
	 * be properly loaded! Attempting to fetch a model view for a sub corpus whose
	 * current page is empty, will result in a {@code ModelException} being thrown.
	 *
	 * @return the corpus model view on the data represented by this sub corpus.
	 * @throws ModelException if no data has been loaded so far
	 */
	CorpusModel getModel() throws ModelException;
}
