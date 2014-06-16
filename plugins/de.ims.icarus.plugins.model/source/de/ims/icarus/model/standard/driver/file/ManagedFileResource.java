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
package de.ims.icarus.model.standard.driver.file;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.io.SynchronizedAccessor;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ManagedFileResource {

	/**
	 * Maximum size of supported files, limited to 32 Gigabytes
	 */
	public static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L * 32L;

	/**
	 * Minimum size of cache blocks in bytes. Chosen to be {@value #MIN_BLOCK_SIZE}
	 * so that a file within the size limitations of {@link #MAX_FILE_SIZE} can still
	 * be addressed on the block level by means of integer values.
	 */
	public static final long MIN_BLOCK_SIZE = 1000L;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final AtomicInteger useCount = new AtomicInteger();

	private final Path file;

	private final BlockCache cache;

	private final TIntSet changedBlocks = new TIntHashSet();

	private int bytesPerBlock = -1;
	private final int cacheSize;

	private ByteBuffer buffer;
	private Block tmpBlock;

	protected ManagedFileResource(Path file, BlockCache cache, int cacheSize) {
		if (file == null)
			throw new NullPointerException("Invalid folder"); //$NON-NLS-1$

		if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createFile(file);
			} catch (IOException e) {
				throw new ModelException(null, ModelError.DRIVER_INDEX_IO,
						"Failed to open managed resource", e); //$NON-NLS-1$
			}
		}

		if(!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
			throw new IllegalArgumentException("Supplied file is not regular file: "+file); //$NON-NLS-1$

		if (cache == null)
			throw new NullPointerException("Invalid cache"); //$NON-NLS-1$

		this.file = file;
		this.cache = cache;
		this.cacheSize = cacheSize;
	}

	protected void setBytesPerBlock(int bytesPerBlock) {
		// Ensure we never have to worry about block id values
		// exceeding Integer.MAX_VALUE
		if(bytesPerBlock<MIN_BLOCK_SIZE)
			throw new IllegalArgumentException("Invalid block size: "+bytesPerBlock+" - minimum size is "+MIN_BLOCK_SIZE); //$NON-NLS-1$ //$NON-NLS-2$

		this.bytesPerBlock = bytesPerBlock;
	}

	public final Lock getReadLock() {
		return lock.readLock();
	}

	public final Lock getWriteLock() {
		return lock.writeLock();
	}

	/**
	 * @return the bytesPerBlock
	 */
	public int getBytesPerBlock() {
		return bytesPerBlock;
	}

	public final void delete() throws IOException {
		close();
		Files.delete(file);
	}

	protected final void lockBlock(int id, Block block) {
		changedBlocks.add(id);
		block.lock();
	}

	protected final void refreshBlockSize(Block block, int size) {
		if(size<0 || size>block.getSize()+1)
			throw new ModelException(ModelError.DRIVER_INDEX_WRITE_VIOLATION,
					"Entry index out of boundy for block: "+size+" - expected non negative value up to "+block.getSize()+1); //$NON-NLS-1$ //$NON-NLS-2$
		block.setSize(size);
	}

	protected final Block getBlock(int id, boolean writeAccess) {
		Block block = cache.getBlock(id);

		if(block==null) {

			// Automatic flushing if cache gets stale
			if(changedBlocks.size()>(cacheSize>>1)) {
				try {
					flush();
				} catch (IOException e) {
					throw new ModelException(ModelError.DRIVER_INDEX_IO,
							"Failed to automatically flush index changes", e); //$NON-NLS-1$
				}
			}

			long offset = id*(long)bytesPerBlock;

			try {
				boolean exists = Files.size(file)>offset;

				// We can abort lookup if our desired offset is outside the file bounds
				// and all we want to do is read data
				if(!exists && !writeAccess) {
					return null;
				}

				if(tmpBlock==null) {
					tmpBlock = new Block(newBlockData());
				}

				block = tmpBlock;

				if(!readBlock(block, file, offset)) {
					return null;
				}

				tmpBlock = cache.addBlock(block, id);
			} catch(IOException e) {
				throw new ModelException(null, ModelError.DRIVER_INDEX_IO,
						"Failed to read block "+id+" in file "+file, e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		return block;
	}

	private boolean readBlock(Block block, Path file, long offset) throws IOException {
		try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {

			// Read data from channel
			channel.position(offset);
			int bytesRead = channel.read(buffer);

			if(bytesRead==-1) {
				return false;
			}

			// Read entries from buffer
			int entriesRead = read(block.data, buffer);

			if(entriesRead<=0) {
				return false;
			}

			// Save number of entries read
			block.setSize(entriesRead);

			return true;
		}
	}

	private boolean writeBlock(Block block, Path file, long offset) throws IOException {
		try(SeekableByteChannel channel = Files.newByteChannel(file,
				StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {

			// Write data to buffer
			write(block.getData(), buffer, block.getSize());

			// Copy buffer to channel
			channel.position(offset);
			channel.write(buffer);

			return true;
		}
	}

	protected abstract void write(Object source, ByteBuffer buffer, int length) throws IOException;

	protected abstract int read(Object target, ByteBuffer buffer) throws IOException;

	public void flush() throws IOException {
		for(TIntIterator it = changedBlocks.iterator(); it.hasNext(); ) {
			int id = it.next();

			Block block = cache.getBlock(id);
			if(block==null)
				throw new CorruptedStateException("Missing block marked as locked: "+id); //$NON-NLS-1$

			long offset = id*(long)bytesPerBlock;

			writeBlock(block, file, offset);

			it.remove();
			block.unlock();
		}
	}

	protected abstract Object newBlockData();

	public final void incrementUseCount() {
		// Previous use count
		int count = useCount.getAndIncrement();

		if(count==0) {
			open();
		}
	}

	protected void open() {
		if(bytesPerBlock<0)
			throw new IllegalStateException("No block size defined - cannot allocate buffer"); //$NON-NLS-1$

		buffer = ByteBuffer.allocateDirect(bytesPerBlock);

		cache.open(cacheSize);
	}

	public final void decrementUseCount() {
		// New use count
		int count = useCount.decrementAndGet();

		if(count==0) {
			try {
				close();
			} catch (IOException e) {
				throw new ModelException(null, ModelError.DRIVER_INDEX_IO,
						"Failed to close managed resource", e); //$NON-NLS-1$
			}
		}
	}

	protected void close() throws IOException {
		try {
			flush();
		} finally {
			buffer = null;
			tmpBlock = null;
			cache.close();
		}
	}

	public final Path getFile() {
		return file;
	}

	public static final class Block {

		// To check if a block differs from default
		private int size = 0;
		// Storage data
		private Object data;
		// Flag to prevent removal from cache
		private boolean locked;

		Block(Object data) {
			this.data = data;
		}

		public int getSize() {
			return size;
		}

		public Object getData() {
			return data;
		}

		public boolean isLocked() {
			return locked;
		}

		void setSize(int size) {
			this.size = size;
		}

		void setData(Object data) {
			this.data = data;
		}

		void lock() {
			locked = true;
		}

		void unlock() {
			locked = false;
		}
	}

	public interface BlockCache {

		public static final int MIN_CAPACITY = 100;

		/**
		 * Lookup the block stored for the specified {@code id}. If the cache
		 * does not contain such a block, return {@code null}.
		 *
		 * @param id
		 * @return
		 */
		Block getBlock(int id);

		/**
		 * Add the given {@code block} to the cache using the specified
		 * {@code id}.
		 *
		 * @param block The block that was pushed out of the cache or {@code null}
		 * @param id
		 * @throws IllegalStateException if the supplied block is already present in the cache
		 */
		Block addBlock(Block block, int id);

		/**
		 * Initialize storage and allocate basic resources.
		 */
		void open(int capacity);

		/**
		 * Discard any stored data and invalidate cache until
		 * {@link #open()} gets called.
		 */
		void close();
	}

	/**
	 * Provides a basic implementation for synchronized read access to the data in
	 * the hosting {@link ManagedFileResource}. Creating an instance of this accessor
	 * will automatically increment the resource's use counter and closing it will
	 * then again decrement the counter.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 * @param <T> The source type of the accessor
	 */
	protected class ReadAccessor<T extends Object> implements SynchronizedAccessor<T> {

		protected ReadAccessor() {
			incrementUseCount();
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#getSource()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public T getSource() {
			return (T) ManagedFileResource.this;
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

	}


	/**
	 * Provides a basic implementation for synchronized write access to the data in
	 * the hosting {@link ManagedFileResource}. Creating an instance of this accessor
	 * will automatically increment the resource's use counter and closing it will
	 * then again decrement the counter.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 * @param <T> The source type of the accessor
	 */
	protected class WriteAccessor<T extends Object> implements SynchronizedAccessor<T> {

		protected WriteAccessor() {
			incrementUseCount();
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#getSource()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public T getSource() {
			return (T) ManagedFileResource.this;
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

	}
}
