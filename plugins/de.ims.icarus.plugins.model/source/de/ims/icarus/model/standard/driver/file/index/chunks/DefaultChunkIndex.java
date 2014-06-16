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
package de.ims.icarus.model.standard.driver.file.index.chunks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.standard.driver.file.FileSet;
import de.ims.icarus.model.standard.driver.file.ManagedFileResource;
import de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter;

/**
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultChunkIndex extends ManagedFileResource implements ChunkIndex {

	private final FileSet fileSet;
	private final ChunkArrays.ArrayAdapter arrayAdapter;

	private static final int BLOCK_POWER = 12;

	private static final int ENTRIES_PER_BLOCK = 2<<BLOCK_POWER;

	public DefaultChunkIndex(Path file, BlockCache cache, int cacheSize,
			FileSet fileSet, boolean largeIndex) {
		super(file, cache, cacheSize);

		if (fileSet == null)
			throw new NullPointerException("Invalid fileSet"); //$NON-NLS-1$

		this.fileSet = fileSet;

		arrayAdapter = createAdapter(fileSet.getFileCount()>1, largeIndex);

		setBytesPerBlock(ENTRIES_PER_BLOCK * arrayAdapter.chunkSize());
	}

	protected ArrayAdapter createAdapter(boolean multiFile, boolean largeIndex) {
		if(multiFile) {
			return largeIndex ? ChunkArrays.createLongFileAdapter() :
				ChunkArrays.createIntFileAdapter();
		} else {
			return largeIndex ? ChunkArrays.createLongAdapter() :
				ChunkArrays.createIntAdapter();
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#getFileSet()
	 */
	@Override
	public FileSet getFileSet() {
		return fileSet;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#newReader()
	 */
	@Override
	public ChunkIndexReader newReader() {
		return this.new Reader();
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#newWriter()
	 */
	@Override
	public ChunkIndexWriter newWriter() {
		return this.new Writer();
	}

	public long getEntryCount() {
		try {
			return Files.size(getFile())/arrayAdapter.chunkSize();
		} catch (IOException e) {
			LoggerFactory.error(this, "Unable to read file size: "+getFile(), e); //$NON-NLS-1$

			return 0;
		}
	}

	protected ArrayAdapter getAdapter() {
		return arrayAdapter;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.ManagedFileResource#read(java.lang.Object, java.nio.ByteBuffer)
	 */
	@Override
	protected int read(Object target, ByteBuffer buffer) throws IOException {
		int length = buffer.remaining()/arrayAdapter.chunkSize();
		arrayAdapter.read(target, buffer, 0, length);
		return length;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.ManagedFileResource#write(java.lang.Object, java.nio.ByteBuffer, int)
	 */
	@Override
	protected void write(Object source, ByteBuffer buffer, int size) throws IOException {
		arrayAdapter.write(source, buffer, 0, size);
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.ManagedFileResource#newBlockData()
	 */
	@Override
	protected Object newBlockData() {
		return arrayAdapter.createBuffer(getBytesPerBlock());
	}

	private int id(long index) {
		return (int) (index>>BLOCK_POWER);
	}

	private int localIndex(long index) {
		return (int)(index & (BLOCK_POWER-1));
	}

	protected int getFileId(long index) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, false);
		return block==null ? -1 : arrayAdapter.getFileId(block.getData(), localIndex);
	}

	protected long getBeginOffset(long index) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, false);
		return block==null ? -1 : arrayAdapter.getBeginOffset(block.getData(), localIndex);
	}

	protected long getEndOffset(long index) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, false);
		return block==null ? -1 : arrayAdapter.getEndOffset(block.getData(), localIndex);
	}

	protected int setFileId(long index, int fileId) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, true);
		int result = arrayAdapter.setFileId(block.getData(), localIndex, fileId);
		lockBlock(id, block);

		return result;
	}

	protected long setBeginOffset(long index, long offset) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, true);
		long result = arrayAdapter.setBeginOffset(block.getData(), localIndex, offset);
		lockBlock(id, block);

		return result;
	}

	protected long setEndOffset(long index, long offset) {
		int id = id(index);
		int localIndex = localIndex(index);

		Block block = getBlock(id, true);
		long result = arrayAdapter.setEndOffset(block.getData(), localIndex, offset);
		lockBlock(id, block);

		return result;
	}

	private class Reader implements ChunkIndexReader {

		private Reader() {
			incrementUseCount();
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public ChunkIndex getSource() {
			return DefaultChunkIndex.this;
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			getReadLock().lock();
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			getReadLock().unlock();
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() throws ModelException {
			decrementUseCount();
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndexReader#getEntryCount()
		 */
		@Override
		public long getEntryCount() {
			return DefaultChunkIndex.this.getEntryCount();
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndexReader#getFileId(long)
		 */
		@Override
		public int getFileId(long index) {
			return DefaultChunkIndex.this.getFileId(index);
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndexReader#getBeginOffset(long)
		 */
		@Override
		public long getBeginOffset(long index) {
			return DefaultChunkIndex.this.getBeginOffset(index);
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndexReader#getEndOffset(long)
		 */
		@Override
		public long getEndOffset(long index) {
			return DefaultChunkIndex.this.getEndOffset(index);
		}

	}

	private class Writer implements ChunkIndexWriter {

		private Writer() {
			incrementUseCount();
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public ChunkIndex getSource() {
			return DefaultChunkIndex.this;
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			getWriteLock().lock();
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			getWriteLock().unlock();
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() throws ModelException {
			decrementUseCount();
		}

		/**
		 * @see java.io.Flushable#flush()
		 */
		@Override
		public void flush() throws IOException {
			DefaultChunkIndex.this.flush();
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndexWriter#setFileId(long, int)
		 */
		@Override
		public int setFileId(long index, int fileId) {
			return DefaultChunkIndex.this.setFileId(index, fileId);
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndexWriter#setBeginOffset(long, long)
		 */
		@Override
		public long setBeginOffset(long index, long offset) {
			return DefaultChunkIndex.this.setBeginOffset(index, offset);
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndexWriter#setEndOffset(long, long)
		 */
		@Override
		public long setEndOffset(long index, long offset) {
			return DefaultChunkIndex.this.setEndOffset(index, offset);
		}

	}
}
