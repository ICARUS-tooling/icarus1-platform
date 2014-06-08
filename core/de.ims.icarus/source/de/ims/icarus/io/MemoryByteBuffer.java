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
package de.ims.icarus.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MemoryByteBuffer {

	private byte[] buffer;

	private static final long INT_LIMIT = (long) Integer.MAX_VALUE;

	private final Object bufferLock = new Object();
	private int size = 0;

	public MemoryByteBuffer() {
		this(8000);
	}

	public MemoryByteBuffer(int size) {
		if(size<0)
			throw new IllegalArgumentException("Negative buffer size"); //$NON-NLS-1$

		buffer = new byte[size];
	}

	public SeekableByteChannel newChannel() {
		return this.new SlaveChannel();
	}

	public int size() {
		synchronized (bufferLock) {
			return size;
		}
	}

	public int read(int position, ByteBuffer dst) throws IOException {
		if (dst == null)
			throw new NullPointerException("Invalid dst"); //$NON-NLS-1$

		synchronized (bufferLock) {
			int size = size();

			if(position>=size || position<0)
				throw new IndexOutOfBoundsException("Position out of bounds(0 to "+(size-1)+": "+position); //$NON-NLS-1$ //$NON-NLS-2$

			int bytesToRead = Math.min(dst.remaining(), size-position);

			if(bytesToRead==0) {
				return -1;
			}

			for(int i=0; i<bytesToRead; i++) {
				dst.put(buffer, position, bytesToRead);
			}

			return bytesToRead;
		}
	}

	public int write(int position, ByteBuffer src) throws IOException {
		if (src == null)
			throw new NullPointerException("Invalid src"); //$NON-NLS-1$

		synchronized (bufferLock) {

			int bytesToWrite = Math.min(Integer.MAX_VALUE-position, src.remaining());

			ensureCapacity(position+bytesToWrite);

			src.get(buffer, position, bytesToWrite);

			return bytesToWrite;
		}

	}

	public void truncate(int size) {
		if(size<0)
			throw new IllegalArgumentException("Negative size: "+size); //$NON-NLS-1$

		synchronized (bufferLock) {
			int currentSize = size();

			if(size>currentSize) {
				return;
			}

			Arrays.fill(buffer, size, currentSize, (byte)0);
			this.size = size;
		}
	}

	// Assumed to be called under bufferLock synchronization
	private void ensureCapacity(int required) {

		int capacity = buffer.length;

		if(required<capacity) {
			return;
		}

		//TODO optimize growth factor
		double growthFactor = 2.0;
		if(capacity>1000000) {
			growthFactor = 1.5;
		}

		int newCapacity = Math.max((int)(capacity*growthFactor), required);

		//FIXME
//		byte[] newBuffer = new byte[newCapacity];
//		System.arraycopy(buffer, 0, newBuffer, 0, size);

		buffer = Arrays.copyOf(buffer, newCapacity);
	}

	private class SlaveChannel implements SeekableByteChannel {

		private AtomicInteger position = new AtomicInteger();
		private AtomicBoolean closed = new AtomicBoolean();

		/**
		 * @see java.nio.channels.Channel#isOpen()
		 */
		@Override
		public boolean isOpen() {
			return !closed.get();
		}

		/**
		 * @see java.nio.channels.Channel#close()
		 */
		@Override
		public void close() throws IOException {
			closed.set(true);
		}

		private void checkOpen() throws ClosedChannelException {
			if(closed.get())
				throw new ClosedChannelException();
		}

		/**
		 * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
		 */
		@Override
		public int read(ByteBuffer dst) throws IOException {
			if (dst == null)
				throw new NullPointerException("Invalid dst"); //$NON-NLS-1$

			checkOpen();

			return MemoryByteBuffer.this.read(position.get(), dst);
		}

		/**
		 * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
		 */
		@Override
		public int write(ByteBuffer src) throws IOException {
			if (src == null)
				throw new NullPointerException("Invalid src"); //$NON-NLS-1$

			checkOpen();

			return MemoryByteBuffer.this.write(position.get(), src);
		}

		/**
		 * @see java.nio.channels.SeekableByteChannel#position()
		 */
		@Override
		public long position() throws IOException {
			return position.get();
		}

		/**
		 * @see java.nio.channels.SeekableByteChannel#position(long)
		 */
		@Override
		public SeekableByteChannel position(long newPosition) throws IOException {
			if(newPosition<0)
				throw new IllegalArgumentException("Negative position: "+newPosition); //$NON-NLS-1$
			if(newPosition>=INT_LIMIT)
				throw new IllegalArgumentException("Position exceeds integer limit of buffer array: "+newPosition); //$NON-NLS-1$

			checkOpen();

			position.set((int) newPosition);

			return this;
		}

		/**
		 * @see java.nio.channels.SeekableByteChannel#size()
		 */
		@Override
		public long size() throws IOException {
			return MemoryByteBuffer.this.size();
		}

		/**
		 * @see java.nio.channels.SeekableByteChannel#truncate(long)
		 */
		@Override
		public SeekableByteChannel truncate(long size) throws IOException {
			if(size>=INT_LIMIT)
				throw new IllegalArgumentException("Size exceeds integer limit of buffer array: "+size); //$NON-NLS-1$

			MemoryByteBuffer.this.truncate((int) size);

			return this;
		}

	}
}
