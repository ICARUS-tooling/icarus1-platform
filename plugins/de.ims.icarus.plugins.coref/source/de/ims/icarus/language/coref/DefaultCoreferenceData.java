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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref;

import de.ims.icarus.language.BasicSentenceData;
import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Primitive;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class DefaultCoreferenceData extends BasicSentenceData<CorefProperties> implements CoreferenceData, CorefConstants, LanguageConstants {

	private static final long serialVersionUID = 1641469565583964051L;

	@Reference(ReferenceType.UPLINK)
	protected CoreferenceDocumentData document;

	@Primitive
	protected int sentenceIndex = -1;

	public DefaultCoreferenceData() {
		super();
	}

	public DefaultCoreferenceData(String[] forms) {
		super(forms);
	}

	public DefaultCoreferenceData(CoreferenceDocumentData document, String[] forms) {
		this(forms);
		setDocument(document);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceData#getSpans()
	 */
	@Override
	public Span[] getSpans() {
		return sentenceIndex==-1 ? null
				: getDocument().getSpanSet().getSpans(sentenceIndex);
	}

	@Override
	public CoreferenceData clone() {
		DefaultCoreferenceData clone = new DefaultCoreferenceData(document, forms);
		clone.setProperties(cloneProperties());

		return clone;
	}

	@Override
	public CoreferenceDocumentData getDocument() {
		return document;
	}

	public void setDocument(CoreferenceDocumentData document) {
		if(forms!=null && forms.length==0) {
			return;
		}

		if(document==null)
			throw new NullPointerException("Invalid document"); //$NON-NLS-1$

		this.document = document;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	@Override
	public Object getProperty(String key) {
		switch (key) {

		case SENTENCE_INDEX_KEX:
			return getSentenceIndex();

		default:
			return super.getProperty(key);
		}
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getIndex()
	 */
	@Override
	public int getIndex() {
		return sentenceIndex;
	}
}
