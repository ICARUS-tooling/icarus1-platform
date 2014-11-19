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
package de.ims.icarus.util.strings.pattern;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.util.strings.StringUtil;
import de.ims.icarus.util.strings.pattern.TextSource.CompoundTextSource;
import de.ims.icarus.util.strings.pattern.TextSource.StaticTextSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PatternFactory<L extends Enum<L>> {

	public static final String DEFAULT_EMPTY_TEXT = "-"; //$NON-NLS-1$
	public static final String DEFAULT_TEXT_SEPARATOR = " "; //$NON-NLS-1$

	private final PatternContext<L> context;

	private CompoundTextSource root;
	private CharSequence source;
	private int cursor;
	private final StringBuilder buffer = new StringBuilder();
	private L level;

	public PatternFactory(PatternContext<L> context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		this.context = context;
	}

	public PatternContext<L> getContext() {
		return context;
	}

	public L getLevel() {
		return level;
	}

	private void cleanup() {
		root = null;
		source = null;
		cursor = -1;
		buffer.setLength(0);
		level = null;
	}

	public synchronized TextSource parse(L level, CharSequence input, Map<String, String> defaultOptions) throws ParseException {
		try {
			return parse0(level, input, 0, defaultOptions);
		} finally {
			cleanup();
		}
	}

	public static String escape(String s) {
		return s==null ? null : s.replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String unescape(String s) {
		return s==null ? null : s.replaceAll("\\\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public synchronized TextSource parse(L level, CharSequence input, int index, Map<String, String> defaultOptions) throws ParseException {
		try {
			return parse0(level, input, index, defaultOptions);
		} finally {
			cleanup();
		}
	}

	private static final char ESCAPE_SYMBOL = '\\';
	private static final char TOKEN_DELIMITER = ':';
	private static final char OPTION_DELIMITER = ';';
	private static final char ASSIGNMENT_SYMBOL = '=';
	private static final char ACCESSOR_BEGIN = '{';
	private static final char ACCESSOR_END = '}';

	private TextSource parse0(L level, CharSequence input, int index, Map<String, String> defaultOptions) throws ParseException {
		if (input == null)
			throw new NullPointerException("Invalid input"); //$NON-NLS-1$

		if(defaultOptions==null) {
			defaultOptions = Collections.emptyMap();
		}

		root = new CompoundTextSource();
		buffer.setLength(0);
		source = input;
		cursor = index;
		this.level = level;

		root.setExternalForm(unescapedPart(index, input.length()));

		int fromIndex = cursor;

		while(nextUnescaped(ACCESSOR_BEGIN)) {
			if(cursor>fromIndex) {
				root.addElement(new StaticTextSource(part(fromIndex, cursor)));
			}

			root.addElement(parseAccessorSource(level, defaultOptions));

			// Move to next pos and save
			cursor++;
			fromIndex = cursor;
		}

		if(fromIndex<source.length()) {
			root.addElement(new StaticTextSource(part(fromIndex, source.length())));
		}

		return root;
	}

	private boolean nextUnescaped(char target) {
		boolean escaped = false;

		while(cursor<source.length()) {
			char c = source.charAt(cursor);

			if(!escaped && c==target) {
				return true;
			}

			escaped = c==ESCAPE_SYMBOL;

			cursor++;
		}

		return false;
	}

	private char current() {
		return source.charAt(cursor);
	}

	/**
	 * Assumes the current symbol is '{' (defined by the internal cursor).
	 * After a successful match, the cursor will then be at the corresponding
	 * '}' symbol.
	 */
	private TextSource parseAccessorSource(L level, Map<String, String> defaultOptions) throws ParseException {

		if(current()!=ACCESSOR_BEGIN)
			throw new IllegalStateException("Current cursor does not point to a valid opening symbol: "+current()); //$NON-NLS-1$

		int accessBegin = cursor;

		// Scan for end of statement
		if(!nextUnescaped(ACCESSOR_END))
			throw new ParseException("Missing closing symbol '"+ACCESSOR_END+"' after beginning of accessor declaration in source '"+source+"' at index ", accessBegin); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		int accessEnd = cursor;

		// Scan for token delimiter
		int tokenSepIndex = StringUtil.indexOf(source, TOKEN_DELIMITER, accessBegin+1, accessEnd-1);

		if(tokenSepIndex==-1)
			throw new ParseException("Missing token delimiter symbol '"+TOKEN_DELIMITER+"' after beginning of accessor declaration in source '"+source+"' at index ", accessBegin); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Scan for options delimiter
		int optionsSepIndex = StringUtil.indexOf(source, OPTION_DELIMITER, tokenSepIndex+1, accessEnd-1);

		String statementString = unescapedPart(accessBegin, accessEnd+1).toString();

		String tokenString = unescapedPart(accessBegin+1, tokenSepIndex).toString();
		String specifierString;

		Map<String, String> options = defaultOptions;

		if(optionsSepIndex==-1) {
			specifierString = part(tokenSepIndex+1, accessEnd).toString();
		} else {
			specifierString = part(tokenSepIndex+1, optionsSepIndex).toString();

			options = new HashMap<>(options);
			parseOptions(options, optionsSepIndex+1, accessEnd-1);
		}

		try {
			Accessor<L> accessor = context.ceateAccessor(statementString, tokenString, specifierString, options);

			TextSource result = context.createTextSource(level, accessor);

			result.setExternalForm(statementString);

			return result;
		} catch(Exception e) {
			throw (ParseException)new ParseException("Unexpected error while parsing accessor", accessBegin).initCause(e); //$NON-NLS-1$
		}
	}

	private String unescapedPart(int start, int end) {
		return source.subSequence(start, end).toString();
	}

	private String part(int start, int end) {

		if(StringUtil.indexOf(source, '\\', start, end-1)==-1) {
			return source.subSequence(start, end).toString();
		} else {
			buffer.setLength(0);
			boolean escaped = false;

			for(int i=start; i<end; i++) {
				char c = source.charAt(i);

				boolean isEscapeSymbol = c==ESCAPE_SYMBOL;

				if(escaped || !isEscapeSymbol) {
					buffer.append(c);
				}

				escaped = isEscapeSymbol;
			}

			StringUtil.trim(buffer);

			return buffer.toString();
		}
	}

	private int unescapedIndexOf(char target, int fromIndex, int toIndex) {
		boolean escaped = false;

		for(int i=fromIndex; i<=toIndex; i++) {
			char c = source.charAt(i);

			if(!escaped && c==target) {
				return i;
			}

			escaped = c==ESCAPE_SYMBOL;
		}

		return -1;

	}

	private void parseOptions(Map<String, String> options, int fromIndex, int toIndex) throws ParseException {
		while(fromIndex<toIndex) {
			int assignIndex = unescapedIndexOf(ASSIGNMENT_SYMBOL, fromIndex, toIndex);

			if(assignIndex==-1)
				throw new ParseException("Missing assignment symbol '"+ASSIGNMENT_SYMBOL+"' after beginning of options declaration in source '"+source+"' at index ", fromIndex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			int sepIndex = unescapedIndexOf(OPTION_DELIMITER, assignIndex, toIndex);

			String key = part(fromIndex, assignIndex).toString();
			String value;

			if(sepIndex==-1) {
				value = part(assignIndex+1, toIndex+1);
				fromIndex = toIndex;
			} else {
				value = part(assignIndex+1, sepIndex);
				fromIndex = sepIndex+1;
			}

			options.put(key, value);
		}
	}
}
