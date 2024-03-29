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
import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class DefaultCoreferenceData extends BasicSentenceData<CompactProperties> implements CoreferenceData,
		CorefConstants, LanguageConstants {

	private static final long serialVersionUID = 1641469565583964051L;

	@Reference(ReferenceType.UPLINK)
	protected DocumentData document;

	public DefaultCoreferenceData() {
		super();
	}

	public DefaultCoreferenceData(String[] forms) {
		super(forms);
	}

	public DefaultCoreferenceData(DocumentData document, String[] forms) {
		this(forms);
		setDocument(document);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceData#getSpans()
	 */
	@Override
	public Span[] getSpans() {
		return index==-1 ? null
				: getDocument().getSpanSet().getSpans(index);
	}

	@Override
	public DefaultCoreferenceData clone() {
		return (DefaultCoreferenceData) super.clone();
	}

	@Override
	public DocumentData getDocument() {
		return document;
	}

	public void setDocument(DocumentData document) {
		if(forms!=null && forms.length==0) {
			return;
		}

		if(document==null)
			throw new NullPointerException("Invalid document"); //$NON-NLS-1$

		this.document = document;
	}

	@Override
	public Object getProperty(String key) {
		switch (key) {

		case SENTENCE_INDEX_KEX:
			return getIndex();

		default:
			return super.getProperty(key);
		}
	}
}
