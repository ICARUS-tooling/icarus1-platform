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

 * $Revision: 459 $
 * $Date: 2016-05-16 23:25:11 +0200 (Mo, 16 Mai 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.prosody/source/de/ims/icarus/plugins/prosody/pattern/CorefDataProxy.java $
 *
 * $LastChangedDate: 2016-05-16 23:25:11 +0200 (Mo, 16 Mai 2016) $
 * $LastChangedRevision: 459 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.coref.pattern;

import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.DocumentSet;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.Span;

public final class CorefDataProxy {

	private DocumentSet documentSet;

	private DocumentData document;
	private SentenceData sentence;
	private Span span;
	private Edge edge;

	private int documentIndex, sentenceIndex, wordIndex;

	public DocumentSet getDocumentSet() {
		return documentSet;
	}

	public DocumentData getDocument() {
		return document;
	}

	public SentenceData getSentence() {
		return sentence;
	}

	public int getWordIndex() {
		return wordIndex;
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

	public boolean isSpanSet() {
		return span!=null;
	}

	public boolean isEdgeSet() {
		return edge!=null;
	}

	public void set(DocumentData document) {
		clear();
		this.documentIndex = document.getDocumentIndex();
		this.document = document;
		this.documentSet = document.getDocumentSet();
	}

	public void set(DocumentData document, Span span) {
		clear();
		this.documentIndex = document.getDocumentIndex();
		this.document = document;
		this.documentSet = document.getDocumentSet();
		this.span = span;
		if(!span.isROOT() && !span.isVirtual()) {
			this.sentence = document.get(span.getSentenceIndex());
		}
	}

	public void set(DocumentData document, Edge edge) {
		clear();
		this.documentIndex = document.getDocumentIndex();
		this.document = document;
		this.documentSet = document.getDocumentSet();
		this.edge = edge;
	}

	public void set(CoreferenceData sentence) {
		set(sentence, -1, -1);
	}

	public void set(CoreferenceData sentence, int wordIndex) {
		set(sentence, wordIndex, -1);
	}

	public void set(CoreferenceData sentence, int wordIndex, int syllableIndex) {
		if (sentence == null)
			throw new NullPointerException("Invalid sentence"); //$NON-NLS-1$

		this.document = sentence.getDocument();
		this.documentSet = document.getDocumentSet();
		this.span = null;
		this.edge = null;
		this.sentence = sentence;
		this.sentenceIndex = sentence.getIndex();
		this.wordIndex = wordIndex;
	}

	public void set(CorefDataProxy source) {
		documentSet = source.documentSet;
		document = source.document;
		sentence = source.sentence;
		span = source.span;
		edge = source.edge;
		documentIndex = source.documentIndex;
		sentenceIndex = source.sentenceIndex;
		wordIndex = source.wordIndex;
	}

	public void setWordIndex(int wordIndex) {
		this.wordIndex = wordIndex;
	}

	public void setDocumentIndex(int documentIndex) {
		this.documentIndex = documentIndex;
		document = (DocumentData) documentSet.get(documentIndex);
		sentenceIndex = wordIndex = -1;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
		sentence = document.get(sentenceIndex);
		wordIndex = -1;
	}

	public Span getSpan() {
		return span;
	}

	public Edge getEdge() {
		return edge;
	}

	public void setSpan(Span span) {
		this.span = span;
	}

	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	public void clear() {
		document = null;
		sentence = null;
		span = null;
		edge = null;
		documentIndex = sentenceIndex = wordIndex = -1;
	}
}