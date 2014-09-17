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

public class SyllableInfo implements Comparable<SyllableInfo> {
	private int width, x;
	private final float b, c1, c2, d;
	private final int wordIndex, sylIndex;
	private final String label;

	public SyllableInfo(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		this.wordIndex = wordIndex;
		this.sylIndex = sylIndex;

		b = sentence.getPainteB(wordIndex, sylIndex);
		c1 = sentence.getPainteC1(wordIndex, sylIndex);
		c2 = sentence.getPainteC2(wordIndex, sylIndex);
		d = sentence.getPainteD(wordIndex, sylIndex);

		String token = sentence.getForm(wordIndex);
		int sylCount = sentence.getSyllableCount(wordIndex);
		if(sentence.isMapsSyllables()) {
			int offset0 = sentence.getSyllableOffset(wordIndex, sylIndex);
			int offset1 = sylIndex<sylCount-1 ? sentence.getSyllableOffset(wordIndex, sylIndex+1) : token.length();
			label = token.substring(offset0, offset1);
		} else {
			float beginTs = sentence.getBeginTimestamp(wordIndex);
			float duration = sentence.getEndTimestamp(wordIndex)-beginTs;
			float sylBegin = sentence.getSyllableTimestamp(wordIndex, sylIndex)-beginTs;
			float sylDuration = sentence.getSyllableDuration(wordIndex, sylIndex);
			int tokenLength = token.length();
			int offset0 = (int) Math.floor(sylBegin/duration * tokenLength);
			int offset1 = (int) Math.floor((sylBegin+sylDuration) / duration * tokenLength);

			label = token.substring(offset0, Math.min(offset1, tokenLength));
		}
	}

	SyllableInfo() {
		b = c1 = c2 = d = 0;
		wordIndex = sylIndex = -1;
		label = null;
	}

	public String getLabel() {
		return label;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
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
		return wordIndex;
	}

	public int getSylIndex() {
		return sylIndex;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SyllableInfo o) {
		if(o.x>=x && o.x+o.width<=x+width) {
			return 0;
		}
		// Keep in mind that syllables cannot overlap!
		return x-o.x;
	}

	public boolean contains(int offset) {
		return offset>=x && offset<=x+width;
	}
}