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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;

public class CurveInfo {
		private final ProsodicSentenceData sentence;
		private final BufferedImage image;
		private final int sylCount;
		private final String[] sylTokens;
		// [from, to] for every syllable
		private final int[] sylOffsets;
		// [from, to] for every word
		private final int[] formToSylMap;
		// Combined form tokens of sentence
		private final String text;

		public CurveInfo(ProsodicSentenceData sentence, FontMetrics fm, AntiAliasingType antiAliasingType, Color curveColor) {
			this.sentence = sentence;

			int size = sentence.length();

			// Count syllables
			int sylCount = 0;
			for(int i=0; i<size; i++) {
				sylCount += sentence.getSyllableCount(i);
			}

			this.sylCount = sylCount;
			sylTokens = new String[sylCount];
			sylOffsets = new int[sylCount<<1];
			formToSylMap = new int[size<<1];

			// Prepare data
			StringBuilder sb = new StringBuilder(size*5);
			// [b, c1, c2, d] per syllable
			float[] sylPainte = new float[sylCount<<2];
			int width = 0;
			int sylPos = 0;
			float minD = Float.MAX_VALUE;
			float maxD = Float.MIN_VALUE;
			for(int i=0; i<size; i++) {
				if(i>0) {
					sb.append(SentencePanel.SPACE);
					width += fm.charWidth(SentencePanel.SPACE);
				}

				String token = sentence.getForm(i);

				sb.append(token);

				int numSyls = sentence.getSyllableCount(i);
				if(numSyls==0) {
					width += fm.stringWidth(token);
					formToSylMap[i<<1] = -1;
					formToSylMap[(i<<1)+1] = -1;
					continue;
				}

				formToSylMap[i<<1] = sylPos;
				formToSylMap[(i<<1)+1] = sylPos+numSyls-1;

				for(int k=0; k<numSyls; k++) {

					int offset0 = sentence.getSyllableOffset(i, k);
					int offset1 = k<numSyls-1 ? sentence.getSyllableOffset(i, k+1) : token.length();
					String sylToken = token.substring(offset0, offset1);
					int sylWidth = fm.stringWidth(sylToken);

					sylOffsets[sylPos<<1] = width;
					sylOffsets[(sylPos<<1)+1] = width+sylWidth;
					sylTokens[sylPos] = sylToken;

					float d = sentence.getPainteD(i, k);
					float c1 = sentence.getPainteC1(i, k);
					float c2 = sentence.getPainteC2(i, k);
					float b = sentence.getPainteB(i, k);
					maxD = Math.max(maxD, d);
					minD = Math.min(minD, d-Math.max(c1, c2));
					int painteIdx = sylPos<<2;
					sylPainte[painteIdx++] = b;
					sylPainte[painteIdx++] = c1;
					sylPainte[painteIdx++] = c2;
					sylPainte[painteIdx] = d;

					width += sylWidth;
					sylPos++;
				}
			}

			text = sb.toString();

			// Now draw image

			int height = fm.getAscent();
			float scaleY = height/(maxD-minD);
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();

			g.setColor(new Color(0, 0, 0, 0));
			g.fillRect(0, 0, width, height);

			antiAliasingType.apply(g);
			g.setColor(curveColor);

			for(int i=0; i<sylCount; i++) {
				int painteIdx = i<<2;

				float b = sylPainte[painteIdx];
				float c1 = sylPainte[painteIdx+1];
				float c2 = sylPainte[painteIdx+2];
				float d = sylPainte[painteIdx+3];

				// Ignore peaks outside the current syllable
				//FIXME ask whether filtering them out would be better!
//				if(b<-1F || b>1F) {
//					continue;
//				}

				int x0 = sylOffsets[i<<1];
				int x2 = sylOffsets[(i<<1)+1];
				int x1 = (x2+x0)>>1;

				int y0 = height - (int)((d-c1-minD) * scaleY);
				int y1 = height - (int)((d-minD) * scaleY);
				int y2 = height - (int)((d-c2-minD) * scaleY);

				g.drawLine(x0, y0, x1, y1);
				g.drawLine(x1, y1, x2, y2);
			}
		}

		public ProsodicSentenceData getSentence() {
			return sentence;
		}

		public int sylCount() {
			return sylCount;
		}

		public String getText() {
			return text;
		}

		public BufferedImage getImage() {
			return image;
		}

		public boolean hasSyllables(int wordIndex) {
			return formToSylMap[wordIndex<<1] >=0;
		}

		public int sylCount(int wordIndex) {
			int left = formToSylMap[wordIndex<<1];
			int right = formToSylMap[(wordIndex<<1)+1];

			return (left==-1 || right==-1) ? 0 : right-left+1;
		}

		public int sylCount(int leftWord, int rightWord) {
			int count = 0 ;
			for(int i=leftWord; i<=rightWord; i++) {
				int left = formToSylMap[i<<1];
				int right = formToSylMap[(i<<1)+1];

				if(left!=-1 && right!=-1) {
					count += right-left+1;
				}
			}

			return count;
		}

		public int firstSyl(int wordIndex) {
			return formToSylMap[wordIndex<<1];
		}

		public int lastSyl(int wordIndex) {
			return formToSylMap[(wordIndex<<1)+1];
		}

		public int leftOffset(int sylIndex) {
			return sylOffsets[sylIndex<<1];
		}

		public int rightOffset(int sylIndex) {
			return sylOffsets[(sylIndex<<1)+1];
		}

		public int offset2Syl(int offset) {
    		int sylIndex = Arrays.binarySearch(sylOffsets, offset);
    		if(sylIndex<0) {
    			sylIndex = -(sylIndex + 1);
    		}
    		sylIndex /= 2;

    		if(sylOffsets[sylIndex<<1]>offset) {
    			sylIndex--;
    		} else if(sylOffsets[(sylIndex<<1)+1]<offset) {
    			sylIndex++;
    		}

    		if(sylIndex<0 || sylIndex>=sylCount) {
    			return -1;
    		}

    		return sylIndex;
		}

		public int syl2Word(int sylIndex) {

//    		int wordIndex = Arrays.binarySearch(curveInfo.formToSylMap, sylIndex);
//    		if(wordIndex<0) {
//    			wordIndex = -(wordIndex + 1);
//    		}
//    		wordIndex /= 2;
//
//    		return wordIndex;

			//FIXME binary search breaks since we store -1 for word tokens not containing syllables (like punctuation characters)

			for(int i=0; i<sentence.length(); i++) {
				if(sylIndex>=formToSylMap[i<<1] && sylIndex<=formToSylMap[(i<<1)+1])  {
					return i;
				}
			}

			return -1;
		}
	}