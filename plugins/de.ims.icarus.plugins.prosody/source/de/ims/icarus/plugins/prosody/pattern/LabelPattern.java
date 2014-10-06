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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LabelPattern implements ProsodyConstants {

	public static void main(String[] args) {
		String test = "%documentId::15:% (n) $speaker:::?:$\\:  ";
		LabelPattern pattern = new LabelPattern(test);
	}

	private Element[] elements;
	private String pattern;

	public static final String NO_VALUE = "-"; //$NON-NLS-1$

	private static final Element EMPTY_LINE = new Element() {

		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			return null;
		}
	};

	public static final Map<Object, Object> magicCharacters =
			Collections.unmodifiableMap(CollectionUtils.asLinkedMap(
					"\\", "plugins.prosody.labelPattern.escape", //$NON-NLS-1$ //$NON-NLS-2$
					"c", "plugins.prosody.labelPattern.syllableCount", //$NON-NLS-1$ //$NON-NLS-2$
					"n", "plugins.prosody.labelPattern.sentenceNumber", //$NON-NLS-1$ //$NON-NLS-2$
					"d", "plugins.prosody.labelPattern.documentIndex", //$NON-NLS-1$ //$NON-NLS-2$
					"%...%", "plugins.prosody.labelPattern.documentProperty", //$NON-NLS-1$ //$NON-NLS-2$
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

	private void consumeText(StringBuilder sb, Collection<? super Element> buffer) {
		if(sb.length()>0) {
			buffer.add(new TextElement(getString(sb)));
		}
	}

	private static String getString(StringBuilder sb) {
		if(sb.length()>0) {
			String s = sb.toString();
			sb.setLength(0);
			return s;
		} else {
			return null;
		}
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
		// [min_length, max_length, default]
		Opts options = new Opts();
		for(int i=0; i<newElements.length; i++) {

			String line = lines[i];
			if(line==null || line.isEmpty()) {
				newElements[i] = EMPTY_LINE;
				continue;
			}

			StringBuilder sb = new StringBuilder();
			boolean escaped = false;
			boolean key = false;
			boolean allowOptions = false;
			int size = line.length();
			Element element = null;
			options.reset();

			for(int j=0; j<size; j++) {
				char c = line.charAt(j);

				if(escaped || (key && c!='$' && c!='%' && c!='#' && c!=':')) {
					sb.append(c);
					escaped = false;
					continue;
				}

				allowOptions = element!=null;

				switch (c) {
				case '\\':
					escaped = true;
					break;

				case ':':
					if(element==null || !allowOptions)
						throw new IllegalStateException("Attempting to define options outside of element context"); //$NON-NLS-1$

					if(options.isEmpty() && sb.length()>0) {
						element.setModifier(getString(sb));
					}

					options.addOption(sb);
					break;

				case '$':
					if(element==null) {
						consumeText(sb, buffer);
						element = new PropertyElement();
						buffer.add(element);
					} else {
						if(!options.applyOptions(element)) {
							element.setModifier(getString(sb));
						}
					}
					key = !key;
					break;

				case '#':
					if(element==null) {
						consumeText(sb, buffer);
						element = new SyllablePropertyElement();
						buffer.add(element);
					} else {
						if(!options.applyOptions(element)) {
							element.setModifier(getString(sb));
						}
					}
					key = !key;
					break;

				case '%':
					if(element==null) {
						consumeText(sb, buffer);
						element = new DocumentPropertyElement();
						buffer.add(element);
					} else {
						if(!options.applyOptions(element)) {
							element.setModifier(getString(sb));
						}
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

				case 'n':
					consumeText(sb, buffer);
					element = new PropertyElement();
					element.setModifier(SENTENCE_NUMBER_KEY);
					buffer.add(element);
					break;

				case 'd':
					element = new DocumentIndexElement();
					buffer.add(element);
					break;

				default:
					sb.append(c);
					element = null;
					break;
				}
			}

			options.applyOptions(element);
			consumeText(sb, buffer);

			element = EMPTY_LINE;

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
			lines[i] = getText0(elements[i], sentence, wordIndex);
		}

		return lines;
	}

	public String[] getText(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		if(elements==null || elements.length==0) {
			return null;
		}

		String[] lines = new String[elements.length];

		for(int i=0; i<elements.length; i++) {
			lines[i] = getText0(elements[i], sentence, wordIndex, sylIndex);
		}

		return lines;
	}

	public int getElementCount() {
		return elements==null ? 0 : elements.length;
	}

	public Element getPatternElement(int index) {
		return elements[index];
	}

	private static String getText0(Element element, ProsodicSentenceData sentence, int wordIndex) {

		String text = element.getText(sentence, wordIndex);

		if(text==null) {
			text = element.getNoValueLabel();
		} else {
			if(element.getMinLength()!=-1 && text.length()<element.getMinLength()) {
				text = StringUtil.padRight(text, element.getMinLength());
			} else if(element.getMaxLength()!=-1 && text.length()>element.getMaxLength()) {
				text = text.substring(0, element.getMaxLength()-5)+"[...]"; //$NON-NLS-1$
			}
		}

		return text;
	}

	private static String getText0(Element element, ProsodicSentenceData sentence, int wordIndex, int sylIndex) {

		String text = element.getText(sentence, wordIndex, sylIndex);

		if(text==null) {
			text = element.getNoValueLabel();
		} else {
			if(element.getMinLength()!=-1 && text.length()<element.getMinLength()) {
				text = StringUtil.padRight(text, element.getMinLength());
			} else if(element.getMaxLength()!=-1 && text.length()>element.getMaxLength()) {
				text = text.substring(0, element.getMaxLength()-5)+"[...]"; //$NON-NLS-1$
			}
		}

		return text;
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
			return String.format(Locale.ENGLISH, "%.02f", value); //$NON-NLS-1$
		}

		return String.valueOf(value);
	}

	private static class Opts {
		private String[] options = new String[3];
		private int optIndex = -1;

		public void addOption(StringBuilder sb) {
			if(optIndex>=options.length)
				throw new IllegalArgumentException("Too many options"); //$NON-NLS-1$

			if(sb.length()>0) {
				options[optIndex] = getString(sb);
			}
			optIndex++;
		}

		public boolean isEmpty() {
			return optIndex<0;
		}

		public void reset() {
			Arrays.fill(options, null);
			optIndex = -1;
		}

		public boolean applyOptions(Element element) {
			if(element==null) {
				return false;
			}

			boolean empty = true;
			if(options[0]!=null && !options[0].isEmpty()) {
				element.setMinLength(Integer.parseInt(options[0]));
				empty = false;
			}
			if(options[1]!=null && !options[1].isEmpty()) {
				element.setMaxLength(Integer.parseInt(options[1]));
				empty = false;
			}
			if(options[2]!=null && !options[2].isEmpty()) {
				element.setNoValueLabel(options[2]);
				empty = false;
			}

			reset();

			return !empty;
		}
	}

	public static abstract class Element {
		private int minLength = -1;
		private int maxLength = -1;
		private String noValueLabel = NO_VALUE;

		public abstract String getText(ProsodicSentenceData sentence, int wordIndex);

		public String getText(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
			return null;
		}

		protected void setModifier(String modifier) {
			// for subclasses
		}

		public int getMinLength() {
			return minLength;
		}

		public int getMaxLength() {
			return maxLength;
		}

		public String getNoValueLabel() {
			return noValueLabel;
		}

		public void setMinLength(int minLength) {
			if(minLength<-1)
				throw new IllegalArgumentException("Minimum length  must not be less than -1: "+minLength); //$NON-NLS-1$

			this.minLength = minLength;
		}

		public void setMaxLength(int maxLength) {
			if(maxLength<6 && maxLength!=-1)
				throw new IllegalArgumentException("Maximum length must be either -1 or greater than 5: "+maxLength); //$NON-NLS-1$

			this.maxLength = maxLength;
		}

		public void setNoValueLabel(String noValueLabel) {
			if (noValueLabel == null)
				throw new NullPointerException("Invalid noValueLabel"); //$NON-NLS-1$

			this.noValueLabel = noValueLabel;
		}
	}

	private static class CompoundElement extends Element {
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
				return null;
			}

			for(Element element : elements) {
				buffer.append(getText0(element, sentence, wordIndex));
			}

			String result = buffer.toString();
			buffer.setLength(0);

			return result;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
			if(elements.isEmpty()) {
				return null;
			}

			for(Element element : elements) {
				buffer.append(getText0(element, sentence, wordIndex, sylIndex));
			}

			String result = buffer.toString();
			buffer.setLength(0);

			return result;
		}

	}

	private static class TextElement extends Element {

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

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
			return text;
		}

	}

	private static class PropertyElement extends Element {

		private String property;

		@Override
		public void setModifier(String modifier) {
			if (modifier == null)
				throw new NullPointerException("Invalid modifier"); //$NON-NLS-1$

			property = modifier;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			Object value = wordIndex==-1 ? sentence.getProperty(property) : sentence.getProperty(wordIndex, property);

			return val2String(value);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
			Object value = sentence.getSyllableProperty(wordIndex, property, sylIndex);

			return val2String(value);
		}

	}

	private static class DocumentIndexElement extends Element {

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			ProsodicDocumentData document = sentence.getDocument();
			return String.valueOf(document.getDocumentIndex());
		}

	}

	private static class DocumentPropertyElement extends Element {

		private String property;

		@Override
		public void setModifier(String modifier) {
			if (modifier == null)
				throw new NullPointerException("Invalid modifier"); //$NON-NLS-1$

			property = modifier;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			ProsodicDocumentData document = sentence.getDocument();
			Object value = document.getProperty(property);

			return val2String(value);
		}

	}

	private static class SyllableCountElement extends Element {

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
			return count<=0 ? null : String.valueOf(count);
		}

	}

	private static class SyllablePropertyElement extends Element {

		private String property;
		private final StringBuilder buffer = new StringBuilder(50);

		@Override
		public void setModifier(String modifier) {
			if (modifier == null)
				throw new NullPointerException("Invalid modifier"); //$NON-NLS-1$

			property = modifier;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex) {
			int sylCount = sentence.getSyllableCount(wordIndex);

			if(sylCount==0) {
				return null;
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

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.LabelPattern.Element#getText(de.ims.icarus.plugins.prosody.ProsodicSentenceData, int, int)
		 */
		@Override
		public String getText(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {

			Object value = sentence.getSyllableProperty(wordIndex, property, sylIndex);
			return val2String(value);
		}
	}
}
