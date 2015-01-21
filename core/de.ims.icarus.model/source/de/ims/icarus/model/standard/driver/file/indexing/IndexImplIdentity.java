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
package de.ims.icarus.model.standard.driver.file.indexing;

import static de.ims.icarus.model.api.driver.IndexUtils.firstIndex;
import static de.ims.icarus.model.api.driver.IndexUtils.lastIndex;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.api.driver.indexing.Index;
import de.ims.icarus.model.api.driver.indexing.IndexCollector;
import de.ims.icarus.model.api.driver.indexing.IndexReader;
import de.ims.icarus.model.standard.index.SingletonIndexSet;

/**
 * Implements a total index of type {@code one-to-one} which maps
 * indices to their own value between two layers. This index stores
 * <b>no</b> internal state and therefore the synchronization and
 * close methods on its reader instances have no effect!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IndexImplIdentity extends AbstractIndex {

	/**
	 * @see de.ims.icarus.model.api.driver.indexing.Index#newReader()
	 */
	@Override
	public IndexReader newReader() {
		return this.new Reader();
	}

	public class Reader implements IndexReader {

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public Index getSource() {
			return IndexImplIdentity.this;
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() throws ModelException {
			// no-op
		}

		/**
		 * @return
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(long, de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector)
				throws ModelException, InterruptedException {
			collector.add(sourceIndex);
			return true;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(long)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex) throws ModelException,
				InterruptedException {
			return new IndexSet[]{new SingletonIndexSet(sourceIndex)};
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getBeginIndex(long)
		 */
		@Override
		public long getBeginIndex(long sourceIndex) throws ModelException,
				InterruptedException {
			return sourceIndex;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getEndIndex(long)
		 */
		@Override
		public long getEndIndex(long sourceIndex) throws ModelException,
				InterruptedException {
			return sourceIndex;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public IndexSet[] lookup(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {
			return sourceIndices;
		}

		/**
		 * @return
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector)
				throws ModelException, InterruptedException {
			collector.add(sourceIndices);
			return true;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getBeginIndex(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {
			return firstIndex(sourceIndices);
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getEndIndex(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {
			return lastIndex(sourceIndices);
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, long)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex)
				throws ModelException, InterruptedException {
			return targetIndex;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public IndexSet[] find(long fromSource, long toSource,
				IndexSet[] targetIndices) throws ModelException,
				InterruptedException {
			return targetIndices;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector)
				throws ModelException, InterruptedException {
			collector.add(targetIndices);
			return true;
		}

	}
}
