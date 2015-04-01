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
package de.ims.icarus.plugins.prosody.io;

import java.io.BufferedReader;
import java.io.IOException;

import de.ims.icarus.util.strings.StringPrimitives;
import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UniSylIOUtils {

	public static UniSylConfig readConfig(UniSylReader reader) throws IOException {
		if(!reader.skipEmptyLines())
			throw new IOException("No header available");

		//TODO

		return null;
	}

	public static class UniSylReader {
		private final BufferedReader reader;
		private String line;
		private int lineNumber;

		public UniSylReader(BufferedReader reader) {
			if (reader == null)
				throw new NullPointerException("Invalid reader"); //$NON-NLS-1$

			this.reader = reader;
		}

		public String readLine() throws IOException {
			line = reader.readLine();
			if(line!=null) {
				lineNumber++;
			}

			return line;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public String getLine() {
			if(line==null)
				throw new IllegalStateException("No last line available"); //$NON-NLS-1$

			return line;
		}

		/**
		 * Skips lines till a non-empty line is found.
		 * Returns {@code true} iff after skipping empty lines there is at least one
		 * non-empty line available (and currently buffered).
		 */
		public boolean skipEmptyLines() throws IOException {
			String line = null;
			while((line = readLine())!=null) {
				if(!line.isEmpty()) {
					break;
				}
			}

			return line!=null;
		}
	}

	public static class UniSylConfig {
		Column[] columns;

		Column[] wordDelimiterColumns;
		Object[] wordDelimiterBuffer;

		Column[] docDelimiterColumns;
	}

	public static class Column {
		ColumnHandler handler;
		AnnotationLevel level;
		String property;
	}

	public static enum AnnotationLevel {
		SYLLABLE("syl"), //$NON-NLS-1$
		WORD("word"), //$NON-NLS-1$
		SENTENCE("sentence"), //$NON-NLS-1$
		DOCUMENT("document"), //$NON-NLS-1$
		;

		private final String key;

		AnnotationLevel(String key) {
			this.key = key;
		}

		public static AnnotationLevel parseLevel(String s) {
			switch (s) {
			case "syl": return SYLLABLE; //$NON-NLS-1$
			case "word": return WORD; //$NON-NLS-1$
			case "sentence": return SENTENCE; //$NON-NLS-1$
			case "document": return DOCUMENT; //$NON-NLS-1$

			default:
				throw new IllegalArgumentException("Unknown annotation level: "+s); //$NON-NLS-1$
			}
		}
	}

	private static final float[] EMPTY_FLOATS = new float[0];
	private static final double[] EMPTY_DOUBLES = new double[0];
	private static final int[] EMPTY_INTS = new int[0];
	private static final String[] EMPTY_STRINGS = new String[0];
	private static final boolean[] EMPTY_BITS = new boolean[0];

	public static enum ColumnHandler {

		INTEGER {
			@Override
			public Object createSylBuffer(int sylCount) {
				return new int[sylCount];
			}

			@Override
			public Object emptySylBuffer() {
				return EMPTY_INTS;
			}

			@Override
			public void parseAndSet(Object buffer, int index, CharSequence s) {
				((int[])buffer)[index] = StringPrimitives.parseInt(s);
			}

			@Override
			public Object parse(CharSequence s) {
				return StringPrimitives.parseInt(s);
			}
		},

		FLOAT {
			@Override
			public Object createSylBuffer(int sylCount) {
				return new float[sylCount];
			}

			@Override
			public Object emptySylBuffer() {
				return EMPTY_FLOATS;
			}

			@Override
			public void parseAndSet(Object buffer, int index, CharSequence s) {
				((float[])buffer)[index] = StringPrimitives.parseFloat(s);
			}

			@Override
			public Object parse(CharSequence s) {
				return StringPrimitives.parseFloat(s);
			}
		},

		DOUBLE {
			@Override
			public Object createSylBuffer(int sylCount) {
				return new double[sylCount];
			}

			@Override
			public Object emptySylBuffer() {
				return EMPTY_DOUBLES;
			}

			@Override
			public void parseAndSet(Object buffer, int index, CharSequence s) {
				((double[])buffer)[index] = StringPrimitives.parseDouble(s);
			}

			@Override
			public Object parse(CharSequence s) {
				return StringPrimitives.parseDouble(s);
			}
		},

		BIT {
			@Override
			public Object createSylBuffer(int sylCount) {
				return new boolean[sylCount];
			}

			@Override
			public Object emptySylBuffer() {
				return EMPTY_BITS;
			}

			@Override
			public void parseAndSet(Object buffer, int index, CharSequence s) {
				((boolean[])buffer)[index] = StringUtil.equals(s, "1"); //$NON-NLS-1$
			}

			@Override
			public Object parse(CharSequence s) {
				return StringUtil.equals(s, "1"); //$NON-NLS-1$
			}
		},

		BOOLEAN {
			@Override
			public Object createSylBuffer(int sylCount) {
				return new boolean[sylCount];
			}

			@Override
			public Object emptySylBuffer() {
				return EMPTY_BITS;
			}

			@Override
			public void parseAndSet(Object buffer, int index, CharSequence s) {
				((boolean[])buffer)[index] = StringPrimitives.parseBoolean(s);
			}

			@Override
			public Object parse(CharSequence s) {
				return StringPrimitives.parseBoolean(s);
			}
		},

		STRING {
			@Override
			public Object createSylBuffer(int sylCount) {
				return new String[sylCount];
			}

			@Override
			public Object emptySylBuffer() {
				return EMPTY_STRINGS;
			}

			@Override
			public void parseAndSet(Object buffer, int index, CharSequence s) {
				((String[])buffer)[index] = StringUtil.intern(s);
			}

			@Override
			public Object parse(CharSequence s) {
				return StringUtil.intern(s);
			}
		},

		;

		public abstract Object createSylBuffer(int sylCount);
		public abstract Object emptySylBuffer();
		public abstract void parseAndSet(Object buffer, int index, CharSequence s);
		public abstract Object parse(CharSequence s);
	}
}
