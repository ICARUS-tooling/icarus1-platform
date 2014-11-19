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
import java.util.Map.Entry;

import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.plugins.prosody.DefaultProsodicDocumentData;
import de.ims.icarus.plugins.prosody.DefaultProsodicSentenceData;
import de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.HtmlUtils;
import de.ims.icarus.util.Options;
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

	public static void main(String[] args) throws Exception {
		CoreferenceDocumentSet documentSet = new CoreferenceDocumentSet();
		DefaultProsodicDocumentData document = new DefaultProsodicDocumentData(documentSet, 0);

		String[] forms = new String[]{"This", "is", "a", "test"};
		DefaultProsodicSentenceData sentence = new DefaultProsodicSentenceData(document, forms);
		sentence.setProperty("id", 1.234);

		for(int i=0; i<sentence.length(); i++) {
			sentence.setProperty(i, "form", sentence.getForm(i));
			sentence.setProperty(i, "lemma", sentence.getForm(i).toLowerCase());
		}

		sentence.setProperty(0, "syllable_label", new String[]{"s0", "s1", "s2"});
		sentence.setProperty(0, "syllable_count", 3);

		String pattern = "{sent:id} {syl:syllable_label;pos=1,-2;pref=\\{;suf=\\}} {word:form;min=10} ({word:lemma}) {env:file}";

		PatternFactory<ProsodyLevel> factory = new PatternFactory<>(new ProsodyPatternContext());

		TextSource textSource = factory.parse(ProsodyLevel.WORD, pattern, null);

		ProsodyData data = new ProsodyData();

		data.set(sentence, 0);

		Options options = new Options();
		options.put("file", "<some-file-some-where>");

		System.out.println(textSource.getText(data, options));
	}

	public static TextSource createTextSource(ProsodyLevel level, String pattern) throws ParseException {
		return createTextSource(level, pattern, null);
	}

	public static TextSource createTextSource(ProsodyLevel level, String pattern, Map<String, String> options) throws ParseException {
		if(pattern==null || pattern.isEmpty()) {
			return EMPTY_TEXT_SOURCE;
		}

		return new PatternFactory<>(new ProsodyPatternContext()).parse(level, pattern, null);
	}

	public static String getInfoText() {
		StringBuilder sb = new StringBuilder(300);
		ResourceManager rm = ResourceManager.getInstance();

		sb.append("<html>"); //$NON-NLS-1$
		sb.append("<h3>").append(rm.get("plugins.prosody.labelPattern.title")).append("</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append("<table>"); //$NON-NLS-1$
		sb.append("<tr><th>") //$NON-NLS-1$
			.append(rm.get("plugins.prosody.labelPattern.character")).append("</th><th>") //$NON-NLS-1$ //$NON-NLS-2$
			.append(rm.get("plugins.prosody.labelPattern.description")).append("</th></tr>"); //$NON-NLS-1$ //$NON-NLS-2$

		Map<Object, Object> mc = LabelPattern.magicCharacters;
		for(Entry<Object, Object> entry : mc.entrySet()) {
			String c = entry.getKey().toString();
			String key = entry.getValue().toString();

			sb.append("<tr><td>").append(HtmlUtils.escapeHTML(c)) //$NON-NLS-1$
			.append("</td><td>").append(rm.get(key)).append("</td></tr>"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		sb.append("</table>"); //$NON-NLS-1$

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

		// Sentence upwards does not support any form of aggregation
		if(accessor.getLevel().compareTo(ProsodyLevel.SENTENCE)>=0) {
			return new TextSource.DirectTextSource(accessor);
		}

		// Cannot step over more than one hierarchical boundary
		if(level.ordinal()-accessor.getLevel().ordinal()>1) {
			return EMPTY_TEXT_SOURCE;
		}

		ProsodyAccessor prosodyAccessor = (ProsodyAccessor) accessor;

		boolean isSyllableLevel = level==ProsodyLevel.SYLLABLE;
		boolean isSyllableAccessor = accessor.getLevel()==ProsodyLevel.SYLLABLE;

		ProsodyTextSource textSource = isSyllableAccessor ?
				new ProsodyTextSource.SyllableTextSource(prosodyAccessor) :
					new ProsodyTextSource.WordTextSource(prosodyAccessor);

		IndexIterator indexIterator = iteratorForAccessor(prosodyAccessor);
		if(indexIterator==null && isSyllableAccessor && !isSyllableLevel) {
			indexIterator = new ProsodyTextSource.CompleteIndexIterator();
		}

		textSource.setIndexIterator(indexIterator);

		return textSource;
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
