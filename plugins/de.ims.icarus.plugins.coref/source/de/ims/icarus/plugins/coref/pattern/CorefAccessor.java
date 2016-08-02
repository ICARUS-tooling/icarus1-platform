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

 * $Revision: 332 $
 * $Date: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.prosody/source/de/ims/icarus/plugins/prosody/pattern/CorefAccessor.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.coref.pattern;

import java.util.Map;

import de.ims.icarus.Core;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.strings.pattern.Accessor;

/**
 * @author Markus Gärtner
 * @version $Id: CorefAccessor.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public abstract class CorefAccessor extends Accessor<CorefLevel> {

	// Modifiers for values
	protected String format;
	protected int leftOffset = -1, rightOffset = -1, offset = -1;
	protected int[] positions;

	protected CorefAccessor(String source, String specifier, CorefLevel type) {
		super(source, specifier, type);
	}

	public String getFormat() {
		return format;
	}

	public int getLeftOffset() {
		return leftOffset;
	}

	public int getRightOffset() {
		return rightOffset;
	}

	public int getOffset() {
		return offset;
	}

	public int[] getPositions() {
		return positions;
	}

	@Override
	public void readOptions(Map<String, String> options) {
		super.readOptions(options);

		format = CollectionUtils.firstSet(options, "f", "format"); //$NON-NLS-1$ //$NON-NLS-2$
		leftOffset = CollectionUtils.firstSetInt(options, -1, "l", "left"); //$NON-NLS-1$ //$NON-NLS-2$
		rightOffset = CollectionUtils.firstSetInt(options, -1, "r", "right"); //$NON-NLS-1$ //$NON-NLS-2$
		offset = CollectionUtils.firstSetInt(options, -1, "offset", "scope"); //$NON-NLS-1$ //$NON-NLS-2$

		String pos = CollectionUtils.firstSet(options, "pos", "positions"); //$NON-NLS-1$ //$NON-NLS-2$
		if(pos!=null) {
			String[] items = pos.split(","); //$NON-NLS-1$
			positions = new int[items.length];
			for(int i=0; i<items.length; i++) {
				positions[i] = Integer.parseInt(items[i].trim());
			}
		}
		//TODO
	}

	@Override
	protected Object fetchValue(Object data, Options env) {
		return fetchValue0((CorefDataProxy) data, env);
	}

	@Override
	protected Object applyValueOptions(Object value) {
		if(format!=null) {
			return String.format(locale, format, value);
		} else if(value instanceof Float || value instanceof Double) {
			return String.format(locale, "%.2f", value); //$NON-NLS-1$
		} else {
			return value;
		}
	}

	protected abstract Object fetchValue0(CorefDataProxy data, Options env);

	public static CorefAccessor forLevel(CorefLevel level, String source, String specifier) {
		if (level == null)
			throw new NullPointerException("Invalid level"); //$NON-NLS-1$

		switch (level) {
		case ENVIRONMENT:
			return new EnvironmentAccessor(source, specifier);
		case DOCUMENT:
			return new DocumentAccessor(source, specifier);
		case SENTENCE:
			return new SentenceAccessor(source, specifier);
		case SPAN:
			return new SpanAccessor(source, specifier);
		case EDGE:
			return new EdgeAccessor(source, specifier);
		case WORD:
			return new WordAccessor(source, specifier);

		default:
			throw new CorruptedStateException("Impossible switch branch: "+level); //$NON-NLS-1$
		}
	}

	public static class WrappedCorefAccessor extends CorefAccessor {

		private final CorefTextSource textSource;

		public WrappedCorefAccessor(CorefTextSource textSource) {
			super("", "", textSource.getAccessor().getLevel()); //$NON-NLS-1$ //$NON-NLS-2$

			this.textSource = textSource;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyAccessor#fetchProsodyValue(de.ims.icarus.plugins.prosody.pattern.PatternDataProxy, de.ims.icarus.util.Options)
		 */
		@Override
		protected Object fetchValue0(CorefDataProxy data, Options env) {
			return textSource.getText(data, env);
		}

	}

	public static class WordAccessor extends CorefAccessor {

		public WordAccessor(String source, String specifier) {
			super(source, specifier, CorefLevel.WORD);
		}

		@Override
		public Object fetchValue0(CorefDataProxy data, Options env) {
			return data.getSentence().getProperty(data.getWordIndex(), getSpecifier());
		}

	}

	public static class SpanAccessor extends CorefAccessor {

		public SpanAccessor(String source, String specifier) {
			super(source, specifier, CorefLevel.SPAN);
		}

		@Override
		public Object fetchValue0(CorefDataProxy data, Options env) {
			Span span = data.getSpan();
			return span==null ? null : span.getProperty(getSpecifier());
		}
	}

	public static class EdgeAccessor extends CorefAccessor {

		public EdgeAccessor(String source, String specifier) {
			super(source, specifier, CorefLevel.EDGE);
		}

		@Override
		public Object fetchValue0(CorefDataProxy data, Options env) {
			Edge edge = data.getEdge();
			return edge==null ? null : edge.getProperty(getSpecifier());
		}
	}

	public static class SentenceAccessor extends CorefAccessor {

		public SentenceAccessor(String source, String specifier) {
			super(source, specifier, CorefLevel.SENTENCE);
		}

		@Override
		public Object fetchValue0(CorefDataProxy data, Options env) {
			return data.getSentence().getProperty(getSpecifier());
		}

	}

	public static class DocumentAccessor extends CorefAccessor {

		public DocumentAccessor(String source, String specifier) {
			super(source, specifier, CorefLevel.DOCUMENT);
		}

		@Override
		public Object fetchValue0(CorefDataProxy data, Options env) {
			return data.getDocument().getProperty(getSpecifier());
		}

	}

	public static class EnvironmentAccessor extends CorefAccessor {

		public EnvironmentAccessor(String source, String specifier) {
			super(source, specifier, CorefLevel.ENVIRONMENT);
		}

		@Override
		public Object fetchValue0(CorefDataProxy data, Options env) {
			Object value = env==null ? null : env.get(getSpecifier());
			return value==null ? Core.getCore().getProperty(getSpecifier()) : value;
		}

	}
}
