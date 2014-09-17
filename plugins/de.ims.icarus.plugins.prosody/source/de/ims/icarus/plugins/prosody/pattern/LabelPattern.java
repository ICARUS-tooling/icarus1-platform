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
package de.ims.icarus.plugins.prosody.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LabelPattern implements ProsodyConstants {

	private Element[] elements;
	private String pattern;

	public static final String NO_VALUE = "-"; //$NON-NLS-1$

	private static final Element EMPTY_LINE = new Element() {

		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			return NO_VALUE;
		}
	};

	public static final Map<Object, Object> magicCharacters =
			Collections.unmodifiableMap(CollectionUtils.asLinkedMap(
					"\\", "plugins.prosody.labelPattern.escape", //$NON-NLS-1$ //$NON-NLS-2$
					"c", "plugins.prosody.labelPattern.syllableCount", //$NON-NLS-1$ //$NON-NLS-2$
					"n", "plugins.prosody.labelPattern.sentenceNumber", //$NON-NLS-1$ //$NON-NLS-2$
					"$...$", "plugins.prosody.labelPattern.wordProperty", //$NON-NLS-1$ //$NON-NLS-2$
					"#...#", "plugins.prosody.labelPattern.syllableProperty" //$NON-NLS-1$ //$NON-NLS-2$
	));

	public static String escapePattern(String s) {
		return s==null ? null : s.replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String unescapePattern(String s) {
		return s==null ? null : s.replaceAll("\\\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public LabelPattern() {
		// no-op
	}

	public LabelPattern(String pattern) {
		compile(pattern);
	}

	public void compile(String pattern) {
		if (pattern == null)
			throw new NullPointerException("Invalid pattern"); //$NON-NLS-1$

		if(pattern.isEmpty()) {
			elements = null;
			return;
		}

		String[] lines = pattern.split("[\\n\\r]+"); //$NON-NLS-1$

		if(lines.length==0) {
			elements = null;
			return;
		}

		List<Element> buffer = new ArrayList<>();

		Element[] newElements = new Element[lines.length];
		for(int i=0; i<newElements.length; i++) {

			String line = lines[i];
			if(line==null || line.isEmpty()) {
				newElements[i] = EMPTY_LINE;
				continue;
			}

			StringBuilder sb = new StringBuilder();
			boolean escaped = false;
			boolean key = false;
			int size = line.length();

			for(int j=0; j<size; j++) {
				char c = line.charAt(j);
				Element element = null;
				boolean collectChars = false;

				if(escaped || (key && c!='$' && c!='#')) {
					sb.append(c);
					escaped = false;
					continue;
				}

				switch (c) {
				case '\\':
					escaped = true;
					break;

				case '$':
					if(key) {
						element = new PropertyElement(sb.toString());
						sb.setLength(0);
					} else {
						collectChars = true;
					}
					key = !key;
					break;

				case '#':
					if(key) {
						element = new SyllablePropertyElement(sb.toString());
						sb.setLength(0);
					} else {
						collectChars = true;
					}
					key = !key;
					break;

//				case 'b':
//					element = new PropertyElement(BEGIN_TS_KEY);
//					break;
//
//				case 'e':
//					element = new PropertyElement(END_TS_KEY);
//					break;

				case 'c':
					element = new SyllableCountElement();
					break;

//				case 'f':
//					element = new PropertyElement(FORM_KEY);
//					break;
//
//				case 'p':
//					element = new PropertyElement(POS_KEY);
//					break;

				case 'n':
					element = new PropertyElement(SENTENCE_NUMBER_KEY);
					break;

				default:
					sb.append(c);
					break;
				}

				if(sb.length()>0 && (collectChars || element!=null || j==size-1)) {
					buffer.add(new TextElement(sb.toString()));
					sb.setLength(0);
				}
				if(element!=null) {
					buffer.add(element);
				}
			}

			Element element = EMPTY_LINE;

			if(buffer.size()>1) {
				CompoundElement cElem = new CompoundElement();
				for(Element el : buffer) {
					cElem.addElement(el);
				}
				element = cElem;
			} else if(!buffer.isEmpty()) {
				element = buffer.get(0);
			}

			buffer.clear();

			newElements[i] = element;
		}

		this.pattern = pattern;
		elements = newElements;
	}

	public String[] getText(ProsodicSentenceData sentence) {
		return getText(sentence, -1);
	}

	public String[] getText(ProsodicSentenceData sentence, int wordIndex) {
		if(elements==null || elements.length==0) {
			return null;
		}

		String[] lines = new String[elements.length];

		for(int i=0; i<elements.length; i++) {
			lines[i] = elements[i].getText(sentence, wordIndex);
		}

		return lines;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	private static String val2String(Object value) {
		if(value==null) {
			return NO_VALUE;
		}

		if(value instanceof Double || value instanceof Float) {
			return String.format(Locale.ENGLISH, "%.02f", value);
		}

		return String.valueOf(value);
	}

	private interface Element {
		String getText(ProsodicSentenceData sentence, int wordIndex);
	}

	private static class CompoundElement implements Element {
		private List<Element> elements = new ArrayList<>();
		private final StringBuilder buffer = new StringBuilder(50);

		public void addElement(Element element) {
			if (element == null)
				throw new NullPointerException("Invalid element"); //$NON-NLS-1$

			elements.add(element);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			if(elements.isEmpty()) {
				return NO_VALUE;
			}

			for(Element element : elements) {
				buffer.append(element.getText(sentence, wordIndex));
			}

			String result = buffer.toString();
			buffer.setLength(0);

			return result;
		}

	}

	private static class TextElement implements Element {

		private final String text;

		TextElement(String text) {
			if (text == null)
				throw new NullPointerException("Invalid text"); //$NON-NLS-1$

			this.text = text;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			return text;
		}

	}

	private static class PropertyElement implements Element {

		private final String property;

		PropertyElement(String property) {
			if (property == null)
				throw new NullPointerException("Invalid property"); //$NON-NLS-1$

			this.property = property;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			Object value = wordIndex==-1 ? sentence.getProperty(property) : sentence.getProperty(wordIndex, property);

			return val2String(value);
		}

	}

	private static class SyllableCountElement implements Element {

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			int count = 0;
			if(wordIndex==-1) {
				for(int i=0; i<sentence.length(); i++) {
					count += sentence.getSyllableCount(i);
				}
			} else {
				count = sentence.getSyllableCount(wordIndex);
			}
			return count<=0 ? NO_VALUE : String.valueOf(count);
		}

	}

	private static class SyllablePropertyElement implements Element {

		private final String property;
		private final StringBuilder buffer = new StringBuilder(50);

		SyllablePropertyElement(String property) {
			if (property == null)
				throw new NullPointerException("Invalid property"); //$NON-NLS-1$

			this.property = property;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			int sylCount = sentence.getSyllableCount(wordIndex);

			if(sylCount==0) {
				return NO_VALUE;
			}

			for(int i=0; i<sylCount; i++) {
				if(i>0) {
					buffer.append(',');
				}

				Object value = sentence.getSyllableProperty(wordIndex, property, i);

				buffer.append(val2String(value));
			}

			String result = buffer.toString();
			buffer.setLength(0);

			return result;
		}
	}
}
