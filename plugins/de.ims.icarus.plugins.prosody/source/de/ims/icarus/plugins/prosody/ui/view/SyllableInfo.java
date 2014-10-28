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
package de.ims.icarus.plugins.prosody.ui.view;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;


public class SyllableInfo extends PartInfo {
	private final float b, c1, c2, d;
	private final int sylIndex;
	private final WordInfo wordInfo;

	public SyllableInfo(WordInfo wordInfo, int sylIndex) {
		this.sylIndex = sylIndex;
		this.wordInfo = wordInfo;

		ProsodicSentenceData sentence = wordInfo.getSentenceInfo().getSentence();
		int wordIndex = wordInfo.getWordIndex();

		b = sentence.getPainteB(wordIndex, sylIndex);
		c1 = sentence.getPainteC1(wordIndex, sylIndex);
		c2 = sentence.getPainteC2(wordIndex, sylIndex);
		d = sentence.getPainteD(wordIndex, sylIndex);

		String token = sentence.getForm(wordIndex);
//		int sylCount = sentence.getSyllableCount(wordIndex);
		if(sentence.isMapsSyllables(wordIndex)) {
//			int offset0 = sentence.getSyllableOffset(wordIndex, sylIndex);
//			int offset1 = sylIndex<sylCount-1 ? sentence.getSyllableOffset(wordIndex, sylIndex+1) : token.length();
//			setLabel(token.substring(offset0, offset1));
			setLabel(sentence.getSyllableForm(wordIndex, sylIndex));
		} else {
			float beginTs = sentence.getBeginTimestamp(wordIndex);
			float duration = sentence.getEndTimestamp(wordIndex)-beginTs;
			float sylBegin = sentence.getSyllableTimestamp(wordIndex, sylIndex)-beginTs;
			float sylDuration = sentence.getSyllableDuration(wordIndex, sylIndex);
			int tokenLength = token.length();
			int offset0 = (int) Math.floor(sylBegin/duration * tokenLength);
			int offset1 = (int) Math.floor((sylBegin+sylDuration) / duration * tokenLength);

			offset1 = Math.min(offset1, tokenLength);

			if(sylIndex==sentence.getSyllableCount(wordIndex)-1) {
				offset1 = tokenLength;
			}

			setLabel(token.substring(offset0, offset1));
		}
	}

	SyllableInfo() {
		b = c1 = c2 = d = 0;
		sylIndex = -1;
		wordInfo = null;
	}

	public Object getBaseProperty(String key) {
		return wordInfo.getSentenceInfo().getSentence().getSyllableProperty(wordInfo.getWordIndex(), key, sylIndex);
	}

	public WordInfo getWordInfo() {
		return wordInfo;
	}

	public float getB() {
		return b;
	}

	public float getC1() {
		return c1;
	}

	public float getC2() {
		return c2;
	}

	public float getD() {
		return d;
	}

	public int getWordIndex() {
		return wordInfo.getWordIndex();
	}

	public int getSylIndex() {
		return sylIndex;
	}
}