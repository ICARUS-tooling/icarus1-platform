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
package de.ims.icarus.plugins.prosody.ui.view.outline;

import java.util.Arrays;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.util.CompactProperties;

public class WordInfo implements Comparable<WordInfo> {
	private final int wordIndex;
	private String[] lines;
	private int width, x, curveWidth;
	private final SyllableInfo[] syllables;
	private final float minD, maxD;

	private final CompactProperties properties = new CompactProperties();

	public WordInfo(ProsodicSentenceData sentence, int wordIndex) {
		this.wordIndex = wordIndex;

		int sylCount = sentence.getSyllableCount(wordIndex);

		if(sylCount>0) {

			float minD = Float.MAX_VALUE;
			float maxD = Float.MIN_VALUE;

			syllables = new SyllableInfo[sylCount];

			for(int i=0; i<sylCount; i++) {
				SyllableInfo syllableInfo = new SyllableInfo(sentence, wordIndex, i);
				maxD = Math.max(maxD, syllableInfo.getD());
				minD = Math.min(minD, syllableInfo.getD()-Math.max(syllableInfo.getC1(), syllableInfo.getC2()));

				syllables[i] = syllableInfo;
			}

			this.minD = minD;
			this.maxD = maxD;
		} else {
			syllables = null;
			minD = -1F;
			maxD = -1F;
		}
	}

	WordInfo() {
		wordIndex = -1;
		syllables = null;
		minD = maxD = 0;
	}

	public int sylCount() {
		return syllables==null ? 0 : syllables.length;
	}

	public String[] getLines() {
		return lines;
	}

	public int getWidth() {
		return width;
	}

	public boolean hasSyllables() {
		return syllables!=null && syllables.length>0;
	}

	public int getWordIndex() {
		return wordIndex;
	}

	public SyllableInfo[] getSyllables() {
		return syllables;
	}

	public float getMinD() {
		return minD;
	}

	public float getMaxD() {
		return maxD;
	}

	public SyllableInfo syllableInfo(int sylIndex) {
		return syllables[sylIndex];
	}

	public void setLines(String[] lines) {
		this.lines = lines;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the curveWidth
	 */
	public int getCurveWidth() {
		return curveWidth;
	}

	/**
	 * @param curveWidth the curveWidth to set
	 */
	public void setCurveWidth(int curveWidth) {
		this.curveWidth = curveWidth;
	}

	public SyllableInfo firstSyl() {
		return hasSyllables() ? syllables[0] : null;
	}

	public SyllableInfo lastSyl() {
		return hasSyllables() ? syllables[syllables.length-1] : null;
	}

	private static final SyllableInfo sharedCompareKey = new SyllableInfo();

	public SyllableInfo sylForOffset(int offset) {
		if(!hasSyllables()) {
			return null;
		}
		if(offset<x || offset>x+width) {
			return null;
		}

		sharedCompareKey.setX(offset);
		sharedCompareKey.setWidth(0);

		int index = Arrays.binarySearch(syllables, sharedCompareKey);

		return index < 0 ? null : syllables[index];
	}

	public SyllableInfo nearestSylForOffset(int offset) {
		if(!hasSyllables()) {
			return null;
		}
		if(offset<x || offset>x+width) {
			return null;
		}

		sharedCompareKey.setX(offset);
		sharedCompareKey.setWidth(0);

		int index = Arrays.binarySearch(syllables, sharedCompareKey);

		if(index<0) {
			index = -(index+1);
		}

		index = Math.max(0, Math.min(index, syllables.length-1));

		return syllables[index];
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WordInfo o) {
		if(o.x>=x && o.x+o.width<=x+width) {
			return 0;
		}
		// Keep in mind that words cannot overlap!
		return x-o.x;
	}

	public boolean contains(int offset) {
		return offset>=x && offset<=x+width;
	}

	public CompactProperties getProperties() {
		return properties;
	}

	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}
}