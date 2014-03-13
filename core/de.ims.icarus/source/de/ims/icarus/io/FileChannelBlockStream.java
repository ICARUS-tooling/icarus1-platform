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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FileChannelBlockStream extends InputStream {

	private FileChannel channel;

	private ByteBuffer buffer = ByteBuffer.allocateDirect(8000);

	private long position = -1;
	private long remaining = -1;

	// "end of stream" flag
	private boolean eos = false;

	public FileChannelBlockStream(FileChannel channel) {
		setChannel(channel);
	}

	/**
	 * @return the channel
	 */
	public FileChannel getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(FileChannel channel) {
		if (channel == null)
			throw new NullPointerException("Invalid channel"); //$NON-NLS-1$

		this.channel = channel;
		position = -1;
		remaining = -1;
		eos = false;
	}

	public void reload(long position, long size) {
		if(position<0)
			throw new IllegalArgumentException("Position is negative: "+position); //$NON-NLS-1$
		if(size<0)
			throw new IllegalArgumentException("Size is negative: "+size); //$NON-NLS-1$

		this.position = position;
		this.remaining = size;

		eos = size==0;
		buffer.limit(0);
	}

	private void fillBuffer() throws IOException {
		if(position<0)
			throw new IllegalStateException();

		if(eos || buffer.hasRemaining()) {
			return;
		}

		buffer.clear();
		buffer.limit((int) Math.min(remaining, buffer.capacity()));

		// Make sure we read from the correct position
		channel.position(position);
		// Buffer data
		int bytesRead = channel.read(buffer);
		if(bytesRead<1) {
			eos = true;
		} else {
			remaining -= bytesRead;
			position += bytesRead;
		}
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		fillBuffer();
		return eos ? -1 : buffer.get();
	}

	/**
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		fillBuffer();
		if(eos) {
			return -1;
		}
		// Prevent BufferUnderflowException
		// ByteBuffer.remaining() is guaranteed to return value
		// greater than 0 here since otherwise eos would be true
		len = Math.min(len, buffer.remaining());
		buffer.get(b, off, len);
		return len;
	}

	/**
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		if(position<0)
			throw new IllegalStateException();
		n = Math.min(n, remaining);

		remaining -= n;
		position += n;

		if(remaining==0) {
			eos = true;
		} else {
			buffer.limit(0);
		}

		return n;
	}

	/**
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		fillBuffer();

		return eos ? 0 : buffer.remaining();
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		position = -1;
		remaining = -1;
		eos = true;
	}
}
