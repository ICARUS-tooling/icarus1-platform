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

import java.nio.ByteBuffer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SpanArrays {

	public interface SpanAdapter {

		Object createBuffer(int byteCount);

		int bufferSize(Object buffer);

		long getFrom(Object buffer, int index);

		long getTo(Object buffer, int index);

		long setFrom(Object buffer, int index, long value);

		long setTo(Object buffer, int index, long value);

		void read(Object target, ByteBuffer buffer, int offset, int length);

		void write(Object source, ByteBuffer buffer, int offset, int length);

		int find(Object source, int from, int to, long value);

		/**
		 * Returns the byte size of a single entry. This information
		 * is used to create {@code ByteBuffer} objects of adequate size
		 * for reading and writing.
		 * @return
		 */
		int chunkSize();
	}

	public static SpanAdapter createIntSpanAdapter() {
		return new IntSpanAdapter();
	}

	public static SpanAdapter createLongSpanAdapter() {
		return new LongSpanAdapter();
	}

	public static SpanAdapter createSpanAdapter(boolean largeIndex) {
		return largeIndex ? new LongSpanAdapter() : new IntSpanAdapter();
	}

	private static class IntSpanAdapter implements SpanAdapter {

		// from | to
		private static final int BYTES_PER_ENTRY = 4 + 4;

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			int size = byteCount/chunkSize();
			return new int[size];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((int[])buffer).length>>1;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#getFrom(int)
		 */
		@Override
		public long getFrom(Object buffer, int index) {
			return ((int[])buffer)[index<<1];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#getTo(int)
		 */
		@Override
		public long getTo(Object buffer, int index) {
			return ((int[])buffer)[(index<<1)+1];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#setFrom(int, long)
		 */
		@Override
		public long setFrom(Object buffer, int index, long value) {
			int[] array = (int[]) buffer;
			int idx = index<<1;

			long oldValue = array[idx];
			array[idx] = (int) value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#setTo(int, long)
		 */
		@Override
		public long setTo(Object buffer, int index, long value) {
			int[] array = (int[]) buffer;
			int idx = (index<<1) + 1;

			long oldValue = array[idx];
			array[idx] = (int) value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#read(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			int[] array = (int[]) target;
			int index = offset<<1;
			while(length-->0) {
				array[index++] = buffer.getInt();
				array[index++] = buffer.getInt();
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#write(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			int[] array = (int[]) source;
			int index = offset<<1;
			while(length-->0) {
				buffer.putInt(array[index++]);
				buffer.putInt(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#find(java.lang.Object, int, int, long)
		 */
		@Override
		public int find(Object source, int from, int to, long value) {
			int[] array = (int[]) source;

			int index = (int) value;

	        int low = from<<1;
	        int high = to<<1;

	        while (low <= high) {
	            int mid = (low + high) >>> 1;

	            if (array[mid]>=index)
	            	// Continue on left area
	            	high = mid - 2;
	            else if (array[mid+1]<=index)
	            	// Continue on right area
	            	low = mid + 2;
	            else
	                return mid>>>1; // span found
	        }

	        return -1;  // span not found.
		}

	}

	private static class LongSpanAdapter implements SpanAdapter {

		// from | to
		private static final int BYTES_PER_ENTRY = 8 + 8;

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#createBuffer(int)
		 */
		@Override
		public Object createBuffer(int byteCount) {
			int size = byteCount/chunkSize();
			return new long[size];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#bufferSize(java.lang.Object)
		 */
		@Override
		public int bufferSize(Object buffer) {
			return ((long[])buffer).length>>1;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#getFrom(int)
		 */
		@Override
		public long getFrom(Object buffer, int index) {
			return ((long[])buffer)[index<<1];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#getTo(int)
		 */
		@Override
		public long getTo(Object buffer, int index) {
			return ((long[])buffer)[(index<<1)+1];
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#setFrom(int, long)
		 */
		@Override
		public long setFrom(Object buffer, int index, long value) {
			long[] array = (long[]) buffer;
			int idx = index<<1;

			long oldValue = array[idx];
			array[idx] = value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#setTo(int, long)
		 */
		@Override
		public long setTo(Object buffer, int index, long value) {
			long[] array = (long[]) buffer;
			int idx = (index<<1) + 1;

			long oldValue = array[idx];
			array[idx] = value;

			return oldValue;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#chunkSize()
		 */
		@Override
		public int chunkSize() {
			return BYTES_PER_ENTRY;
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#read(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void read(Object target, ByteBuffer buffer, int offset,
				int length) {
			long[] array = (long[]) target;
			int index = offset<<1;
			while(length-->0) {
				array[index++] = buffer.getLong();
				array[index++] = buffer.getLong();
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#write(java.lang.Object, java.nio.ByteBuffer, int, int)
		 */
		@Override
		public void write(Object source, ByteBuffer buffer, int offset,
				int length) {
			long[] array = (long[]) source;
			int index = offset<<1;
			while(length-->0) {
				buffer.putLong(array[index++]);
				buffer.putLong(array[index++]);
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.driver.file.index.SpanArrays.SpanAdapter#find(java.lang.Object, int, int, long)
		 */
		@Override
		public int find(Object source, int from, int to, long value) {
			long[] array = (long[]) source;

			long index = (int) value;

	        int low = from<<1;
	        int high = to<<1;

	        while (low <= high) {
	            int mid = (low + high) >>> 1;

	            if (array[mid]>=index)
	            	// Continue on left area
	            	high = mid - 2;
	            else if (array[mid+1]<=index)
	            	// Continue on right area
	            	low = mid + 2;
	            else
	                return mid>>>1; // span found
	        }

	        return -1;  // span not found.
		}
	}
}
