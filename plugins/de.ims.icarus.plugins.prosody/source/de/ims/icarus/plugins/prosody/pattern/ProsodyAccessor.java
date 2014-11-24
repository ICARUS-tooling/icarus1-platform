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

import java.util.Map;

import de.ims.icarus.Core;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.strings.pattern.Accessor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ProsodyAccessor extends Accessor<ProsodyLevel> {

	// Modifiers for values
	protected String format;
	protected int leftOffset = -1, rightOffset = -1, offset = -1;
	protected int[] positions;

	protected ProsodyAccessor(String source, String specifier, ProsodyLevel type) {
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
		return fetchProsodyValue((ProsodyData) data, env);
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

	protected abstract Object fetchProsodyValue(ProsodyData data, Options env);

	public static ProsodyAccessor forLevel(ProsodyLevel level, String source, String specifier) {
		if (level == null)
			throw new NullPointerException("Invalid level"); //$NON-NLS-1$

		switch (level) {
		case ENVIRONMENT:
			return new EnvironmentAccessor(source, specifier);
		case DOCUMENT:
			return new DocumentAccessor(source, specifier);
		case SENTENCE:
			return new SentenceAccessor(source, specifier);
		case SYLLABLE:
			return new SyllableAccessor(source, specifier);
		case WORD:
			return new WordAccessor(source, specifier);

		default:
			throw new CorruptedStateException("Impossible switch branch"); //$NON-NLS-1$
		}
	}

	public static class SyllableAccessor extends ProsodyAccessor {

		public SyllableAccessor(String source, String specifier) {
			super(source, specifier, ProsodyLevel.SYLLABLE);
		}

		@Override
		public Object fetchProsodyValue(ProsodyData data, Options env) {
			return data.getSentence().getSyllableProperty(data.getWordIndex(), getSpecifier(), data.getSyllableIndex());
		}
	}

	public static class WordAccessor extends ProsodyAccessor {

		public WordAccessor(String source, String specifier) {
			super(source, specifier, ProsodyLevel.WORD);
		}

		@Override
		public Object fetchProsodyValue(ProsodyData data, Options env) {
			return data.getSentence().getProperty(data.getWordIndex(), getSpecifier());
		}

	}

	public static class SentenceAccessor extends ProsodyAccessor {

		public SentenceAccessor(String source, String specifier) {
			super(source, specifier, ProsodyLevel.SENTENCE);
		}

		@Override
		public Object fetchProsodyValue(ProsodyData data, Options env) {
			return data.getSentence().getProperty(getSpecifier());
		}

	}

	public static class DocumentAccessor extends ProsodyAccessor {

		public DocumentAccessor(String source, String specifier) {
			super(source, specifier, ProsodyLevel.DOCUMENT);
		}

		@Override
		public Object fetchProsodyValue(ProsodyData data, Options env) {
			return data.getSentence().getDocument().getProperty(getSpecifier());
		}

	}

	public static class EnvironmentAccessor extends ProsodyAccessor {

		public EnvironmentAccessor(String source, String specifier) {
			super(source, specifier, ProsodyLevel.ENVIRONMENT);
		}

		@Override
		public Object fetchProsodyValue(ProsodyData data, Options env) {
			Object value = env==null ? null : env.get(getSpecifier());
			return value==null ? Core.getCore().getProperty(getSpecifier()) : value;
		}

	}
}
