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

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;

public class CurveInfo {
		private final ProsodicSentenceData sentence;
		private final float minD, maxD;
		private final WordInfo[] words;
		private final String text;
		private final int sylCount;

		private int width;

		public CurveInfo(ProsodicSentenceData sentence) {
			this.sentence = sentence;
			text = LanguageUtils.combine(sentence);

			float minD = Float.MAX_VALUE;
			float maxD = Float.MIN_VALUE;
			int sylCount = 0;

			words = new WordInfo[sentence.length()];

			for(int i=0; i<words.length; i++) {
				WordInfo wordInfo = new WordInfo(sentence, i);

				if(wordInfo.hasSyllables()) {
					minD = Math.min(minD, wordInfo.getMinD());
					maxD = Math.max(maxD, wordInfo.getMaxD());
					sylCount += wordInfo.sylCount();
				}

				words[i] = wordInfo;
			}

			this.minD = minD;
			this.maxD = maxD;
			this.sylCount = sylCount;
		}

		public ProsodicSentenceData getSentence() {
			return sentence;
		}

		public float getMinD() {
			return minD;
		}

		public float getMaxD() {
			return maxD;
		}

		public boolean hasWords() {
			return words!=null && words.length>0;
		}

		public WordInfo[] getWords() {
			return words;
		}

		public int wordCount() {
			return words==null ? 0 : words.length;
		}

		public String getText() {
			return text;
		}

		public int sylCount() {
			return sylCount;
		}

		/**
		 * @return the width
		 */
		public int getWidth() {
			return width;
		}

		/**
		 * @param width the width to set
		 */
		public void setWidth(int width) {
			this.width = width;
		}

		public WordInfo wordInfo(int wordIndex) {
			return words[wordIndex];
		}

		public SyllableInfo syllableInfo(int wordIndex, int sylIndex) {
			return wordInfo(wordIndex).syllableInfo(sylIndex);
		}

		public boolean hasSyllables(int wordIndex) {
			return wordInfo(wordIndex).hasSyllables();
		}

		public int sylCount(int wordIndex) {
			return wordInfo(wordIndex).sylCount();
		}

		public int sylCount(int leftWord, int rightWord) {
			int count = 0 ;
			for(int i=leftWord; i<=rightWord; i++) {
				WordInfo wordInfo = wordInfo(i);
				if(wordInfo!=null) {
					count += wordInfo.sylCount();
				}
			}

			return count;
		}

		public WordInfo firstWord() {
			return hasWords() ? words[0] : null;
		}

		public WordInfo lastWord() {
			return hasWords() ? words[words.length-1] : null;
		}

		public SyllableInfo firstSyl(int wordIndex) {
			return firstWord().firstSyl();
		}

		public SyllableInfo lastSyl() {
			return lastWord().lastSyl();
		}

		private static final WordInfo sharedCompareKey = new WordInfo();

		public WordInfo wordForOffset(int offset) {
			if(!hasWords()) {
				return null;
			}

			sharedCompareKey.setX(offset);
			sharedCompareKey.setWidth(0);

			int index = Arrays.binarySearch(words, sharedCompareKey);

			return index < 0 ? null : words[index];
		}

		public SyllableInfo sylForOffset(int offset) {
			WordInfo wordInfo = wordForOffset(offset);
			if(wordInfo==null) {
				return null;
			}

			offset -= wordInfo.getX();
			return wordInfo.sylForOffset(offset);
		}

		public WordInfo nearestWordForOffset(int offset) {
			if(!hasWords()) {
				return null;
			}

			sharedCompareKey.setX(offset);
			sharedCompareKey.setWidth(0);

			int index = Arrays.binarySearch(words, sharedCompareKey);

			if(index<0) {
				index = -(index+1);
			}

			index = Math.max(0, Math.min(index, words.length-1));

			return words[index];
		}

		public SyllableInfo nearestSylForOffset(int offset) {
			WordInfo wordInfo = nearestWordForOffset(offset);
			if(wordInfo==null) {
				return null;
			}

			offset -= wordInfo.getX();
			return wordInfo.nearestSylForOffset(offset);
		}
	}