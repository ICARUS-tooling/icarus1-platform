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

import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;

public final class ProsodyData {

	private CoreferenceDocumentSet documentSet;

	private ProsodicDocumentData document;
	private ProsodicSentenceData sentence;

	private int documentIndex, sentenceIndex, wordIndex, syllableIndex;

	public CoreferenceDocumentSet getDocumentSet() {
		return documentSet;
	}

	public ProsodicDocumentData getDocument() {
		return document;
	}

	public ProsodicSentenceData getSentence() {
		return sentence;
	}

	public int getWordIndex() {
		return wordIndex;
	}

	public int getSyllableIndex() {
		return syllableIndex;
	}

	public int getDocumentIndex() {
		return documentIndex;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public boolean isDocumentIndexSet() {
		return documentIndex!=-1;
	}

	public boolean isSentenceIndexSet() {
		return sentenceIndex!=-1;
	}

	public boolean isWordIndexSet() {
		return wordIndex!=-1;
	}

	public boolean isSyllableIndexSet() {
		return syllableIndex!=-1;
	}

	public void set(ProsodicDocumentData document) {
		clear();
		this.documentIndex = document.getDocumentIndex();
		this.document = document;
		this.documentSet = document.getDocumentSet();
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

		this.document = sentence.getDocument();
		this.documentSet = document.getDocumentSet();
		this.sentence = sentence;
		this.sentenceIndex = sentence.getIndex();
		this.wordIndex = wordIndex;
		this.syllableIndex = syllableIndex;
	}

	public void set(ProsodyData source) {
		documentSet = source.documentSet;
		document = source.document;
		sentence = source.sentence;
		documentIndex = source.documentIndex;
		sentenceIndex = source.sentenceIndex;
		wordIndex = source.wordIndex;
		syllableIndex = source.syllableIndex;
	}

	public void setWordIndex(int wordIndex) {
		this.wordIndex = wordIndex;
	}

	public void setSyllableIndex(int syllableIndex) {
		this.syllableIndex = syllableIndex;
	}

	public void setDocumentIndex(int documentIndex) {
		this.documentIndex = documentIndex;
		document = (ProsodicDocumentData) documentSet.get(documentIndex);
		sentenceIndex = wordIndex = syllableIndex = -1;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
		sentence = document.get(sentenceIndex);
		wordIndex = syllableIndex = -1;
	}

	public void clear() {
		document = null;
		sentence = null;
		documentIndex = sentenceIndex = wordIndex = syllableIndex = -1;
	}
}