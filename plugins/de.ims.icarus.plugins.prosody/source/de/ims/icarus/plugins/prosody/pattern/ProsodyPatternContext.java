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

import java.text.ParseException;
import java.util.Map;

import de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.strings.pattern.Accessor;
import de.ims.icarus.util.strings.pattern.PatternContext;
import de.ims.icarus.util.strings.pattern.PatternFactory;
import de.ims.icarus.util.strings.pattern.TextSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyPatternContext implements PatternContext<ProsodyLevel> {

//	public static void main(String[] args) throws Exception {
//		CoreferenceDocumentSet documentSet = new CoreferenceDocumentSet();
//		DefaultProsodicDocumentData document = new DefaultProsodicDocumentData(documentSet, 0);
//
//		String[] forms = new String[]{"This", "is", "a", "test"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//		DefaultProsodicSentenceData sentence = new DefaultProsodicSentenceData(document, forms);
//		sentence.setProperty("id", 1.234); //$NON-NLS-1$
//
//		for(int i=0; i<sentence.length(); i++) {
//			sentence.setProperty(i, "form", sentence.getForm(i)); //$NON-NLS-1$
//			sentence.setProperty(i, "lemma", sentence.getForm(i).toLowerCase()); //$NON-NLS-1$
//		}
//
//		sentence.setProperty(0, "syllable_label", new String[]{"s0", "s1", "s2"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//		sentence.setProperty(0, "syllable_count", 3); //$NON-NLS-1$
//
//		String pattern = "{sent:id} {syl:syllable_label;pos=1,-2;pref=\\{;suf=\\}} {word:form;min=10} ({word:lemma}) {env:file}"; //$NON-NLS-1$
//
//		PatternFactory<ProsodyLevel> factory = new PatternFactory<>(new ProsodyPatternContext());
//
//		TextSource textSource = factory.parse(ProsodyLevel.WORD, pattern, null);
//
//		ProsodyData data = new ProsodyData();
//
//		data.set(sentence, 0);
//
//		Options options = new Options();
//		options.put("file", "<some-file-some-where>"); //$NON-NLS-1$ //$NON-NLS-2$
//
//		System.out.println(textSource.getText(data, options));
//	}

	public static TextSource createTextSource(ProsodyLevel level, String pattern) throws ParseException {
		return createTextSource(level, pattern, null);
	}

	public static TextSource createTextSource(ProsodyLevel level, String pattern, Map<String, String> options) throws ParseException {
		if(pattern==null || pattern.isEmpty()) {
			return EMPTY_TEXT_SOURCE;
		}

		return new PatternFactory<>(new ProsodyPatternContext()).parse(level, pattern, null);
	}

	public static TextSource createTextSource(String pattern, Map<String, String> options) throws ParseException {
		if(pattern==null || pattern.isEmpty()) {
			return EMPTY_TEXT_SOURCE;
		}

		return new PatternFactory<>(new ProsodyPatternContext()).parse(pattern, null);
	}

	public static String createStatement(ProsodyLevel level, String specifier) {
		return PatternFactory.ACCESSOR_BEGIN+level.getToken()+PatternFactory.TOKEN_DELIMITER+specifier+PatternFactory.ACCESSOR_END;
	}

	public static String getInfoText() {
		StringBuilder sb = new StringBuilder(300);
		ResourceManager rm = ResourceManager.getInstance();

		sb.append("<html>"); //$NON-NLS-1$
		sb.append("<h3>").append(rm.get("plugins.prosody.labelPattern.title")).append("</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append("<br>"); //$NON-NLS-1$
		sb.append("{&lt;level&gt;:&lt;property-name&gt;[;option]}"); //$NON-NLS-1$

		for(ProsodyLevel level: ProsodyLevel.values()) {
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

	private static final TextSource EMPTY_TEXT_SOURCE = new TextSource.StaticTextSource(PatternFactory.DEFAULT_EMPTY_TEXT);

	/**
	 * @see de.ims.icarus.util.strings.pattern.PatternContext#ceateAccessor(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public Accessor<ProsodyLevel> ceateAccessor(String statement, String token,
			String specifier, Map<String, String> options) {
		ProsodyLevel level = ProsodyLevel.parseLevel(token);

		if(level==null)
			throw new IllegalArgumentException("Not a valid prosody level token: "+token); //$NON-NLS-1$

		ProsodyAccessor accessor = ProsodyAccessor.forLevel(level, statement, specifier);

		if(accessor==null)
			throw new IllegalArgumentException("No accessor found for token: "+token); //$NON-NLS-1$

		accessor.readOptions(options);

		return accessor;
	}

	/**
	 * @see de.ims.icarus.util.strings.pattern.PatternContext#createTextSource(java.lang.Enum, de.ims.icarus.util.strings.pattern.Accessor)
	 */
	@Override
	public TextSource createTextSource(ProsodyLevel level,
			Accessor<ProsodyLevel> accessor) {

		int dif = level.ordinal()-accessor.getLevel().ordinal();
		if(accessor.getLevel()==ProsodyLevel.ENVIRONMENT) {
			return new ProsodyTextSource.DirectProsodyTextSource((ProsodyAccessor) accessor);
		} else if(dif>1) {
		// Cannot step over more than one hierarchical boundary downwards
			return EMPTY_TEXT_SOURCE;
		} else if(dif<0) {
			return new TextSource.DirectTextSource(accessor);
		}

		ProsodyAccessor prosodyAccessor = (ProsodyAccessor) accessor;

//		boolean isSyllableLevel = level==ProsodyLevel.SYLLABLE;
//		boolean isSyllableAccessor = accessor.getLevel()==ProsodyLevel.SYLLABLE;

		if(level!=prosodyAccessor.getLevel()) {
			ProsodyTextSource textSource = textSourceForLevel(prosodyAccessor.getLevel(), prosodyAccessor);

			IndexIterator indexIterator = iteratorForAccessor(prosodyAccessor);
			if(indexIterator==null) {
				indexIterator = new ProsodyTextSource.CompleteIndexIterator();
			}
			textSource.setIndexIterator(indexIterator);

			prosodyAccessor = new ProsodyAccessor.WrappedProsodyAccessor(textSource);
		}

		return textSourceForLevel(level, prosodyAccessor);
	}

	private static ProsodyTextSource textSourceForLevel(ProsodyLevel level, ProsodyAccessor prosodyAccessor) {
		switch (level) {
		case SYLLABLE: return new ProsodyTextSource.SyllableTextSource(prosodyAccessor);
		case WORD: return new ProsodyTextSource.WordTextSource(prosodyAccessor);
		case SENTENCE: return new ProsodyTextSource.SentenceTextSource(prosodyAccessor);
		case DOCUMENT: return new ProsodyTextSource.DocumentTextSource(prosodyAccessor);

		default:
			throw new IllegalArgumentException("Not a valid level: "+level); //$NON-NLS-1$
		}
	}

	private IndexIterator iteratorForAccessor(ProsodyAccessor accessor) {
		int scope = accessor.getOffset();
		if(scope!=-1) {
			return new ProsodyTextSource.ScopeIndexIterator(scope);
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

			return new ProsodyTextSource.OffsetIndexIterator(leftOffset, rightOffset);
		}

		int[] positions = accessor.getPositions();
		if(positions!=null && positions.length>0) {
			return new ProsodyTextSource.FixedIndexIterator(positions);
		}

		return null;
	}
}
