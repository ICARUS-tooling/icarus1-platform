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
package de.ims.icarus.model.api.driver.indexing;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.api.driver.IndexUtils;
import de.ims.icarus.model.io.SynchronizedAccessor;

/**
 * Models the read access to an {@link Index} implementation. Note that all
 * methods in this interface that take arrays of {@link IndexSet} instances as
 * arguments, expect those arrays to be sorted according to the order defined by
 * {@link IndexUtils#INDEX_SET_SORTER}!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface IndexReader extends SynchronizedAccessor<Index> {

	public static final long INVALID = -1L;

	// Single index lookups

	boolean lookup(long sourceIndex, IndexCollector collector) throws ModelException, InterruptedException;

	IndexSet[] lookup(long sourceIndex) throws ModelException, InterruptedException;

	long getBeginIndex(long sourceIndex) throws ModelException, InterruptedException;
	long getEndIndex(long sourceIndex) throws ModelException, InterruptedException;

	// Bulk index lookups

	IndexSet[] lookup(IndexSet[] sourceIndices) throws ModelException, InterruptedException;

	boolean lookup(IndexSet[] sourceIndices, IndexCollector collector) throws ModelException, InterruptedException;

	long getBeginIndex(IndexSet[] sourceIndices) throws ModelException, InterruptedException;
	long getEndIndex(IndexSet[] sourceIndices) throws ModelException, InterruptedException;

	// Utility method for efficient reverse lookups

	/**
	 * Find the source index that maps to the specified {@code targetIndex}, restricting the
	 * search to the closed interval {@code fromSource} to {@code toSource}. This method is
	 * intended for use of reverse indices that are able to efficiently pin down the possible
	 * range of source indices for a given target index and then delegate the remaining work
	 * of the lookup to an existing index inverse to their own mapping direction.
	 *
	 * @param fromSource
	 * @param toSource
	 * @param targetIndex
	 * @return
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	long find(long fromSource, long toSource, long targetIndex) throws ModelException, InterruptedException;

	/**
	 * Performs a reverse lookup for a collection of target indices. Note that the {@code targetIndices}
	 * array is required to be sorted according to {@link IndexUtils#INDEX_SET_SORTER}!
	 *
	 * @see #find(long, long, long)
	 *
	 * @param fromSource
	 * @param toSource
	 * @param targetIndices
	 * @return
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	IndexSet[] find(long fromSource, long toSource, IndexSet[] targetIndices) throws ModelException, InterruptedException;

	boolean find(long fromSource, long toSource, IndexSet[] targetIndices, IndexCollector collector) throws ModelException, InterruptedException;
}
