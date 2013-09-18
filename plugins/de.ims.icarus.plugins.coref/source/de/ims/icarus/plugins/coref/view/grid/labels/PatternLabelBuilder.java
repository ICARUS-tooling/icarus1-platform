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
package de.ims.icarus.plugins.coref.view.grid.labels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.ims.icarus.language.coref.Cluster;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.plugins.coref.view.grid.EntityGridNode;
import de.ims.icarus.util.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PatternLabelBuilder implements GridLabelBuilder {
	
	protected String pattern;
	
	protected Element[] elements;
	protected StringBuilder buffer = new StringBuilder();

	public PatternLabelBuilder(String pattern) {
		setPattern(pattern);
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		if(pattern==null)
			throw new IllegalArgumentException("Invalid pattern"); //$NON-NLS-1$
		
		if(pattern.equals(this.pattern)) {
			return;
		}
		
		this.pattern = pattern;
		
		elements = compile(pattern);
		
		if(elements==null || elements.length==0)
			throw new IllegalStateException("No valid element array available"); //$NON-NLS-1$
	}
	
	/**
	 * Default implementation magic characters:
	 * <p>
	 * <table>
	 * <tr><th>Character</th><th>Description</th></tr>
	 * <tr><td>\</td><td>escaping character to allow for magic characters to be used without substitution</td></tr>
	 * <tr><td>b</td><td><i>begin index</i> of the {@link Span}</td></tr>
	 * <tr><td>e</td><td><i>end index</i> of the {@code Span}</td></tr>
	 * <tr><td>c</td><td>number of {@code Span}s within the given ones {@link Cluster} in this sentence</td></tr>
	 * <tr><td>r</td><td><i>range</i> of the given {@code Span}, i.e. the number of tokens it spans across in the surrounding sentence</td></tr>
	 * <tr><td>l</td><td><i>length</i> of the current {@code Span} in terms of characters (note that whitespace characters are included)</td></tr>
	 * <tr><td>%...%</td><td>value of the span property associated with the given key (%name% would cause the value for the 'name' property to be inserted)</td></tr>
	 * <tr><td>$...$</td><td>value of the sentence property associated with the given key (%form% would cause the value for the 'form' property to be inserted)</td></tr>
	 * </table>
	 * <p>
	 * All characters not listed as magic characters remain untouched by the
	 * replacement engine.
	 */
	protected Element[] compile(String pattern) {
		List<Element> elements = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		
		boolean escaped = false;
		boolean key = false;
		int size = pattern.length();
		char context = '\0';
		
		for(int i=0; i<size; i++) {
			char c = pattern.charAt(i);
			Element element = null;
			boolean collectChars = false;
			
			if(escaped || (key && c!='%' && c!='$' && c!='§')) {
				sb.append(c);
				escaped = false;
				continue;
			} 
			
			switch (c) {
			case '\\': 
				escaped = true;
				break;
				
			case '%':
				if(key) {
					element = new PropertyElement(sb.toString());
					sb.setLength(0);
				} else {
					collectChars = true;
				}
				key = !key;
				break;
				
			case '§':
				if(key) {
					element = new HeadPropertyElement(sb.toString());
					sb.setLength(0);
				} else {
					collectChars = true;
				}
				key = !key;
				break;
				
			case '$':
				if(key) {
					element = new SentencePropertyElement(sb.toString());
					sb.setLength(0);
				} else {
					collectChars = true;
				}
				key = !key;
				break;

			case 'b':
				element = new BoundaryElement(true);
				break;

			case 'e':
				element = new BoundaryElement(false);
				break;

			case 'c':
				element = new CountElement();
				break;

			case 'r':
				element = new LengthElement(false);
				break;

			case 'l':
				element = new LengthElement(true);
				break;
				
			default:
				sb.append(c);
				break;
			}
			
			if(sb.length()>0 && (collectChars || element!=null || i==size-1)) {
				char[] cr = new char[sb.length()];
				sb.getChars(0, cr.length, cr, 0);
				sb.setLength(0);
				elements.add(new CharacterElement(cr));
			}
			if(element!=null) {
				elements.add(element);
			}
		}
		
		return elements.toArray(new Element[elements.size()]);
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.grid.labels.GridLabelBuilder#getLabel(de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
	 */
	@Override
	public String getLabel(EntityGridNode node, int spanIndex) {
		buffer.setLength(0);
		
		for(Element element : elements) {
			element.append(buffer, node, spanIndex);
		}
		
		return buffer.toString();
	}

	public static final Map<Object, Object> magicCharacters =
			Collections.unmodifiableMap(CollectionUtils.asLinkedMap(
					"\\", "plugins.coref.labelPattern.escape", //$NON-NLS-1$ //$NON-NLS-2$
					"b", "plugins.coref.labelPattern.beginIndex", //$NON-NLS-1$ //$NON-NLS-2$
					"e", "plugins.coref.labelPattern.endIndex", //$NON-NLS-1$ //$NON-NLS-2$
					"c", "plugins.coref.labelPattern.count", //$NON-NLS-1$ //$NON-NLS-2$
					"r", "plugins.coref.labelPattern.range", //$NON-NLS-1$ //$NON-NLS-2$
					"l", "plugins.coref.labelPattern.length", //$NON-NLS-1$ //$NON-NLS-2$
					"%...%", "plugins.coref.labelPattern.spanProperty", //$NON-NLS-1$ //$NON-NLS-2$
					"$...$", "plugins.coref.labelPattern.sentenceProperty", //$NON-NLS-1$ //$NON-NLS-2$
					"§...§", "plugins.coref.labelPattern.headProperty" //$NON-NLS-1$ //$NON-NLS-2$
	));

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface Element {
		void append(StringBuilder buffer, EntityGridNode node, int spanIndex);
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class CharacterElement implements Element {
		private final char[] characters;

		public CharacterElement(char[] characters) {
			if(characters==null || characters.length==0)
				throw new IllegalArgumentException("Invalid or empty character array"); //$NON-NLS-1$
			
			this.characters = characters;
		}

		/**
		 * @see de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder.Element#append(java.lang.StringBuilder, de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
		 */
		@Override
		public void append(StringBuilder buffer, EntityGridNode node,
				int spanIndex) {
			buffer.append(characters);
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class CountElement implements Element {

		/**
		 * @see de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder.Element#append(java.lang.StringBuilder, de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
		 */
		@Override
		public void append(StringBuilder buffer, EntityGridNode node,
				int spanIndex) {
			buffer.append(node.getSpanCount());
		}
		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class BoundaryElement implements Element {
		private final boolean isStart;

		public BoundaryElement(boolean isStart) {
			this.isStart = isStart;
		}

		/**
		 * @see de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder.Element#append(java.lang.StringBuilder, de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
		 */
		@Override
		public void append(StringBuilder buffer, EntityGridNode node, int spanIndex) {
			Span span = node.getSpan(spanIndex);
			if(isStart) {
				buffer.append(span.getBeginIndex()+1);
			} else {
				buffer.append(span.getEndIndex()+1);
			}
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class LengthElement implements Element {

		private final boolean charLength;
		public LengthElement(boolean charLength) {
			this.charLength = charLength;
		}
		/**
		 * @see de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder.Element#append(java.lang.StringBuilder, de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
		 */
		@Override
		public void append(StringBuilder buffer, EntityGridNode node,
				int spanIndex) {
			Span span = node.getSpan(spanIndex);
			if(charLength) {
				CoreferenceData sentence = node.getSentence();
				int len = 0;
				for(int i=span.getBeginIndex(); i<=span.getEndIndex(); i++) {
					len += sentence.getForm(i).length();
					if(i<span.getEndIndex()) {
						len++;
					}
				}
				
				buffer.append(len);
			} else {
				buffer.append(span.getRange());
			}
		}
		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class PropertyElement implements Element {

		private final String key;
		
		public PropertyElement(String key) {
			if(key==null || key.isEmpty())
				throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
			
			this.key = key;
		}

		/**
		 * @see de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder.Element#append(java.lang.StringBuilder, de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
		 */
		@Override
		public void append(StringBuilder buffer, EntityGridNode node,
				int spanIndex) {
			Object value = node.getSpan(spanIndex).getProperty(key);
			buffer.append(value==null ? '-' : value);
		}		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class SentencePropertyElement implements Element {

		private final String key;
		
		public SentencePropertyElement(String key) {
			if(key==null || key.isEmpty())
				throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
			
			this.key = key;
		}

		/**
		 * @see de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder.Element#append(java.lang.StringBuilder, de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
		 */
		@Override
		public void append(StringBuilder buffer, EntityGridNode node,
				int spanIndex) {
			CoreferenceData sentence = node.getSentence();
			Span span = node.getSpan(spanIndex);
			
			int length = buffer.length();
			
			int beginIndex = span.getBeginIndex();
			int endIndex = span.getEndIndex();
			for(int i=beginIndex; i<=endIndex; i++) {
				buffer.append(sentence.getProperty(key+'_'+i));
				if(i<endIndex) {
					buffer.append(' ');
				}
			}
			
			if(buffer.length()==length) {
				buffer.append('-');
			}
		}		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class HeadPropertyElement implements Element {

		private final String key;
		
		public HeadPropertyElement(String key) {
			if(key==null || key.isEmpty())
				throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
			
			this.key = key;
		}

		/**
		 * @see de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder.Element#append(java.lang.StringBuilder, de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
		 */
		@Override
		public void append(StringBuilder buffer, EntityGridNode node,
				int spanIndex) {
			CoreferenceData sentence = node.getSentence();
			Span span = node.getSpan(spanIndex);
			
			int length = buffer.length();
			
			int head = span.getHead();
			
			buffer.append(sentence.getProperty(key+'_'+head));
			
			if(buffer.length()==length) {
				buffer.append('-');
			}
		}		
	}
}
