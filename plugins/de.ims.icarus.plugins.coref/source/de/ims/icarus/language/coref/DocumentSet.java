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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.DuplicateIdentifierException;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class DocumentSet extends CorefListMember<DocumentData> {

	@Link
	private Map<String, DocumentData> idMap;

	@Link
	private CoreferenceAllocation allocation;
	@Link
	private CoreferenceAllocation defaultAllocation = new CoreferenceAllocation();

	public DocumentData getDocument(String documentId) {
		return idMap==null ? null : idMap.get(documentId);
	}

	public Set<String> getDocumentIds() {
		Set<String> result = null;
		if(idMap!=null) {
			result = idMap.keySet();
		}
		if(result==null) {
			result = Collections.emptySet();
		}

		return result;
	}

	public Collection<DocumentData> getDocuments() {
		return CollectionUtils.getCollectionProxy(items);
	}

	@Override
	public void add(DocumentData data) {
		if(idMap==null) {
			idMap = new HashMap<>();
		}
		if(idMap.containsKey(data.getId()))
			throw new DuplicateIdentifierException("Duplicate document id: "+data.getDocumentIndex()); //$NON-NLS-1$
		idMap.put(data.getId(), data);

		super.add(data);
	}

	public DocumentData newDocument(String id) {
		DefaultCoreferenceDocumentData data = new DefaultCoreferenceDocumentData(this, size());
		data.setId(id);
		add(data);
		return data;
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

	public CoreferenceAllocation getAllocation() {
		return allocation==null ? getDefaultAllocation() : allocation;
	}

	public void setAllocation(CoreferenceAllocation allocation) {
		this.allocation = allocation;
	}

	public CoreferenceAllocation getDefaultAllocation() {
		return defaultAllocation;
	}

	/**
	 * @see de.ims.icarus.language.coref.CorefListMember#free()
	 */
	@Override
	public void free() {
		if(idMap!=null) {
			idMap.clear();
		}

		idMap = null;
		allocation = null;
		defaultAllocation = new CoreferenceAllocation();

		super.free();
	}
}
