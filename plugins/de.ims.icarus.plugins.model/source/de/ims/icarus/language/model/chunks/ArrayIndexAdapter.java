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
package de.ims.icarus.language.model.chunks;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ArrayIndexAdapter extends AbstractIndexAdapter {

	private int chunkCount = 0;
	private int chunkCapacity = 0;

	// Fast lookup for offsets within a chunk block
	private final int fileIdDelta;
	private final int beginOffsetDelta;
	private final int endOffsetDelta;
	private final int beginIndexDelta;
	private final int endIndexDelta;

	private Object data;

	private static final int MIN_CHUNK_BUFFER_SIZE = 1000;

	public static int getChunkLimit(ChunkIndex index) {
		if (index == null)
			throw new NullPointerException("Invalid index"); //$NON-NLS-1$

		// Calculate byte size of chunk blocks
		int fieldSize = 2; // 2 int values for byte offsets
		if(index.getFileCount()>1) {
			// Another 4 bytes for the file id
			fieldSize ++;
		}
		if(index.isMappingElements()) {
			// And yet another 2 int values for element indices
			fieldSize += 2;
		}

		return (Integer.MAX_VALUE/fieldSize) - 1;
	}

	public static ArrayIndexAdapter createIntAdapter(ChunkIndex chunkIndex) {
		return new ArrayIndexAdapter(chunkIndex, false);
	}

	public static ArrayIndexAdapter createLongAdapter(ChunkIndex chunkIndex) {
		return new ArrayIndexAdapter(chunkIndex, true);
	}

	/**
	 * @param chunkIndex
	 * @param supportsLargeIndex
	 */
	public ArrayIndexAdapter(ChunkIndex chunkIndex, boolean longIndex) {
		super(chunkIndex, false, longIndex);

		// Init delta values
		fileIdDelta = isReadFileId() ? 0 : -1;
		beginOffsetDelta = fileIdDelta + 1;
		endOffsetDelta = beginOffsetDelta + 1;
		beginIndexDelta = isReadElements() ? endOffsetDelta + 1 : -1;
		endIndexDelta = isReadElements() ? beginIndexDelta + 1 : -1;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.AbstractIndexAdapter#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return data!=null;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.AbstractIndexAdapter#delete()
	 */
	@Override
	protected void delete() {
		data = null;
		chunkCount = 0;
		chunkCapacity = 0;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.AbstractIndexAdapter#writeData(java.nio.channels.SeekableByteChannel, java.lang.Object, boolean, boolean)
	 */
	@Override
	protected void writeData() throws IOException {
		if (data == null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$

		if(Array.getLength(data)==0) {
			return;
		}

		try (SeekableByteChannel channel = getChannel()) {

			ByteBuffer buffer = ByteBuffer.allocate(getBlockSize());

			// Write header
			writeHeader(channel);

			boolean longIndex = isLongIndex();

			for(int i=0; i<getChunkCount(); i++) {

				boolean discard = false;
				int index = i * getFieldSize();

				if(isReadFileId()) {
					discard |= copy(buffer, data, index++, false);
				}
				discard |= copy(buffer, data, index++, longIndex);
				discard |= copy(buffer, data, index++, longIndex);

				if(isReadElements()) {
					// NOTE:
					// This implementation always uses int as storage for
					// element indices, since it assumes other related indices to be
					// based on the same limitations
					discard |= copy(buffer, data, index++, false);
					discard |= copy(buffer, data, index++, false);
				}

				if(discard) {
					break;
				}

				// Write chunk to channel
				if(!buffer.hasRemaining()) {
					buffer.flip();
					channel.write(buffer);

					buffer.clear();
				}
			}
		}
	}

	/**
	 * Returns {@code true} if an invalid value ({@code -1}) occurred.
	 */
	private boolean copy(ByteBuffer buffer, Object array, int index, boolean isLong) {
		long value = Array.getLong(array, index);

		if(value==-1) {
			return true;
		}

		if(isLong) {
			buffer.putLong(value);
		} else {
			buffer.putInt((int) value);
		}

		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.AbstractIndexAdapter#readData(java.nio.channels.SeekableByteChannel, boolean, boolean)
	 */
	@Override
	protected void readData() throws IOException {

		try (SeekableByteChannel channel = getChannel()) {

			long available = channel.size();
			// Number of chunks stored in the index file
			long blockCount = available/getBlockSize();
			if(blockCount>getMaxChunkCount())
				throw new IllegalStateException("Chunk count exceeds storage limit: "+StringUtil.formatDecimal(blockCount)); //$NON-NLS-1$
			if(blockCount*getBlockSize() != available)
				throw new IllegalStateException("Index data corrupted - byte count does not match block size: expected " //$NON-NLS-1$
						+StringUtil.formatDecimal(blockCount*getBlockSize()) + ", got " //$NON-NLS-1$
						+StringUtil.formatDecimal(available));

			checkHeader(channel);

			ByteBuffer buffer = ByteBuffer.allocate(getBlockSize());

			// Number of data fields available in file
			chunkCount = (int) (available/getBlockSize());
			chunkCapacity = Math.max(chunkCount, MIN_CHUNK_BUFFER_SIZE);

			int fieldSize = getFieldSize();
			int blockSize = getBlockSize();

			// Create data storage
			Class<?> compType = isLongIndex() ? long.class : int.class;
			data = Array.newInstance(compType, chunkCapacity * fieldSize);

			// Number of fields to read
			int validFields = chunkCount * fieldSize;

			// Now read in all the fields
			for(int i=0; i<chunkCount; i++) {

				if(channel.read(buffer) != blockSize)
					throw new EOFException("Unexpected end of channel - expected "+(chunkCount-i)+" more chunks"); //$NON-NLS-1$ //$NON-NLS-2$
				buffer.flip();

				int index = i * fieldSize;

				if(isReadFileId()) {
					Array.setInt(data, index++, buffer.getInt());
				}

				if(isLongIndex()) {
					Array.setLong(data, index++, buffer.getLong());
					Array.setLong(data, index++, buffer.getLong());
				} else {
					Array.setInt(data, index++, buffer.getInt());
					Array.setInt(data, index++, buffer.getInt());
				}

				Array.setInt(data, index++, buffer.getInt());
				Array.setInt(data, index++, buffer.getInt());

				buffer.clear();
			}

			// Mark remaining storage as invalid
			for(int i=validFields; i<chunkCapacity; i++) {
				Array.setInt(data, i, -1);
			}
		}
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexReader#getChunkCount()
	 */
	@Override
	public long getChunkCount() {
		return chunkCount;
	}

	private long replace(long chunk, int delta, long value, boolean isLong) {
		if(!isOpen())
			throw new IllegalStateException("Not opened"); //$NON-NLS-1$
		if(chunk>=getMaxChunkCount())
			throw new IllegalArgumentException("Chunk index exceeds limit: "+chunk); //$NON-NLS-1$
		if(!isLong && value>=INT_LIMIT)
			throw new IllegalArgumentException("Value exceeds integer limit: "+value); //$NON-NLS-1$
		if(delta<0)
			throw new UnsupportedOperationException();
		if(chunk>chunkCount)
			throw new IllegalArgumentException("Chunk index out of bounds (including append): "+chunk); //$NON-NLS-1$

		if(chunk>=chunkCapacity) {
			// chunk cannot exceed maxChunkCount at this point
			int oldCapacity = chunkCapacity;
			int newCapacity = Math.max((int)chunk, Math.min(oldCapacity+MIN_CHUNK_BUFFER_SIZE, (int)getMaxChunkCount()));

			// Expand data storage
			if(isLongIndex()) {
				long[] newData = Arrays.copyOf((long[])data, newCapacity*getFieldSize());
				Arrays.fill(newData, oldCapacity, newData.length, -1);
				data = newCapacity;
			} else {
				int[] newData = Arrays.copyOf((int[])data, newCapacity*getFieldSize());
				Arrays.fill(newData, oldCapacity, newData.length, -1);
				data = newCapacity;
			}
			chunkCapacity = newCapacity;
		}

		int index = (int) chunk * getFieldSize() + delta;

		long oldValue = Array.getLong(data, index);
		if(isLong) {
			Array.setLong(data, index, value);
		} else {
			Array.setInt(data, index, (int) value);
		}

		return oldValue;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexWriter#setFileId(long, int)
	 */
	@Override
	public int setFileId(long index, int fileId) {
		return (int) replace(index, fileIdDelta, fileId, false);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexWriter#setBeginOffset(long, long)
	 */
	@Override
	public long setBeginOffset(long index, long offset) {
		return replace(index, beginOffsetDelta, offset, isLongIndex());
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexWriter#setEndOffset(long, long)
	 */
	@Override
	public long setEndOffset(long index, long offset) {
		return replace(index, endOffsetDelta, offset, isLongIndex());
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexWriter#setBeginIndex(long, long)
	 */
	@Override
	public long setBeginIndex(long index, long value) {
		return replace(index, beginIndexDelta, value, false);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexWriter#setEndIndex(long, long)
	 */
	@Override
	public long setEndIndex(long index, long value) {
		return replace(index, endIndexDelta, value, false);
	}

	private long lookup(long chunk, int delta) {
		if(!isOpen())
			throw new IllegalStateException("Not opened"); //$NON-NLS-1$
		if(delta<0)
			throw new UnsupportedOperationException();
		if(chunk>=chunkCount)
			throw new IllegalArgumentException("Chunk index out of bounds: "+chunk); //$NON-NLS-1$

		return Array.getLong(data, (int) chunk * getFieldSize() + delta);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexReader#getFileId(long)
	 */
	@Override
	public int getFileId(long index) {
		return (int) lookup(index, fileIdDelta);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexReader#getBeginOffset(long)
	 */
	@Override
	public long getBeginOffset(long index) {
		return lookup(index, beginOffsetDelta);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexReader#getEndOffset(long)
	 */
	@Override
	public long getEndOffset(long index) {
		return lookup(index, endOffsetDelta);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexReader#getBeginIndex(long)
	 */
	@Override
	public long getBeginIndex(long index) {
		return lookup(index, beginIndexDelta);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexReader#getEndIndex(long)
	 */
	@Override
	public long getEndIndex(long index) {
		return lookup(index, endIndexDelta);
	}
}
