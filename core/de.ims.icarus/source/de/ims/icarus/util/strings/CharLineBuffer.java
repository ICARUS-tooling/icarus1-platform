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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CharLineBuffer extends Splitable {

	// Data source
	private Reader reader;
	private char[] buffer;
	private boolean ignoreLF = false;

	private final int bufferSize;
	private int nextChar;

	private int lineNumber;
	private boolean eos;

	// Cursor cursorCache
	private Stack<Cursor> cursorCache = new Stack<>();
	private Map<String, Matcher> regexCache;


	private static final char CR = '\r';
	private static final char LF = '\n';

	public CharLineBuffer() {
		this(8000);
	}

	public CharLineBuffer(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void startReading(Reader reader) throws IOException {
		if (reader == null)
			throw new NullPointerException("Invalid reader"); //$NON-NLS-1$

		reset();

		this.reader = reader;

		if(buffer==null) {
			buffer = new char[bufferSize];
		}

		ignoreLF = false;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public boolean isEndOfStream() {
		return eos;
	}

	public void reset() throws IOException {
		if(reader!=null) {
			reader.close();
			reader = null;
		}

		ignoreLF = false;
		eos = false;
		lineNumber = -1;
	}

	public void close() throws IOException {
		reset();
		buffer = null;

		for(Cursor cursor : cursorCache) {
			cursor.closeSplits();
		}
	}

	/**
	 * Reads characters from the underlying reader until the end of the stream
	 * or a linebreak occurs.
	 */
	public boolean next() throws IOException {
		if(eos) {
			return false;
		}

		nextChar = 0;

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
				if(!ignoreLF) {
					lineNumber++;
					break char_loop;
				}
				break;

			default:
				if(nextChar>=buffer.length) {
					buffer = Arrays.copyOf(buffer, nextChar*2+1);
				}
				buffer[nextChar++] = (char) c;
				ignoreLF = false;
				break;
			}
		}

		return !eos || nextChar>0;
	}

	public boolean nextNonEmptyLine() throws IOException {
		while(isEmpty() && next());
		return !eos;
	}

	public void trim() {
		if(isEmpty()) {
			return;
		}

		int leftShift = 0;
		while(leftShift<nextChar && Character.isWhitespace(buffer[leftShift])) leftShift++;

		if(leftShift>0) {
			nextChar -= leftShift;
			System.arraycopy(buffer, leftShift, buffer, 0, nextChar);
		}

		while(nextChar>0 && Character.isWhitespace(buffer[nextChar-1])) nextChar--;
	}

	private Cursor getCursor0(int index0, int index1) {
		Cursor cursor = null;

		// Check cursorCache
		if(!cursorCache.isEmpty()) {
			cursor = cursorCache.pop();
		}

		// Create new cursor only if required
		if(cursor==null) {
//			System.out.printf("creating new cursor: row=%d from=%d to=%d\n",row, index0, index1);
			cursor = new Cursor();
		} else {
			cursor.resetSplits();
		}

		// Now set scope
		cursor.setIndex0(index0);
		cursor.setIndex1(index1);
		cursor.resetHash();

		return cursor;
	}

	private void recycleCursor0(Cursor cursor) {
		if (cursor == null)
			throw new NullPointerException("Invalid cursor"); //$NON-NLS-1$
		cursorCache.push(cursor);
	}

	private Matcher getMatcher0(String regex, CharSequence input) {
		if (regex == null)
			throw new NullPointerException("Invalid regex"); //$NON-NLS-1$

		if(regexCache==null) {
			regexCache = new HashMap<>();
		}

		Matcher m = regexCache.remove(regex);

		if(m==null) {
//			System.out.println("Compiling pattern: "+regex);
			m = Pattern.compile(regex).matcher(input);

//			regexCache.put(regex, m);
		} else {
			m.reset(input);
		}

		return m;
	}

	private void recycleMatcher0(Matcher matcher) {
		if (matcher == null)
			throw new NullPointerException("Invalid matcher"); //$NON-NLS-1$

		if(regexCache==null) {
			regexCache = new HashMap<>();
		}

		matcher.reset();
		regexCache.put(matcher.pattern().pattern(), matcher);
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return nextChar;
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		if(index<0 || index>=nextChar)
			throw new IndexOutOfBoundsException();

		return buffer[index];
	}

	/**
	 * @see de.ims.icarus.util.strings.Splitable#subSequence(int, int)
	 */
	@Override
	public Cursor subSequence(int begin, int end) {
		return getCursor0(begin, end-1);
	}

	public Cursor subSequence(int begin) {
		return getCursor0(begin, length()-1);
	}

	public String substring(int begin, int end) {
		Cursor c = subSequence(begin, end);
		String s = c.toString();
		c.recycle();
		return s;
	}

	public String substring(int begin) {
		Cursor c = subSequence(begin);
		String s = c.toString();
		c.recycle();
		return s;
	}

	/**
	 * @see de.ims.icarus.util.strings.Splitable#recycle()
	 */
	@Override
	public void recycle() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.strings.Splitable#getCachedMatcher(java.lang.String)
	 */
	@Override
	protected Matcher getCachedMatcher(String regex) {
		return getMatcher0(regex, this);
	}

	/**
	 * @see de.ims.icarus.util.strings.Splitable#recycleMatcher(java.util.regex.Matcher)
	 */
	@Override
	protected void recycleMatcher(Matcher matcher) {
		recycleMatcher0(matcher);
	}

	public class Cursor extends Splitable {

		private int index0, index1;

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

			return buffer[index0+index];
		}

		private void setIndex0(int index0) {
			this.index0 = index0;
		}

		private void setIndex1(int index1) {
			this.index1 = index1;
		}

		@Override
		public void recycle() {
			index0 = index1 = -1;

			recycleCursor0(this);
		}

		/**
		 * @see de.ims.icarus.util.strings.AbstractString#subSequence(int, int)
		 */
		@Override
		public Cursor subSequence(int start, int end) {
			return getCursor0(index0+start, index0+end-1);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getCachedMatcher(java.lang.String)
		 */
		@Override
		protected Matcher getCachedMatcher(String regex) {
			return getMatcher0(regex, this);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#recycleMatcher(java.util.regex.Matcher)
		 */
		@Override
		protected void recycleMatcher(Matcher matcher) {
			recycleMatcher0(matcher);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getSplitCursor(int)
		 */
		@Override
		public Cursor getSplitCursor(int index) {
			return (Cursor) super.getSplitCursor(index);
		}
	}
}
