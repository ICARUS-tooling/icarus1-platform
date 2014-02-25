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
package de.ims.icarus.util.strings;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CharTableBuffer {

//	public static void main(String[] args) throws Exception {
//
//		File file = new File("D:/Workspaces/Default/Icarus/data/treebanks/CoNLL2009-ST-English-development.txt"); //$NON-NLS-1$
//
//		Reader reader = new BufferedReader(new FileReader(file));
//
//		CharTableBuffer buffer = new CharTableBuffer(100, 200);
//
//		buffer.startReading(reader);
//
//		buffer.next();
//
//		for(int i=0; i<buffer.getRowCount(); i++) {
//			Row row = buffer.getRow(i);
//			System.out.println(row);
//
//			row.split('\t');
//
//			for(int j=0; j<row.getSplitCount(); j++) {
//				Cursor c = row.getSplitCursor(j);
//				System.out.println(c);
//				c.recycle();
//			}
//
//			if(i>0)
//				break;
//		}
//
//		buffer.close();
//	}

	// Data storage
	private Row[] rows;
	private int columns;
	private int height = 0;

	// Data source
	private Reader reader;
	private char[] buffer;
	private boolean ignoreLF = false;

	private RowFilter rowFilter;

	// Cursor cursorCache
	private Stack<Cursor> cursorCache = new Stack<>();
	private Map<String, Pattern> regexCache;

	public CharTableBuffer() {
		this(50, 100);
	}

	public CharTableBuffer(int rows, int columns) {

		this.columns = columns;

		this.rows = new Row[rows];
	}

	public void startReading(Reader reader) throws IOException {
		if (reader == null)
			throw new NullPointerException("Invalid reader"); //$NON-NLS-1$

		reset();

		this.reader = reader;

		if(buffer==null) {
			buffer = new char[4096];
		}

		ignoreLF = false;
	}

	public boolean next() throws IOException {
		if(reader==null)
			throw new IllegalStateException("No reader initialized"); //$NON-NLS-1$

		height = 0;

		//TODO read empty lines till first content or end of stream?

		line_loop: for(;;) {
			Row row = readLine0();

			if(row==null) {
				// End of stream
				truncate();
				break line_loop;
			} else if(rowFilter==null) {
				// If no custom row handling is defined use the first empty row as delimiter
				if(row.length()==0) {
					truncate();
					break line_loop;
				}
			} else {
				// Allow custom filter to decide
				switch (rowFilter.getRowAction(row)) {
				case END_OF_TABLE:
					truncate();
					break line_loop;

				case IGNORE:
					truncate();
					break;

				default:
					break;
				}
			}
		}

		return height>0;
	}

	private void truncate() {
		if(height==0)
			throw new IllegalStateException();

		height--;
	}


	/**
	 * @return the rowFilter
	 */
	public RowFilter getRowFilter() {
		return rowFilter;
	}

	/**
	 * @param rowFilter the rowFilter to set
	 */
	public void setRowFilter(RowFilter rowFilter) {
		this.rowFilter = rowFilter;
	}


	private static final char CR = '\r';
	private static final char LF = '\n';

	/**
	 * Reads characters from the underlying reader until the end of the stream
	 * or a linebreak occurs. Returns the length of that line.
	 */
	private Row readLine0() throws IOException {
		int nextChar = 0;

		boolean eos = false;

		char_loop : for(;;) {
			int c = reader.read();

			switch (c) {
			case -1:
				eos = true;
				break char_loop;

			case CR:
				ignoreLF = true;
				break char_loop;

			case LF:
				if(!ignoreLF)
					break char_loop;
				break;

			default:
				buffer[nextChar++] = (char) c;
				ignoreLF = false;
				break;
			}
		}

		Row row = nextRow();

		row.set(buffer, 0, nextChar);

		return row.isEmpty() && eos ? null : row;
	}

	public void reset() throws IOException {
		if(reader!=null) {
			reader.close();
			reader = null;
		}

		ignoreLF = false;
	}

	public void close() throws IOException {
		reset();
		buffer = null;

		if(rows!=null) {
			for(Row row : rows) {
				if(row!=null) {
					row.close();
				}
			}
			rows = null;
		}
	}

	private Row nextRow() {
		int index = height;

		if(index>=rows.length) {
			rows = Arrays.copyOf(rows, index*2+1);
		}

		Row row = rows[index];
		if(row==null) {
			row = rows[index] = new Row(index, columns);
		}

		height++;

		return row;
	}

	public Row getRow(int index) {
		if(index>=height)
			throw new IndexOutOfBoundsException();

		return rows[index];
	}

	public int getRowCount() {
		return height;
	}

	public boolean isEmpty() {
		return height==0;
	}

	private Cursor getCursor(int row, int index0, int index1) {
		Cursor cursor = null;

		// Check cursorCache
		if(!cursorCache.isEmpty()) {
			cursor = cursorCache.pop();
		}

		// Create new cursor only if required
		if(cursor==null) {
			cursor = new Cursor();
		} else {
			cursor.resetSplits();
		}

		// Now set scope
		cursor.setRow(row);
		cursor.setIndex0(index0);
		cursor.setIndex1(index1);
		cursor.resetHash();

		return cursor;
	}

	private void recycleCursor(Cursor cursor) {
		if (cursor == null)
			throw new NullPointerException("Invalid cursor"); //$NON-NLS-1$

		cursor.closeSplits();
		cursorCache.push(cursor);
	}

	Pattern getPattern(String regex) {
		if (regex == null)
			throw new NullPointerException("Invalid regex"); //$NON-NLS-1$

		if(regexCache==null) {
			regexCache = new HashMap<>();
		}

		Pattern p = regexCache.get(regex);

		if(p==null) {
			p = Pattern.compile(regex);
			regexCache.put(regex, p);
		}

		return p;
	}

	public class Cursor extends Splitable {

		private int row, index0, index1;

		/**
		 * @see java.lang.CharSequence#length()
		 */
		@Override
		public int length() {
			return index1-index0+1;
		}

		/**
		 * @see java.lang.CharSequence#charAt(int)
		 */
		@Override
		public char charAt(int index) {
			if(index>index1-index0)
				throw new IndexOutOfBoundsException();

			return getRow(row).charAt(index0+index);
		}

		private void setRow(int row) {
			this.row = row;
		}

		private void setIndex0(int index0) {
			this.index0 = index0;
		}

		private void setIndex1(int index1) {
			this.index1 = index1;
		}

		public void recycle() {
			row = index0 = index1 = -1;

			recycleCursor(this);
		}

		/**
		 * @see de.ims.icarus.util.strings.AbstractString#subSequence(int, int)
		 */
		@Override
		public Cursor subSequence(int start, int end) {
			return getCursor(row, index0+start, index0+end);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getCachedPattern(java.lang.String)
		 */
		@Override
		protected Pattern getCachedPattern(String regex) {
			return getPattern(regex);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getSplitCursor(int)
		 */
		@Override
		public Cursor getSplitCursor(int index) {
			return (Cursor) super.getSplitCursor(index);
		}
	}

	public class Row extends Splitable {

		// Row pointer
		private final int rowIndex;

		// Data storage
		private char[] buffer;
		private int width = 0;

		private Row(int rowIndex, int size) {
			this.rowIndex = rowIndex;
			buffer = new char[size];
		}

		private void reset() {
			width = 0;
			resetSplits();
			resetHash();
		}

		private void close() {
			reset();
			buffer = null;
			closeSplits();
		}

		private void ensureCapacity(int capacity) {
			if(capacity>=buffer.length) {
				capacity = 2*capacity+1;
				buffer = Arrays.copyOf(buffer, capacity);
			}
		}

		private void append(CharSequence s) {
			int l = s.length();
			ensureCapacity(width+l);

			for(int i=0; i<l;i++) {
				buffer[width++] = s.charAt(i);
			}

			resetHash();
		}

		private void append(char[] c, int offset, int len) {
			ensureCapacity(width+len);

			System.arraycopy(c, offset, buffer, width, len);
			width += len;

			resetHash();
		}

		private void set(char[] c, int offset, int len) {
			ensureCapacity(len);

			System.arraycopy(c, offset, buffer, 0, len);
			width = len;

			resetHash();
		}

		/**
		 * @see java.lang.CharSequence#length()
		 */
		@Override
		public int length() {
			return width;
		}

		/**
		 * @see java.lang.CharSequence#charAt(int)
		 */
		@Override
		public char charAt(int index) {
			if(index>=width)
				throw new IndexOutOfBoundsException();

			return buffer[index];
		}

		/**
		 * @see de.ims.icarus.util.strings.AbstractString#subSequence(int, int)
		 */
		@Override
		public Cursor subSequence(int start, int end) {
			return getCursor(rowIndex, start, end);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getCachedPattern(java.lang.String)
		 */
		@Override
		protected Pattern getCachedPattern(String regex) {
			return getPattern(regex);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getSplitCursor(int)
		 */
		@Override
		public Cursor getSplitCursor(int index) {
			return (Cursor) super.getSplitCursor(index);
		}
	}

	public enum RowAction {
		IGNORE,
		VALID,
		END_OF_TABLE;
	}

	public interface RowFilter {
		RowAction getRowAction(Row row);
	}
}
