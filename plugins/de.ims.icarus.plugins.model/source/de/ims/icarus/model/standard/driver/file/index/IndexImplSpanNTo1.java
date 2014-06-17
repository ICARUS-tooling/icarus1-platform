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
package de.ims.icarus.model.standard.driver.file.index;

import static de.ims.icarus.model.api.driver.IndexUtils.firstIndex;
import static de.ims.icarus.model.api.driver.IndexUtils.lastIndex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.api.driver.IndexUtils;
import de.ims.icarus.model.api.driver.indexing.Index;
import de.ims.icarus.model.api.driver.indexing.IndexCollector;
import de.ims.icarus.model.api.driver.indexing.IndexReader;
import de.ims.icarus.model.api.manifest.IndexManifest.Coverage;
import de.ims.icarus.model.standard.index.IndexSetBuilder;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IndexImplSpanNTo1 extends AbstractFileIndex {

	private final SpanArrays.SpanAdapter spanAdapter;

	private static final int GROUP_POWER = 7;

	@SuppressWarnings("unused")
	private static final int INDICES_PER_GROUP = 2<<GROUP_POWER;

	private static final int BLOCK_POWER = 14;

	private static final int ENTRIES_PER_BLOCK = 2<<BLOCK_POWER;

	private final Index inverseIndex;

	public IndexImplSpanNTo1(Path file, BlockCache cache, boolean largeIndex, int cacheSize, Index inverseIndex) {
		super(file, cache, cacheSize);

		if (inverseIndex == null)
			throw new NullPointerException("Invalid inverseIndex"); //$NON-NLS-1$

		this.inverseIndex = inverseIndex;

		spanAdapter = SpanArrays.createSpanAdapter(largeIndex);
		setBytesPerBlock(ENTRIES_PER_BLOCK * spanAdapter.chunkSize());
	}

	private long group(long index) {
		return index>>>GROUP_POWER;
	}

	private int id(long index) {
		return (int) (index>>>BLOCK_POWER);
	}

	private int localIndex(long index) {
		return (int)(index & (BLOCK_POWER-1));
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.ManagedFileResource#write(java.lang.Object, java.nio.ByteBuffer, int)
	 */
	@Override
	protected void write(Object source, ByteBuffer buffer, int length)
			throws IOException {
		spanAdapter.write(source, buffer, 0, length);
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.ManagedFileResource#read(java.lang.Object, java.nio.ByteBuffer)
	 */
	@Override
	protected int read(Object target, ByteBuffer buffer) throws IOException {
		int length = buffer.remaining()/spanAdapter.chunkSize();
		spanAdapter.read(target, buffer, 0, length);
		return length;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.ManagedFileResource#newBlockData()
	 */
	@Override
	protected Object newBlockData() {
		return spanAdapter.createBuffer(getBytesPerBlock());
	}

	/**
	 * @see de.ims.icarus.model.api.driver.indexing.Index#newReader()
	 */
	@Override
	public IndexReader newReader() {
		return this.new Reader();
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.AbstractFileIndex#newWriter()
	 */
	@Override
	public IndexWriter newWriter() {
		// TODO Auto-generated method stub
		return null;
	}

	public class Reader extends ReadAccessor<Index> implements IndexReader {

		// Used for the final step in lookup resolution
		// Represents one-to-many mapping
		private final IndexReader inverseReader = inverseIndex.newReader();
		private final Coverage inverseCoverage = inverseReader.getSource().getManifest().getCoverage();

		private long lookup0(long sourceIndex) throws ModelException, InterruptedException {
			long group = group(sourceIndex);
			int id = id(group);
			int localIndex = localIndex(group);

			Block block = getBlock(id, false);
			if(block==null) {
				return INVALID;
			}

			long fromSource = spanAdapter.getFrom(block.getData(), localIndex);
			long toSource = spanAdapter.getTo(block.getData(), localIndex);

			return inverseReader.find(fromSource, toSource, sourceIndex);
		}

		/**
		 * @return
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(long, de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector)
				throws ModelException, InterruptedException {
			long result = lookup0(sourceIndex);

			if(result!=INVALID) {
				collector.add(result);
				return true;
			} else {
				return false;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(long)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex) throws ModelException,
				InterruptedException {
			return IndexUtils.wrap(lookup0(sourceIndex));
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getBeginIndex(long)
		 */
		@Override
		public long getBeginIndex(long sourceIndex) throws ModelException,
				InterruptedException {
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getEndIndex(long)
		 */
		@Override
		public long getEndIndex(long sourceIndex) throws ModelException,
				InterruptedException {
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public IndexSet[] lookup(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {

			IndexSetBuilder builder = new IndexSetBuilder();

			lookup(sourceIndices, builder);

			return builder.build();
		}

		/**
		 * @return
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector)
				throws ModelException, InterruptedException {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getBeginIndex(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {

			// Optimized handling of continuous inverseCoverage: use only first source index
			if(inverseCoverage.isMonotonic()) {
				return lookup0(firstIndex(sourceIndices));
			} else {
				// Expensive alternative: traverse all indices
				long result = Long.MAX_VALUE;

				for(IndexSet indices : sourceIndices) {
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.min(result, lookup0(sourceIndex));
					}
				}

				return result;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getEndIndex(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {

			// Optimized handling of monotonic inverseCoverage: use only last source index
			if(inverseCoverage.isMonotonic()) {
				return lookup0(lastIndex(sourceIndices));
			} else {
				// Expensive alternative: traverse all indices
				long result = Long.MIN_VALUE;

				for(IndexSet indices : sourceIndices) {
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.max(result, lookup0(sourceIndex));
					}
				}

				return result;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, long)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex)
				throws ModelException, InterruptedException {
			long spanBegin = inverseReader.getBeginIndex(targetIndex);
			long spanEnd = inverseReader.getEndIndex(targetIndex);

			if(spanBegin==INVALID || spanEnd==INVALID) {
				return INVALID;
			}

			spanBegin = Math.max(spanBegin, fromSource);
			spanBegin = Math.min(spanEnd, toSource);

			return spanBegin<=spanEnd ? spanBegin : INVALID;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public IndexSet[] find(long fromSource, long toSource,
				IndexSet[] targetIndices) throws ModelException,
				InterruptedException {

			IndexSetBuilder builder = new IndexSetBuilder();

			find(fromSource, toSource, targetIndices, builder);

			return builder.build();
		}

		/**
		 * @return
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector)
				throws ModelException, InterruptedException {
			// TODO Auto-generated method stub

		}

	}
}
