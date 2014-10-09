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
package de.ims.icarus.plugins.prosody.sound;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;

public class SoundOffsets {
	public static final float NO_VALUE = LanguageUtils.DATA_UNDEFINED_VALUE;

	public static float getBeginOffset(ProsodicDocumentData document) {
		String audioOffset = (String) document.getProperty(ProsodyConstants.AUDIO_OFFSET_KEY);
		return audioOffset==null ? NO_VALUE : Float.parseFloat(audioOffset);
	}

	public static float getEndOffset(ProsodicDocumentData document) {
		CoreferenceDocumentSet documentSet = document.getDocumentSet();
		int index = document.getDocumentIndex();
		if(index>=documentSet.size()-1) {
			return NO_VALUE;
		}

		ProsodicDocumentData nextDocument = (ProsodicDocumentData) documentSet.get(index+1);

		if(!document.getProperty(ProsodyConstants.AUDIO_FILE_KEY).equals(
				nextDocument.getProperty(ProsodyConstants.AUDIO_FILE_KEY))) {
			return NO_VALUE;
		}

		String audioOffset = (String) nextDocument.getProperty(ProsodyConstants.AUDIO_OFFSET_KEY);
		return audioOffset==null ? NO_VALUE : Float.parseFloat(audioOffset);
	}

	public static float getBeginOffset(ProsodicSentenceData sentence) {
		String audioOffset = (String) sentence.getProperty(ProsodyConstants.AUDIO_OFFSET_KEY);
		return audioOffset==null ? sentence.getBeginTimestamp(0) : Float.parseFloat(audioOffset);
	}

	public static float getEndOffset(ProsodicSentenceData sentence) {
		int index = sentence.length()-1;
		while(index>0) {
			float endOffset = sentence.getEndTimestamp(index);

			if(endOffset!=LanguageConstants.DATA_UNDEFINED_VALUE) {
				return endOffset;
			}

			index--;
		}
		return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	public static float getBeginOffset(ProsodicSentenceData sentence, int wordIndex) {
		return sentence.getBeginTimestamp(wordIndex);
	}

	public static float getEndOffset(ProsodicSentenceData sentence, int wordIndex) {
		return sentence.getEndTimestamp(wordIndex);
	}

	public static float getBeginOffset(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		return sentence.getSyllableTimestamp(wordIndex, sylIndex);
	}

	public static float getEndOffset(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		return sentence.getSyllableTimestamp(wordIndex, sylIndex)
				+ sentence.getSyllableDuration(wordIndex, sylIndex);
	}

	public static long toMicroSeconds(float offset) {
		return (long) (offset*1_000_000);
	}

	public static long toFrames(float offset, float frameRate) {
		return (long) (offset*frameRate);
	}
}