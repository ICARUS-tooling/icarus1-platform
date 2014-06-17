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
import de.ims.icarus.model.api.ContainerType;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.api.driver.IndexUtils;
import de.ims.icarus.model.api.driver.IndexUtils.IndexProcedure;
import de.ims.icarus.model.api.driver.indexing.Index;
import de.ims.icarus.model.api.driver.indexing.IndexCollector;
import de.ims.icarus.model.api.driver.indexing.IndexReader;
import de.ims.icarus.model.api.manifest.IndexManifest.Coverage;
import de.ims.icarus.model.standard.index.IndexSetBuilder;
import de.ims.icarus.model.standard.index.SpanIndexSet;

/**
 * Implements a one-to-many index for containers of type {@link ContainerType#SPAN}.
 * It stores the begin and end offsets for each span as a pair of either integer or long
 * values (resulting in 8 or 16 bytes per entry). The nature of spans allows for some
 * very efficient optimizations for the corresponding {@link IndexReader} implementations
 * this index provides:
 * <p>
 * If an index function is monotonic, then the begin index of the (partially) covered target
 * indices of a set of sorted source spans is always the target begin index of the first span
 * (the same holds for end index values, where the last index in the last span is sued).
 * In addition a continuous collection of spans always maps to a continuous subset of the
 * target index space, described by the projected target indices of the collections first and
 * last spans.
 * <p>
 * For reverse lookups the two {@code find} methods of the {@code IndexReader} interface are
 * implemented to use binary search in order to pin down source spans in a predefined range.
 * <p>
 * The file based storage is organized in blocks with {@value #ENTRIES_PER_BLOCK} (<tt>2^14</tt>)
 * entries each.
 *
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
		public boolean lookup(long sourceIndex, IndexCollector collector) throws ModelException {
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
		public IndexSet[] lookup(long sourceIndex) throws ModelException {
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
		public long getBeginIndex(long sourceIndex) throws ModelException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			return block==null ? -1L : spanAdapter.getFrom(block.getData(), localIndex);
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getEndIndex(long)
		 */
		@Override
		public long getEndIndex(long sourceIndex) throws ModelException {
			int id = id(sourceIndex);
			int localIndex = localIndex(sourceIndex);

			Block block = getBlock(id, false);
			return block==null ? INVALID : spanAdapter.getTo(block.getData(), localIndex);
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
		 * Runs a check for interrupted thread state before processing an index set.
		 *
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
							checkInterrupted();

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
					checkInterrupted();

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
		 * Runs a check for interrupted thread state before processing an index set.
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
					checkInterrupted();

					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.min(result, getBeginIndex(sourceIndex));
					}
				}

				return result;
			}
		}

		/**
		 *
		 * Runs a check for interrupted thread state before processing an index set.
		 *
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
					checkInterrupted();

					for(int i=0; i<indices.size(); i++) {
						long sourceIndex = indices.indexAt(i);
						result = Math.max(result, getBeginIndex(sourceIndex));
					}
				}

				return result;
			}
		}

		private long translate(int id, int localIndex) {
			return localIndex==-1 ? INVALID : id*ENTRIES_PER_BLOCK + localIndex;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, long)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex) throws ModelException {
			int idFrom = id(fromSource);
			int idTo = id(toSource);
			int localFrom = localIndex(fromSource);
			int localTo = localIndex(toSource);

			return find(idFrom, idTo, localFrom, localTo, targetIndex);
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

		private long find(int idFrom, int idTo, int localFrom, int localTo, long targetIndex) {
			// Special case of a single block search
			if(idFrom==idTo) {
				return find0(idFrom, localFrom, localTo, targetIndex);
			}

			// Check first block
			long result = find0(idFrom, localFrom, ENTRIES_PER_BLOCK-1, targetIndex);
			if(result!=INVALID) {
				return result;
			}

			// Check last block
			result = find0(idTo, 0, localTo, targetIndex);
			if(result!=INVALID) {
				return result;
			}

			// Iterate intermediate blocks
			for(int id=idFrom+1; id<idTo; id++) {
				// Now always include the entire block to search
				result = find0(id, 0, ENTRIES_PER_BLOCK-1, targetIndex);
				if(result!=INVALID) {
					return result;
				}
			}

			return INVALID;
		}

		private long find0(int id, int localFrom, int localTo, long targetIndex) {

			Block block = getBlock(id, false);

			return block==null ? INVALID : translate(id, spanAdapter.find(block.getData(), localFrom, localTo, targetIndex));
		}

		private long findContinuous(int idFrom, int idTo, int localFrom, int localTo,
				long targetBegin, long targetEnd, IndexCollector collector) {

			// Find first span covering the targetBegin
			long sourceBegin = find(idFrom, idTo, localFrom, localTo, targetBegin);

			if(sourceBegin==INVALID) {
				return INVALID;
			}

			// Refresh left end of search interval
			idFrom = id(sourceBegin);
			localFrom = localIndex(sourceBegin);

			// Find last span covering tragetEnd
			long sourceEnd = find(idFrom, idTo, localFrom, localTo, targetEnd);

			if(sourceEnd==INVALID) {
				return INVALID;
			}

			collector.add(sourceBegin, sourceEnd);

			return sourceEnd;
		}

		/**
		 * @return
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean find(final long fromSource, final long toSource,
				final IndexSet[] targetIndices, final IndexCollector collector)
				throws ModelException, InterruptedException {

			if(coverage.isMonotonic()) {

				/*
				 * In case of monotonic index we can adjust our search interval for
				 * the source index space whenever we successfully resolve some
				 * source indices. In addition the first miss is bound to cause the
				 * entire search to fail.
				 * each
				 */

				IndexProcedure proc = new IndexProcedure() {

					int idFrom = id(fromSource);
					int idTo = id(toSource);
					int localFrom = localIndex(fromSource);
					int localTo = localIndex(toSource);

					long targetEnd = lastIndex(targetIndices);

					@Override
					public boolean process(long from, long to) {
						long sourceIndex;

						if(from==to) {
							sourceIndex = find(idFrom, idTo, localFrom, localTo, from);
							if(sourceIndex==INVALID) {
								return false;
							}

							// Manually add mapped source index
							collector.add(sourceIndex);
						} else {
							// The mapped span will already be added inside the inner method
							sourceIndex = findContinuous(idFrom, idTo,
									localFrom, localTo, from, to, collector);

							// Here sourceIndex is the index of the last span that was found
							if(sourceIndex==INVALID) {
								return false;
							}
						}

						if(sourceIndex>=toSource || getEndIndex(sourceIndex)>=targetEnd) {
							return false;
						} else {
							// There has to be space left to map the remaining target indices, so
							// reset interval begin to the next span after the current

							idFrom = id(sourceIndex+1);
							localFrom = localIndex(sourceIndex+1);

							return true;
						}
					}
				};

				return IndexUtils.forEachSpan(targetIndices, proc);
			} else {

				/*
				 * Non-monotonic mapping means the only way of optimizing the search
				 * is to shrink the source interval whenever we encounter spans that
				 * overlap with the current end of the interval.
				 */

				IndexProcedure proc = new IndexProcedure() {

					int idFrom = id(fromSource);
					int idTo = id(toSource);
					int localFrom = localIndex(fromSource);
					int localTo = localIndex(toSource);

					long _fromSource = fromSource;
					long _toSource = toSource;

					@Override
					public boolean process(long from, long to) {

						while(from<=to) {
							long sourceIndex = find(idFrom, idTo, localFrom, localTo, from);

							if(sourceIndex==INVALID) {
								// Continue through the search space when no match was found
								from++;
							} else {

								collector.add(sourceIndex);

								// Fetch end of span to prune some target indices
								long spanEnd = getEndIndex(sourceIndex);

								// Step forward to either the next target index or after
								// the end of the found span, whichever is greater
								from = Math.max(spanEnd, from)+1;

								// Shrink search interval if possible
								if(sourceIndex==_fromSource) {
									_fromSource++;
									idFrom = id(_fromSource);
									localFrom = localIndex(_fromSource);
								}
								if(sourceIndex==_toSource) {
									_toSource--;
									idTo  = id(_toSource);
									localTo = localIndex(_toSource);
								}
							}

							// Global state check of the search window
							if(_toSource<_fromSource) {
								// Search space exhausted, abort future processing
								return false;
							}
						}

						// Only way of finishing search is exhaustion of search space,
						// so always allow to continue here
						return true;
					}
				};

				return IndexUtils.forEachSpan(targetIndices, proc);
			}

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
