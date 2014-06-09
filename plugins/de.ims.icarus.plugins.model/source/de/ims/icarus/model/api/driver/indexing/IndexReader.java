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

import de.ims.icarus.model.api.CorpusException;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.io.SynchronizedAccessor;

/**
 * Models the read access to an {@link Index} implementation.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface IndexReader extends SynchronizedAccessor<Index> {

	// Single index lookups

	void lookup(long sourceIndex, IndexCollector collector) throws CorpusException, InterruptedException;

	IndexSet[] lookup(long sourceIndex) throws CorpusException, InterruptedException;

	long getBeginIndex(long sourceIndex) throws CorpusException, InterruptedException;
	long getEndIndex(long sourceIndex) throws CorpusException, InterruptedException;

	// Bulk index lookups

	IndexSet[] lookup(IndexSet[] sourceIndices) throws CorpusException, InterruptedException;

	void lookup(IndexSet[] sourceIndices, IndexCollector collector) throws CorpusException, InterruptedException;

	long getBeginIndex(IndexSet[] sourceIndices) throws CorpusException, InterruptedException;
	long getEndIndex(IndexSet[] sourceIndices) throws CorpusException, InterruptedException;
}
