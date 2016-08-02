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

 * $Revision: 459 $
 * $Date: 2016-05-16 23:25:11 +0200 (Mo, 16 Mai 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.prosody/source/de/ims/icarus/plugins/prosody/pattern/CorefPatternContext.java $
 *
 * $LastChangedDate: 2016-05-16 23:25:11 +0200 (Mo, 16 Mai 2016) $
 * $LastChangedRevision: 459 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.coref.pattern;

import java.text.ParseException;
import java.util.Map;

import de.ims.icarus.plugins.coref.pattern.CorefTextSource.IndexIterator;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.strings.pattern.Accessor;
import de.ims.icarus.util.strings.pattern.PatternContext;
import de.ims.icarus.util.strings.pattern.PatternFactory;
import de.ims.icarus.util.strings.pattern.TextSource;

/**
 * @author Markus Gärtner
 * @version $Id: CorefPatternContext.java 459 2016-05-16 21:25:11Z mcgaerty $
 *
 */
public class CorefPatternContext implements PatternContext<CorefLevel> {

	public static TextSource createTextSource(CorefLevel level, String pattern) throws ParseException {
		return createTextSource(level, pattern, null);
	}

	public static TextSource createTextSource(CorefLevel level, String pattern, Map<String, String> options) throws ParseException {
		if(pattern==null || pattern.isEmpty()) {
			return EMPTY_TEXT_SOURCE;
		}

		return new PatternFactory<>(new CorefPatternContext()).parse(level, pattern, null);
	}

	public static TextSource createTextSource(String pattern, Map<String, String> options) throws ParseException {
		if(pattern==null || pattern.isEmpty()) {
			return EMPTY_TEXT_SOURCE;
		}

		return new PatternFactory<>(new CorefPatternContext()).parse(pattern, null);
	}

	public static String createStatement(CorefLevel level, String specifier) {
		return PatternFactory.ACCESSOR_BEGIN+level.getToken()+PatternFactory.TOKEN_DELIMITER+specifier+PatternFactory.ACCESSOR_END;
	}

	public static String getInfoText() {
		StringBuilder sb = new StringBuilder(300);
		ResourceManager rm = ResourceManager.getInstance();

		sb.append("<html>"); //$NON-NLS-1$
		sb.append("<h3>").append(rm.get("plugins.coref.labelPattern.info")).append("</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append("<br>"); //$NON-NLS-1$
		sb.append("{&lt;level&gt;:&lt;property-name&gt;[;option]}"); //$NON-NLS-1$

		for(CorefLevel level: CorefLevel.values()) {
			sb.append("<br>"); //$NON-NLS-1$
			sb.append("<h4>").append(level.getName()).append("&nbsp;(").append(level.getToken()).append(")</h4>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sb.append(level.getDescription());

			String[] properties = level.getAvailableProperties();
			if(properties!=null) {
				sb.append(" ("); //$NON-NLS-1$

				for(int i=0; i<properties.length; i++) {
					if(i>0) {
						sb.append(',');
						if(i%10==0) {
							sb.append("<br>"); //$NON-NLS-1$
						}
					}
					sb.append(properties[i]);
				}

				sb.append(")"); //$NON-NLS-1$
			}
		}

		return sb.toString();
	}

	private static final TextSource EMPTY_TEXT_SOURCE = new TextSource.StaticTextSource(""); //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.util.strings.pattern.PatternContext#ceateAccessor(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public Accessor<CorefLevel> ceateAccessor(String statement, String token,
			String specifier, Map<String, String> options) {
		CorefLevel level = CorefLevel.parseLevel(token);

		if(level==null)
			throw new IllegalArgumentException("Not a valid prosody level token: "+token); //$NON-NLS-1$

		CorefAccessor accessor = CorefAccessor.forLevel(level, statement, specifier);

		if(accessor==null)
			throw new IllegalArgumentException("No accessor found for token: "+token); //$NON-NLS-1$

		accessor.readOptions(options);

		return accessor;
	}

	/**
	 * @see de.ims.icarus.util.strings.pattern.PatternContext#createTextSource(java.lang.Enum, de.ims.icarus.util.strings.pattern.Accessor)
	 */
	@Override
	public TextSource createTextSource(CorefLevel level,
			Accessor<CorefLevel> accessor) {

		int dif = CorefLevel.dif(level, accessor.getLevel());
		if(accessor.getLevel()==CorefLevel.ENVIRONMENT
				|| accessor.getLevel()==CorefLevel.EDGE) {
			return new CorefTextSource.DirectCorefTextSource((CorefAccessor) accessor);
		} else if(dif>1) {
		// Cannot step over more than one hierarchical boundary downwards
			return EMPTY_TEXT_SOURCE;
		} else if(dif<0) {
			return new TextSource.DirectTextSource(accessor);
		}

		CorefAccessor corefAccessor = (CorefAccessor) accessor;

		if(level!=corefAccessor.getLevel()) {
			CorefTextSource textSource = textSourceForLevel(corefAccessor.getLevel(), corefAccessor);

			IndexIterator indexIterator = iteratorForAccessor(corefAccessor);
			if(indexIterator==null) {
				indexIterator = new CorefTextSource.CompleteIndexIterator();
			}
			textSource.setIndexIterator(indexIterator);

			corefAccessor = new CorefAccessor.WrappedCorefAccessor(textSource);
		}

		return textSourceForLevel(level, corefAccessor);
	}

	private static CorefTextSource textSourceForLevel(CorefLevel level, CorefAccessor corefAccessor) {
		switch (level) {
		case SPAN: return new CorefTextSource.SpanTextSource(corefAccessor);
		case WORD: return new CorefTextSource.WordTextSource(corefAccessor);
		case SENTENCE: return new CorefTextSource.SentenceTextSource(corefAccessor);
		case DOCUMENT: return new CorefTextSource.DocumentTextSource(corefAccessor);

		default:
			throw new IllegalArgumentException("Not a valid level: "+level); //$NON-NLS-1$
		}
	}

	private IndexIterator iteratorForAccessor(CorefAccessor accessor) {
		int scope = accessor.getOffset();
		if(scope!=-1) {
			return new CorefTextSource.ScopeIndexIterator(scope);
		}

		int leftOffset = accessor.getLeftOffset();
		int rightOffset = accessor.getRightOffset();
		if(leftOffset!=-1 || rightOffset!=-1) {
			if(leftOffset==-1) {
				leftOffset = 0;
			}
			if(rightOffset==-1) {
				rightOffset = 0;
			}

			return new CorefTextSource.OffsetIndexIterator(leftOffset, rightOffset);
		}

		int[] positions = accessor.getPositions();
		if(positions!=null && positions.length>0) {
			return new CorefTextSource.FixedIndexIterator(positions);
		}

		return null;
	}
}
