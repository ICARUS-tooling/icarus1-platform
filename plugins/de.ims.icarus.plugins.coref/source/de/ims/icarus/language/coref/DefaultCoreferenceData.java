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

import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Primitive;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class DefaultCoreferenceData extends CorefMember implements CoreferenceData {

	private static final long serialVersionUID = 1641469565583964051L;

	@Link
	protected final String[] forms;

	@Reference(ReferenceType.UPLINK)
	protected CoreferenceDocumentData document;

	@Primitive
	protected int sentenceIndex = -1;

	public DefaultCoreferenceData(CoreferenceDocumentData document, String[] forms) {
		if(forms==null)
			throw new NullPointerException("Invalid forms array"); //$NON-NLS-1$

		this.forms = forms;
		setDocument(document);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceData#getProperty(int, java.lang.String)
	 */
	@Override
	public Object getProperty(int index, String key) {
		return getProperty(key+'_'+index);
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		return forms[index];
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return length()==0;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#length()
	 */
	@Override
	public int length() {
		return forms.length;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return null;
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
	public String toString() {
		return LanguageUtils.combine(this);
	}

	/**
	 * @see de.ims.icarus.ui.helper.TextItem#getText()
	 */
	@Override
	public String getText() {
		return LanguageUtils.combine(this);
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
