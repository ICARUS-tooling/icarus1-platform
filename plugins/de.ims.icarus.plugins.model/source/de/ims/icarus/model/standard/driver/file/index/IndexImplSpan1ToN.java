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
import de.ims.icarus.model.standard.index.SpanIndexSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IndexImplSpan1ToN extends AbstractFileIndex {

	private final SpanArrays.SpanAdapter spanAdapter;

	private static final int BLOCK_POWER = 14;

	private static final int ENTRIES_PER_BLOCK = 2<<BLOCK_POWER;

	public IndexImplSpan1ToN(Path file, BlockCache cache, boolean largeIndex, int cacheSize) {
		super(file, cache, cacheSize);

		spanAdapter = SpanArrays.createSpanAdapter(largeIndex);
		setBytesPerBlock(ENTRIES_PER_BLOCK * spanAdapter.chunkSize());
	}

	private int id(long index) {
		return (int) (index>>BLOCK_POWER);
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
		return this.new Writer();
	}

	public class Reader extends ReadAccessor<Index> implements IndexReader {

		private final Coverage coverage = getManifest().getCoverage();

		/**
		 * @return
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(long, de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector)
				throws ModelException, InterruptedException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			if(block==null) {
				return false;
			}

			IndexSet indices = new SpanIndexSet(
					spanAdapter.getFrom(block.getData(), localIndex),
					spanAdapter.getTo(block.getData(), localIndex));

			collector.add(indices);

			return true;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(long)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex) throws ModelException,
				InterruptedException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			if(block==null) {
				return new IndexSet[0];
			}

			IndexSet indices = new SpanIndexSet(
					spanAdapter.getFrom(block.getData(), localIndex),
					spanAdapter.getTo(block.getData(), localIndex));

			return new IndexSet[]{indices};
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getBeginIndex(long)
		 */
		@Override
		public long getBeginIndex(long sourceIndex) throws ModelException,
				InterruptedException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			return block==null ? -1L : spanAdapter.getFrom(block.getData(), localIndex);
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getEndIndex(long)
		 */
		@Override
		public long getEndIndex(long sourceIndex) throws ModelException,
				InterruptedException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			return block==null ? -1L : spanAdapter.getTo(block.getData(), localIndex);
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

			if(coverage.isMonotonic()) {
				if(IndexUtils.isContinuous(sourceIndices)) {
					// Special case of a single big span
					collector.add(getBeginIndex(firstIndex(sourceIndices)), getEndIndex(lastIndex(sourceIndices)));
				} else {
					// Requires checks on all individual index sets
					for(IndexSet indices : sourceIndices) {
						if(IndexUtils.isContinuous(indices)) {
							// Spans get projected on other spans
							collector.add(getBeginIndex(indices.firstIndex()), getEndIndex(indices.lastIndex()));
						} else {
							// Expensive version: traverse values and add individual target spans
							for(int i=0; i<indices.size(); i++) {
								long sourceIndex = indices.indexAt(i);
								collector.add(getBeginIndex(sourceIndex), getEndIndex(sourceIndex));
							}
						}
					}
				}

			} else {
				// Expensive version: traverse ALL individual indices and add target spans.
				// Remember however, that we still have an injective index function, so no
				// duplicate checks required!
				for(IndexSet indices : sourceIndices) {
					// Expensive version: traverse values and add individual target spans
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						collector.add(getBeginIndex(sourceIndex), getEndIndex(sourceIndex));
					}
				}
			}

			return true;
		}

		/**
		 * Optimized behavior in case of {@link Coverage#isMonotonic() continuous coverage}:<br>
		 * Since
		 *
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getBeginIndex(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {

			// Optimized handling of continuous coverage: use only first source index
			if(coverage.isMonotonic()) {
				return getBeginIndex(firstIndex(sourceIndices));
			} else {
				// Expensive alternative: traverse all indices
				long result = Long.MAX_VALUE;

				for(IndexSet indices : sourceIndices) {
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.min(result, getBeginIndex(sourceIndex));
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

			// Optimized handling of monotonic coverage: use only last source index
			if(coverage.isMonotonic()) {
				return getEndIndex(lastIndex(sourceIndices));
			} else {
				// Expensive alternative: traverse all indices
				long result = Long.MIN_VALUE;

				for(IndexSet indices : sourceIndices) {
					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.max(result, getBeginIndex(sourceIndex));
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
			int idFrom = id(fromSource);
			int idTo = id(toSource);
			int localFrom = localIndex(fromSource);
			int localTo = localIndex(toSource);

			// Check whether or not the candidate space spans across multiple blocks
			if(idFrom==idTo) {
				// Only requires one block to be loaded
				return find(idFrom, localFrom, localTo, targetIndex);
			} else {
				// Check first block
				long result = find(idFrom, localFrom, localTo, targetIndex);
				if(result!=-1L) {
					return result;
				}

				// Check last block
				result = find(idTo, localFrom, localTo, targetIndex);
				if(result!=-1L) {
					return result;
				}

				// Iterate intermediate blocks
				for(int id=idFrom+1; id<idTo; id++) {
					// Now always include the entire block to search
					result = find(id, 0, ENTRIES_PER_BLOCK-1, targetIndex);
					if(result!=-1L) {
						return result;
					}
				}
			}

			return -1L;
		}

		private long find(int id, int localFrom, int localTo, long targetIndex) {

			Block block = getBlock(id, false);

			return block==null ? -1L : spanAdapter.find(block.getData(), localFrom, localTo, targetIndex);
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
			int idFrom = id(fromSource);
			int idTo = id(toSource);
			int localFrom = localIndex(fromSource);
			int localTo = localIndex(toSource);

			// TODO Auto-generated method stub

		}

	}

	public class Writer extends WriteAccessor<Index> implements IndexWriter {

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.IndexWriter#add(long, long, long)
		 */
		@Override
		public void add(long sourceIndex, long from, long to) {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			spanAdapter.setFrom(block.getData(), localIndex, from);
			spanAdapter.setTo(block.getData(), localIndex, to);
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.IndexWriter#add(long, long)
		 */
		@Override
		public void add(long sourceIndex, long targetIndex) {
			throw new UnsupportedOperationException("Can only add spans"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.IndexWriter#add(long, de.ims.icarus.model.api.driver.IndexSet)
		 */
		@Override
		public void add(long sourceIndex, IndexSet indices) {
			if(!IndexUtils.isContinuous(indices))
				throw new UnsupportedOperationException("Can only add spans"); //$NON-NLS-1$

			add(sourceIndex, indices.firstIndex(), indices.lastIndex());
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.IndexWriter#add(long, de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public void add(long sourceIndex, IndexSet[] indices) {
			if(!IndexUtils.isContinuous(indices))
				throw new UnsupportedOperationException("Can only add spans"); //$NON-NLS-1$

			add(sourceIndex, firstIndex(indices), lastIndex(indices));
		}

	}
}
