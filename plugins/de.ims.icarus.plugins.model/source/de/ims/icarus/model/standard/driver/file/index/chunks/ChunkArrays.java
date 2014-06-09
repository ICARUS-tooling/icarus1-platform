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

import java.nio.ByteBuffer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ChunkArrays {

	public interface ArrayAdapter {

		Object createBuffer(int byteCount);

		int bufferSize(Object buffer);

		void read(Object target, ByteBuffer buffer, int offset, int length);

		void write(Object source, ByteBuffer buffer, int offset, int length);

		int getFileId(Object buffer, int index);

		long getBeginOffset(Object buffer, int index);

		long getEndOffset(Object buffer, int index);

		int setFileId(Object buffer, int index, int fileId);

		long setBeginOffset(Object buffer, int index, long offset);

		long setEndOffset(Object buffer, int index, long offset);

		/**
		 * Returns the byte size of a single entry. This information
		 * is used to create {@code ByteBuffer} objects of adequate size
		 * for reading and writing.
		 * @return
		 */
		int chunkSize();
	}

	public static ArrayAdapter createIntAdapter() {
		return new IntAdapter();
	}

	public static ArrayAdapter createIntFileAdapter() {
		return new IntFileAdapter();
	}

	public static ArrayAdapter createLongAdapter() {
		return new LongAdapter();
	}

	public static ArrayAdapter createLongFileAdapter() {
		return new LongFileAdapter();
	}

	private static class IntAdapter implements ArrayAdapter {

		// begin-offset | end-offset
		private static final int BYTES_PER_ENTRY = 4 + 4;

		private final int deltaBegin;
		private final int deltaEnd;

		private IntAdapter() {
			this(0, 1);
		}

		private IntAdapter(int deltaBegin, int deltaEnd) {
			this.deltaBegin = deltaBegin;
			this.deltaEnd = deltaEnd;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#createBuffer(long)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			int size = byteCount/chunkSize();
			return new int[size];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((int[])buffer).length/2;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset, int length) {
			int[] array = (int[]) target;
			int index = offset*2;
			while(length-->0) {
				array[index++] = buffer.getInt();
				array[index++] = buffer.getInt();
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset, int length) {
			int[] array = (int[]) source;
			int index = offset*2;
			while(length-->0) {
				buffer.putInt(array[index++]);
				buffer.putInt(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
		 */
		@Override
		public int getFileId(Object buffer, int index) {
			return 0;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#getBeginOffset(java.lang.Object, int)
		 */
		@Override
		public long getBeginOffset(Object buffer, int index) {
			return ((int[])buffer)[index*3+deltaBegin];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#getEndOffset(java.lang.Object, int)
		 */
		@Override
		public long getEndOffset(Object buffer, int index) {
			return ((int[])buffer)[index*3+deltaEnd];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
		 */
		@Override
		public int setFileId(Object buffer, int index, int fileId) {
			return 0;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#setBeginOffset(java.lang.Object, long, int)
		 */
		@Override
		public long setBeginOffset(Object buffer, int index, long offset) {
			int[] array = (int[]) buffer;
			index = index*3+deltaBegin;
			int old = array[index];
			array[index] = (int) offset;
			return old;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#setEndOffset(java.lang.Object, long, int)
		 */
		@Override
		public long setEndOffset(Object buffer, int index, long offset) {
			int[] array = (int[]) buffer;
			index = index*3+deltaEnd;
			int old = array[index];
			array[index] = (int) offset;
			return old;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

	}

	private static class IntFileAdapter extends IntAdapter implements ArrayAdapter {

		// file-id | begin-offset | end-offset
		private static final int BYTES_PER_ENTRY = 4 + 4 + 4;

		private IntFileAdapter() {
			super(1, 2);
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((int[])buffer).length/3;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#read(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset, int length) {
			int[] array = (int[]) target;
			int index = offset*3;
			while(length-->0) {
				array[index++] = buffer.getInt();
				array[index++] = buffer.getInt();
				array[index++] = buffer.getInt();
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#write(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset, int length) {
			int[] array = (int[]) source;
			int index = offset*3;
			while(length-->0) {
				buffer.putInt(array[index++]);
				buffer.putInt(array[index++]);
				buffer.putInt(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
		 */
		@Override
		public int getFileId(Object buffer, int index) {
			return ((int[])buffer)[index*3];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
		 */
		@Override
		public int setFileId(Object buffer, int index, int fileId) {
			int[] array = (int[]) buffer;
			index = index*3;
			int old = array[index];
			array[index] = fileId;
			return old;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

	}

	private static class LongAdapter implements ArrayAdapter {

		// begin-offset | end-offset
		private static final int BYTES_PER_ENTRY = 8 + 8;

		private final int deltaBegin;
		private final int deltaEnd;

		private LongAdapter() {
			this(0, 1);
		}

		private LongAdapter(int deltaBegin, int deltaEnd) {
			this.deltaBegin = deltaBegin;
			this.deltaEnd = deltaEnd;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#createBuffer(long)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			int size = byteCount/chunkSize();
			return new long[size];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((long[])buffer).length/2;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.IntAdapter#read(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset, int length) {
			long[] array = (long[]) target;
			int index = offset*2;
			while(length-->0) {
				array[index++] = buffer.getLong();
				array[index++] = buffer.getLong();
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.IntAdapter#write(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset, int length) {
			long[] array = (long[]) source;
			int index = offset*2;
			while(length-->0) {
				buffer.putLong(array[index++]);
				buffer.putLong(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
		 */
		@Override
		public int getFileId(Object buffer, int index) {
			return 0;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#getBeginOffset(java.lang.Object, int)
		 */
		@Override
		public long getBeginOffset(Object buffer, int index) {
			return ((long[])buffer)[index*3+deltaBegin];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#getEndOffset(java.lang.Object, int)
		 */
		@Override
		public long getEndOffset(Object buffer, int index) {
			return ((long[])buffer)[index*3+deltaEnd];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
		 */
		@Override
		public int setFileId(Object buffer, int index, int fileId) {
			return 0;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#setBeginOffset(java.lang.Object, long, int)
		 */
		@Override
		public long setBeginOffset(Object buffer, int index, long offset) {
			long[] array = (long[]) buffer;
			index = index*3+deltaBegin;
			long old = array[index];
			array[index] = offset;
			return old;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#setEndOffset(java.lang.Object, long, int)
		 */
		@Override
		public long setEndOffset(Object buffer, int index, long offset) {
			long[] array = (long[]) buffer;
			index = index*3+deltaEnd;
			long old = array[index];
			array[index] = offset;
			return old;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

	}

	private static class LongFileAdapter extends IntAdapter implements ArrayAdapter {

		// file-id | begin-offset | end-offset
		private static final int BYTES_PER_ENTRY = 4 + 8 + 8;

		private LongFileAdapter() {
			super(1, 2);
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((long[])buffer).length/3;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.IntAdapter#read(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset, int length) {
			long[] array = (long[]) target;
			int index = offset*3;
			while(length-->0) {
				array[index++] = buffer.getInt();
				array[index++] = buffer.getLong();
				array[index++] = buffer.getLong();
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.IntAdapter#write(java.lang.Object, java.nio.ByteBuffer)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset, int length) {
			long[] array = (long[]) source;
			int index = offset*3;
			while(length-->0) {
				buffer.putInt((int) array[index++]);
				buffer.putLong(array[index++]);
				buffer.putLong(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#getFileId(java.lang.Object, int)
		 */
		@Override
		public int getFileId(Object buffer, int index) {
			return (int) ((long[])buffer)[index*3];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.DefaultChunkIndex.ArrayAdapter#setFileId(java.lang.Object, long, int)
		 */
		@Override
		public int setFileId(Object buffer, int index, int fileId) {
			long[] array = (long[]) buffer;
			index = index*3;
			int old = (int) array[index];
			array[index] = fileId;
			return old;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkArrays.ArrayAdapter#maxChunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

	}
}
