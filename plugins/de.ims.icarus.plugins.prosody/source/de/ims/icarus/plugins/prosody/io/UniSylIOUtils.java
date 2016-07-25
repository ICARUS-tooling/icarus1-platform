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

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import de.ims.icarus.util.Options;
import de.ims.icarus.util.strings.CharLineBuffer;
import de.ims.icarus.util.strings.Splitable;
import de.ims.icarus.util.strings.StringPrimitives;
import de.ims.icarus.util.strings.StringUtil;

/**
 * Header format:
 * Each header line must begin with a hash sign '#', followed by the property assignment in the form
 * of <key>=<value> (everything after the equality sign '=' is considered part of the property value!).
 * <p>
 * <b>Header Property Keys</b>
 * <table border="1">
 * <tr><th>Key</th><th>Description</th></tr>
 * <tr><td>columns</td><td>column format specification, see below</td></tr>
 * <tr><td>separator</td><td>regular expression used to split columns in the data</td></tr>
 * <tr><td>documentBegin</td><td>line prefix that indicates beginning of a new document</td></tr>
 * <tr><td>documentEnd</td><td>line prefix that indicates the end of the current document</td></tr>
 * <tr><td>sentenceBegin</td><td>line prefix that indicates the beginning of a new sentence</td></tr>
 * <tr><td>sentenceEnd</td><td>line prefix that indicates the end of the current sentence</td></tr>
 * </table>
 * <p>
 * <b>Column Properties</b>
 * <table border="1">
 * <tr><th>Position</th><th>Property</th><th>Description</th></tr>
 * <tr><td>1</td><td>key</td><td>key that should be used to save the content of the column on the respective member</td></tr>
 * <tr><td>2</td><td>type</td><td>string form of the column's {@link ColumnType type}</td></tr>
 * <tr><td>3</td><td>level</td><td>{@link AnnotationLevel level} within the document set the content of the column is to be assigned to</td></tr>
 * <tr><td>(4)</td><td>role</td><td>optional hint on whether the content of the column is meant to be used as a {@link #ROLE_DELIMITER delimiter}
 * or whether it contains {@link #ROLE_AGGREGATE aggregated} data</td></tr>
 * <tr><td>(5)</td><td>separator</td><td>optional info on what separator is to be used in case the column is marked to contain
 * {@link #ROLE_AGGREGATE aggregated} data (the default separator is the pipe character '|')</td></tr>
 * </table>
 * <p>
 * <b>Constants</b>
 * <table border="1">
 * <tr><th></th><th></th></tr>
 * <tr><td></td><td></td></tr>
 * </table>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UniSylIOUtils {

//	public static void main(String[] args) throws Exception {
//		String path = "D:\\Workspaces\\Default\\Icarus\\data\\prosody\\all-feats-utf8-header.txt"; //$NON-NLS-1$
//
//		Path file = Paths.get(path);
//
//		try(Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
//			CharLineBuffer buffer = new CharLineBuffer(1000);
//
//			buffer.startReading(reader);
//
//			UniSylConfig config = readConfig(buffer);
//
//			System.out.println("config parsed"); //$NON-NLS-1$
//		}
//	}

	public static final String KEY_COLUMNS = "columns"; //$NON-NLS-1$
	public static final String KEY_DOCUMENT_BEGIN = "documentBegin"; //$NON-NLS-1$
	public static final String KEY_DOCUMENT_END = "documentEnd"; //$NON-NLS-1$
	public static final String KEY_SENTENCE_BEGIN = "sentenceBegin"; //$NON-NLS-1$
	public static final String KEY_SENTENCE_END = "sentenceEnd"; //$NON-NLS-1$
	public static final String KEY_WORD_BEGIN = "wordBegin"; //$NON-NLS-1$
	public static final String KEY_WORD_END = "wordEnd"; //$NON-NLS-1$
	public static final String KEY_SEPARATOR = "separator"; //$NON-NLS-1$

	public static final String KEY_SCHEME = "scheme"; //$NON-NLS-1$

	// Special flags
	public static final String KEY_SKIP_EMPTY_LINES = "skipEmptyLines"; //$NON-NLS-1$
	public static final String KEY_SYLLABLE_OFFSETS_FROM_SAMPA = "syllableOffsetsFromSampa"; //$NON-NLS-1$
	public static final String KEY_LOCAL_SAMPA_RULES_FILE = "localSampaRulesFile"; //$NON-NLS-1$
	public static final String KEY_MARK_ACCENT_ON_WORDS = "markAccentOnWords"; //$NON-NLS-1$
	public static final String KEY_ONLY_CONSIDER_STRESSED_SYLLABLES = "onlyConsiderStressedSylables"; //$NON-NLS-1$
	public static final String KEY_ACCENT_EXCURSION = "accentExcursion"; //$NON-NLS-1$
	public static final String KEY_EMPTY_CONTENT = "emptyContent"; //$NON-NLS-1$
	public static final String KEY_ADJUST_DEPENDENCY_HEADS = "adjustDependencyHeads"; //$NON-NLS-1$
	public static final String KEY_CREATE_COREF_STRUCTURE = "createCorefStructure"; //$NON-NLS-1$
	public static final String KEY_COREF_PROPERTY_KEY = "corefPropertyKey"; //$NON-NLS-1$
	public static final String KEY_DECODE_FESTIVAL_UMLAUTS = "decodeFestivalUmlauts"; //$NON-NLS-1$
	public static final String KEY_IGNORE_COLUMN_COUNT_MISMATCH = "ignoreColumnCountMismatch"; //$NON-NLS-1$

	public static final String ROLE_DELIMITER = "DEL"; //$NON-NLS-1$
	public static final String ROLE_AGGREGATE = "AGG"; //$NON-NLS-1$

	public static final String CONST_TAB = "TAB"; //$NON-NLS-1$
	public static final String CONST_SPACE = "SPACE"; //$NON-NLS-1$
	public static final String CONST_NEWLINE = "NEWLINE"; //$NON-NLS-1$
	public static final String CONST_NEWLINES = "NEWLINES"; //$NON-NLS-1$
	public static final String CONST_WHITESPACE = "WHITESPACE"; //$NON-NLS-1$

	public static String resolveConstant(String s) {
		switch (s) {
		case CONST_NEWLINE:
			return "\r\n|\n"; //$NON-NLS-1$
		case CONST_NEWLINES:
			return "[\n\r]+"; //$NON-NLS-1$
		case CONST_SPACE:
			return " "; //$NON-NLS-1$
		case CONST_TAB:
			return "\t"; //$NON-NLS-1$
		case CONST_WHITESPACE:
			return "\\s+"; //$NON-NLS-1$

		default:
			return s;
		}
	}

	/**
	 * Reads in the header area of a UniSyl file and places the line cursor at the
	 * first line after the header. The header area is a collection of lines that all
	 * start with a hash sign '#' and are not separated by empty lines.
	 *
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public static UniSylConfig readConfig(CharLineBuffer buffer) throws IOException {
		if(!buffer.nextNonEmptyLine())
			throw new IOException("No header available"); //$NON-NLS-1$

		Options options = new Options();

		// Read in header
		while(buffer.startsWith("#")) { //$NON-NLS-1$
			int sepIdx = buffer.indexOf('=');
			if(sepIdx!=-1) {
				String key = buffer.substring(1, sepIdx);
				String value = buffer.substring(sepIdx+1);

				options.put(key, value);
			}

			if(!buffer.next()) {
				break;
			}
		}

		UniSylConfig config = new UniSylConfig();

		config.columns = parseColumns(options.getString(KEY_COLUMNS));

		config.separator = options.getString(KEY_SEPARATOR);
		if(config.separator==null) {
			config.separator = CONST_TAB;
		}

		// First check for line based delimiters

		String val;

		// Word level delimiters
		if((val=options.getString(KEY_WORD_BEGIN))!=null) {
			addDelimiter(config, AnnotationLevel.WORD, newLineDelimiter(val, BEGIN_ELEMENT, 0), true);
		}
		if((val=options.getString(KEY_WORD_END))!=null) {
			addDelimiter(config, AnnotationLevel.WORD, newLineDelimiter(val, END_ELEMENT, SAME_ELEMENT), true);
		}

		// Sentence level delimiters
		if((val=options.getString(KEY_SENTENCE_BEGIN))!=null) {
			addDelimiter(config, AnnotationLevel.SENTENCE, newLineDelimiter(val, BEGIN_ELEMENT, 0), true);
		}
		if((val=options.getString(KEY_SENTENCE_END))!=null) {
			addDelimiter(config, AnnotationLevel.SENTENCE, newLineDelimiter(val, END_ELEMENT, SAME_ELEMENT), true);
		}

		// Document level delimiters
		if((val=options.getString(KEY_DOCUMENT_BEGIN))!=null) {
			addDelimiter(config, AnnotationLevel.DOCUMENT, newLineDelimiter(val, BEGIN_ELEMENT, 0), true);
		}
		if((val=options.getString(KEY_DOCUMENT_END))!=null) {
			addDelimiter(config, AnnotationLevel.DOCUMENT, newLineDelimiter(val, END_ELEMENT, SAME_ELEMENT), true);
		}

		// Flags
		config.skipEmptyLines = options.getBoolean(KEY_SKIP_EMPTY_LINES);
		config.syllableOffsetsFromSampa = options.getBoolean(KEY_SYLLABLE_OFFSETS_FROM_SAMPA);
		config.markAccentOnWords = options.getBoolean(KEY_MARK_ACCENT_ON_WORDS);
		config.onlyConsiderStressedSylables = options.getBoolean(KEY_ONLY_CONSIDER_STRESSED_SYLLABLES);
		config.accentExcursion = options.getInteger(KEY_ACCENT_EXCURSION);
		config.localSampaRulesFile = options.getString(KEY_LOCAL_SAMPA_RULES_FILE);
		config.emptyContent = options.getString(KEY_EMPTY_CONTENT);
		config.adjustDependencyHeads = options.getBoolean(KEY_ADJUST_DEPENDENCY_HEADS);
		config.createCorefStructure = options.getBoolean(KEY_CREATE_COREF_STRUCTURE);
		config.corefPropertyKey = options.getString(KEY_COREF_PROPERTY_KEY);
		config.decodeFestivalUmlauts = options.getBoolean(KEY_DECODE_FESTIVAL_UMLAUTS);
		config.ignoreColumnCountMismatch = options.getBoolean(KEY_IGNORE_COLUMN_COUNT_MISMATCH);

		if(config.createCorefStructure && config.corefPropertyKey==null) {
			config.corefPropertyKey = "coref"; //$NON-NLS-1$
		}

		// Now check for column based delimiters and get lineLevel
		AnnotationLevel lineLevel = AnnotationLevel.DOCUMENT_SET;

		for(Column column : config.columns) {
			if(column.ignore()) {
				continue;
			}

			if(column.isDelimiter()) {
				addDelimiter(config, column.level, new PropertyDelimiter(column.index), false);
			}

			if(column.level.compareTo(lineLevel)>0) {
				lineLevel = column.level;
			}

			if(column.isAggregator() && column.separator==null) {
				column.separator = "\\|"; //$NON-NLS-1$
			}
		}

		config.lineLevel = lineLevel;

		return config;
	}

	private static final Column[] NO_COLS = {};

	private static Column[] parseColumns(String s) {
		if(s==null)
			throw new NullPointerException("No columns spec string provided"); //$NON-NLS-1$

		List<Column> columns = new ArrayList<>();

		Column col = null;
		boolean escaped = false;
		int pos = -1;
		StringBuilder sb = new StringBuilder();

		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);

			if(escaped) {
				sb.append(c);
				continue;
			}

			switch (c) {
			case '\\':
				escaped = true;
				break;

			case '[':
				col = new Column(columns.size());
				sb.setLength(0);
				pos = 0;
				break;

			case ']':
				checkCol(col);
				columns.add(col);
				setField(col, pos, sb.toString());
				sb.setLength(0);
				col = null;
				break;

			case ',':
				setField(col, pos++, sb.toString());
				sb.setLength(0);
				break;

			default:
				sb.append(c);
				break;
			}
		}

		if(columns.isEmpty())
			throw new IllegalArgumentException("No colums specified: "+s); //$NON-NLS-1$

		return columns.toArray(NO_COLS);
	}

	private static void checkCol(Column column) {
		if(column.hasRole() && column.level==AnnotationLevel.SYLLABLE)
			throw new IllegalStateException("Column on syllable level cannot have special roles assigned to them: "+column.property); //$NON-NLS-1$
		if(column.isAggregator() && column.level!=AnnotationLevel.WORD)
			throw new IllegalStateException("Can only aggregate from syllable to word level: "+column.property); //$NON-NLS-1$
	}

	private static void setField(Column col, int pos, String s) {
		if(s==null || s.isEmpty()) {
			return;
		}

		switch (pos) {
		case 0:
			col.property = s;
			break;

		case 1:
			col.type = ColumnType.parseType(s);
			break;

		case 2:
			col.level = AnnotationLevel.parseLevel(s);
			break;

		case 3:
			col.role = s;
			break;

		case 4:
			col.separator = s;
			break;

		default:
			throw new IllegalArgumentException("Invalid field pos: "+pos); //$NON-NLS-1$
		}
	}

	private static Delimiter newLineDelimiter(String s, int hitResult, int missResult) {
		switch (s) {
		case CONST_NEWLINE:
			return new EmptyLineDelimiter(hitResult, missResult);

		case CONST_NEWLINES:
			return new EmptyLineDelimiter(hitResult, missResult, true);

		default:
			return new LinePrefixDelimiter(s, hitResult, missResult);
		}
	}

	private static void addDelimiter(UniSylConfig config, AnnotationLevel level, Delimiter delimiter, boolean isRaw) {
		//TODO check if a delimiter is present and merge if required

		EnumMap<AnnotationLevel, Delimiter> delimiters = isRaw ? config.rawDelimiters : config.colDelimiters;

		Delimiter presentDelimiter = delimiters.get(level);

		if(presentDelimiter instanceof CompoundDelimiter) {
			((CompoundDelimiter)presentDelimiter).addDelimiter(delimiter);
		} else if(presentDelimiter!=null) {
			CompoundDelimiter compoundDelimiter = new CompoundDelimiter();
			compoundDelimiter.addDelimiter(presentDelimiter);
			compoundDelimiter.addDelimiter(delimiter);
			delimiters.put(level, compoundDelimiter);
		} else {
			delimiters.put(level, delimiter);
		}
	}

// 	public static class UniSylReader {
//		private final BufferedReader reader;
//		private String line;
//		private int lineNumber;
//
//		public UniSylReader(BufferedReader reader) {
//			if (reader == null)
//				throw new NullPointerException("Invalid reader"); //$NON-NLS-1$
//
//			this.reader = reader;
//		}
//
//		public String readLine() throws IOException {
//			line = reader.readLine();
//			if(line!=null) {
//				lineNumber++;
//			}
//
//			return line;
//		}
//
//		public int getLineNumber() {
//			return lineNumber;
//		}
//
//		public String getLine() {
//			if(line==null)
//				throw new IllegalStateException("No last line available"); //$NON-NLS-1$
//
//			return line;
//		}
//
//		/**
//		 * Skips lines till a non-empty line is found.
//		 * Returns {@code true} iff after skipping empty lines there is at least one
//		 * non-empty line available (and currently buffered).
//		 */
//		public boolean skipEmptyLines() throws IOException {
//			String line = null;
//			while((line = readLine())!=null) {
//				if(!line.isEmpty()) {
//					break;
//				}
//			}
//
//			return line!=null;
//		}
//	}

	public static class UniSylConfig {

		// Column specs
		Column[] columns;

		// Column separator
		String separator;

		AnnotationLevel lineLevel;

		// General delimiters
		EnumMap<AnnotationLevel, Delimiter> rawDelimiters = new EnumMap<>(AnnotationLevel.class);

		// Delimiters using preprocessed data
		EnumMap<AnnotationLevel, Delimiter> colDelimiters = new EnumMap<>(AnnotationLevel.class);

		boolean skipEmptyLines;
		boolean syllableOffsetsFromSampa;
		boolean markAccentOnWords = false;
		boolean onlyConsiderStressedSylables = false;
		int accentExcursion = -1;
		boolean adjustDependencyHeads = false;
		boolean createCorefStructure = false;
		String corefPropertyKey;
		boolean decodeFestivalUmlauts = false;
		boolean ignoreColumnCountMismatch = false;

		String localSampaRulesFile;

		String emptyContent;
	}

	public static class Column {
		final int index;

		public Column(int index) {
			this.index = index;
		}

		ColumnType type;
		AnnotationLevel level;
		String property;
		String role;
		String separator;

		public boolean ignore() {
			return property==null;
		}

		public boolean hasRole() {
			return role!=null;
		}

		public boolean isDelimiter() {
			return role!=null && ROLE_DELIMITER.equals(role);
		}

		public boolean isAggregator() {
			return role!=null && ROLE_AGGREGATE.equals(role);
		}

		@Override
		public String toString() {
			return new StringBuilder()
			.append("Column@[index=").append(index).append(", type=").append(type) //$NON-NLS-1$ //$NON-NLS-2$
			.append(", level=").append(level).append(", property=").append(property) //$NON-NLS-1$ //$NON-NLS-2$
			.append(", role=").append(role).append(']').toString(); //$NON-NLS-1$
		}
	}

	public interface Delimiter {
		void reset();

		int checkLine(CharLineBuffer line);
	}

	public static final int BEGIN_ELEMENT = 1<<0;
	public static final int END_ELEMENT = 1<<1;
	public static final int SAME_ELEMENT = 1<<2;
	public static final int IGNORE_NEWLINES = 1<<3;

	public static boolean isBeginElement(int delimiterResult) {
		return (delimiterResult & BEGIN_ELEMENT) == BEGIN_ELEMENT;
	}

	public static boolean isEndElement(int delimiterResult) {
		return (delimiterResult & END_ELEMENT) == END_ELEMENT;
	}

	public static boolean isSameElement(int delimiterResult) {
		return (delimiterResult & SAME_ELEMENT) == SAME_ELEMENT;
	}

	public static boolean isIgnoreNewlines(int delimiterResult) {
		return (delimiterResult & IGNORE_NEWLINES) == IGNORE_NEWLINES;
	}

	public static class LinePrefixDelimiter implements Delimiter {

		private final String prefix;
		private final int hitResult, missResult;

		public LinePrefixDelimiter(String prefix, int hitResult, int missResult) {
			this.prefix = prefix;
			this.hitResult = hitResult;
			this.missResult = missResult;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Delimiter#checkLine(de.ims.icarus.util.strings.CharLineBuffer)
		 */
		@Override
		public int checkLine(CharLineBuffer line) {
			return line.startsWith(prefix) ? hitResult : missResult;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Delimiter#reset()
		 */
		@Override
		public void reset() {
			// no-op
		}

	}

	public static abstract class ComparingDelimiter implements Delimiter {

		private String lastValue;

		@Override
		public int checkLine(CharLineBuffer line) {
			String value = getValueIfNew(line, lastValue);

			int result = SAME_ELEMENT;

			boolean maybeEnd = lastValue!=null;

			if(value!=null) {
				lastValue = value;
				result |= BEGIN_ELEMENT;

				if(maybeEnd) {
					result |= END_ELEMENT;
				}
			}

			return result;
		}

		protected abstract String getValueIfNew(CharLineBuffer line, String lastValue);

		/**
		 * @see de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Delimiter#reset()
		 */
		@Override
		public void reset() {
			lastValue = null;
		}
	}

	public static class PropertyDelimiter extends ComparingDelimiter {

		private final int columnIndex;

		public PropertyDelimiter(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.UniSylIOUtils.ComparingDelimiter#getValueIfNew(de.ims.icarus.util.strings.CharLineBuffer, java.lang.String)
		 */
		@Override
		protected String getValueIfNew(CharLineBuffer line, String lastValue) {
			Splitable cursor = line.getSplitCursor(columnIndex);

			String value = null;

			if(lastValue==null || !StringUtil.equals(cursor, lastValue)) {
				value = cursor.toString();
				cursor.recycle();
			}

			return value;
		}

	}

	public static class CompoundDelimiter implements Delimiter {
		private final List<Delimiter> delimiters = new ArrayList<>();

		/**
		 * @see de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Delimiter#checkLine(de.ims.icarus.util.strings.CharLineBuffer)
		 */
		@Override
		public int checkLine(CharLineBuffer line) {

			int result = 0;

			for(Delimiter delimiter : delimiters) {
				result |= delimiter.checkLine(line);

				if(isSameElement(result)) {
					break;
				}
			}

			return result;
		}

		public void addDelimiter(Delimiter delimiter) {
			delimiters.add(delimiter);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Delimiter#reset()
		 */
		@Override
		public void reset() {
			for(Delimiter delimiter : delimiters) {
				delimiter.reset();
			}
		}
	}

	public static class EmptyLineDelimiter implements Delimiter {

		private final int hitResult, missResult;
		private final boolean multiline;

		public EmptyLineDelimiter(int hitResult, int missResult) {
			this(hitResult, missResult, false);
		}

		public EmptyLineDelimiter(int hitResult, int missResult, boolean multiline) {
			this.hitResult = hitResult;
			this.missResult = missResult;
			this.multiline = multiline;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Delimiter#checkLine(de.ims.icarus.util.strings.CharLineBuffer)
		 */
		@Override
		public int checkLine(CharLineBuffer line) {

			if(!line.isEmpty()) {
				return missResult;
			}

			if(multiline) {
				try {
					line.nextNonEmptyLine();
				} catch (IOException e) {
					throw new IllegalStateException("Unable to skip over empty lines"); //$NON-NLS-1$
				}
			}

			return hitResult;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Delimiter#reset()
		 */
		@Override
		public void reset() {
			// no-op
		}
	}

	public static enum AnnotationLevel {
		DOCUMENT_SET("document-set"), //$NON-NLS-1$
		DOCUMENT("document"), //$NON-NLS-1$
		SENTENCE("sentence"), //$NON-NLS-1$
		WORD("word"), //$NON-NLS-1$
		SYLLABLE("syl"), //$NON-NLS-1$
		;

		private final String key;

		AnnotationLevel(String key) {
			this.key = key;
		}

		public static AnnotationLevel parseLevel(String s) {
			switch (s) {
			case "syl": return SYLLABLE; //$NON-NLS-1$
			case "word": return WORD; //$NON-NLS-1$
			case "sentence": case "sent": return SENTENCE; //$NON-NLS-1$ //$NON-NLS-2$
			case "document": case "doc": return DOCUMENT; //$NON-NLS-1$ //$NON-NLS-2$
			case "document-set": return DOCUMENT_SET; //$NON-NLS-1$

			default:
				throw new IllegalArgumentException("Unknown annotation level: "+s); //$NON-NLS-1$
			}
		}

		public String getKey() {
			return key;
		}
	}

	private static final float[] EMPTY_FLOATS = new float[0];
	private static final double[] EMPTY_DOUBLES = new double[0];
	private static final int[] EMPTY_INTS = new int[0];
	private static final String[] EMPTY_STRINGS = new String[0];
	private static final boolean[] EMPTY_BITS = new boolean[0];

	public static enum ColumnType {

		INTEGER {
			@Override
			public Object createSylBuffer(int sylCount) {
				return sylCount==0 ? EMPTY_INTS : new int[sylCount];
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

			@Override
			public int bufferSize(Object buffer) {
				return ((int[][])buffer).length;
			}

			@Override
			public Object emptyValue() {
				return Integer.valueOf(-1);
			}
		},

		FLOAT {
			@Override
			public Object createSylBuffer(int sylCount) {
				return sylCount==0 ? EMPTY_FLOATS : new float[sylCount];
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

			@Override
			public int bufferSize(Object buffer) {
				return ((float[][])buffer).length;
			}

			@Override
			public Object emptyValue() {
				return Float.valueOf(0f);
			}
		},

		DOUBLE {
			@Override
			public Object createSylBuffer(int sylCount) {
				return sylCount==0 ? EMPTY_DOUBLES : new double[sylCount];
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

			@Override
			public int bufferSize(Object buffer) {
				return ((double[])buffer).length;
			}

			@Override
			public Object emptyValue() {
				return Double.valueOf(0d);
			}
		},

		BIT {
			@Override
			public Object createSylBuffer(int sylCount) {
				return sylCount==0 ? EMPTY_BITS : new boolean[sylCount];
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

			@Override
			public int bufferSize(Object buffer) {
				return ((boolean[])buffer).length;
			}

			@Override
			public Object emptyValue() {
				return Boolean.valueOf(false);
			}
		},

		BOOLEAN {
			@Override
			public Object createSylBuffer(int sylCount) {
				return sylCount==0 ? EMPTY_BITS : new boolean[sylCount];
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

			@Override
			public int bufferSize(Object buffer) {
				return ((boolean[])buffer).length;
			}

			@Override
			public Object emptyValue() {
				return Boolean.valueOf(false);
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

			@Override
			public int bufferSize(Object buffer) {
				return ((String[])buffer).length;
			}

			@Override
			public Object emptyValue() {
				return ""; //$NON-NLS-1$
			}
		},

		;

		public abstract Object createSylBuffer(int sylCount);
		public abstract Object emptySylBuffer();
		public abstract int bufferSize(Object buffer);
		public abstract void parseAndSet(Object buffer, int index, CharSequence s);
		public abstract Object parse(CharSequence s);
		public abstract Object emptyValue();

		public Object cloneBuffer(Object buffer) {
			int size = bufferSize(buffer);

			if(size==0) {
				return emptySylBuffer();
			} else {
				Object newBuffer = createSylBuffer(size);
				System.arraycopy(buffer, 0, newBuffer, 0, size);
				return newBuffer;
			}
		}

		public static ColumnType parseType(String s) {
			switch (s) {
			case "int": case "integer": return INTEGER; //$NON-NLS-1$ //$NON-NLS-2$
			case "float": return FLOAT; //$NON-NLS-1$
			case "double": return DOUBLE; //$NON-NLS-1$
			case "bit": return BIT; //$NON-NLS-1$
			case "boolean": case "bool": return BOOLEAN; //$NON-NLS-1$ //$NON-NLS-2$
			case "string": return ColumnType.STRING; //$NON-NLS-1$

			default:
				throw new IllegalArgumentException("Unknown handler type: "+s); //$NON-NLS-1$
			}
		}
	}
}
