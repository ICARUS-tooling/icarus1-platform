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

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;

public final class ProsodyData {
	private ProsodicSentenceData sentence;
	private int wordIndex;
	private int syllableIndex;

	public ProsodicSentenceData getSentence() {
		return sentence;
	}

	public int getWordIndex() {
		return wordIndex;
	}

	public int getSyllableIndex() {
		return syllableIndex;
	}

	public boolean isWordIndexSet() {
		return wordIndex!=-1;
	}

	public boolean isSyllableIndexSet() {
		return syllableIndex!=-1;
	}

	public void set(ProsodicSentenceData sentence) {
		set(sentence, -1, -1);
	}

	public void set(ProsodicSentenceData sentence, int wordIndex) {
		set(sentence, wordIndex, -1);
	}

	public void set(ProsodicSentenceData sentence, int wordIndex, int syllableIndex) {
		if (sentence == null)
			throw new NullPointerException("Invalid sentence"); //$NON-NLS-1$

		this.sentence = sentence;
		this.wordIndex = wordIndex;
		this.syllableIndex = syllableIndex;
	}

	public void set(ProsodyData source) {
		sentence = source.sentence;
		wordIndex = source.wordIndex;
		syllableIndex = source.syllableIndex;
	}

	public void setWordIndex(int wordIndex) {
		this.wordIndex = wordIndex;
	}

	public void setSyllableIndex(int syllableIndex) {
		this.syllableIndex = syllableIndex;
	}

	public void clear() {
		sentence = null;
		wordIndex = syllableIndex = -1;
	}
}