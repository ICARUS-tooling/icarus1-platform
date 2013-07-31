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

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentData extends CorefListMember<SentenceData> implements SentenceDataList {

	public static final String DOCUMENT_ID_PROPERTY = "documentId"; //$NON-NLS-1$
	public static final String DOCUMENT_HEADER_PROPERTY = "documentHeader"; //$NON-NLS-1$
	
	protected CoreferenceDocumentSet documentSet;
	
	protected final int documentIndex;
	
	protected String id;
	
	public CoreferenceDocumentData(CoreferenceDocumentSet documentSet, int documentIndex) {		
		setDocumentSet(documentSet);
		this.documentIndex = documentIndex;
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public CoreferenceData get(int index) {
		return (CoreferenceData) super.get(index);
	}
	
	public void add(CoreferenceData data) {
		super.add(data);
	}
	
	public DefaultCoreferenceData newData(String[] forms) {
		DefaultCoreferenceData data = new DefaultCoreferenceData(this, forms);
		data.setSentenceIndex(size());
		add(data);
		
		return data;
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceContentType();
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return type==DataType.SYSTEM;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public CoreferenceData get(int index, DataType type) {
		return get(index);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public CoreferenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return get(index);
	}

	public CoreferenceDocumentSet getDocumentSet() {
		return documentSet;
	}

	public void setDocumentSet(CoreferenceDocumentSet documentSet) {
		if(documentSet==null)
			throw new IllegalArgumentException("Invalid document-set"); //$NON-NLS-1$
		
		this.documentSet = documentSet;
	}
	
	public SpanSet getSpanSet() {
		return getDocumentSet().getAllocation().getSpanSet(getId());
	}
	
	public EdgeSet getEdgeSet() {
		return getDocumentSet().getAllocation().getEdgeSet(getId());
	}
	
	public SpanSet getDefaultSpanSet() {
		return getDocumentSet().getDefaultAllocation().getSpanSet(getId());
	}
	
	public EdgeSet getDefaultEdgeSet() {
		return getDocumentSet().getDefaultAllocation().getEdgeSet(getId());
	}

	public int getDocumentIndex() {
		return documentIndex;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
