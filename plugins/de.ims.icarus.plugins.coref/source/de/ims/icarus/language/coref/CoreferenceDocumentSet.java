/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.DuplicateIdentifierException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSet extends CorefListMember<CoreferenceDocumentData> {
	
	private Map<String, CoreferenceDocumentData> idMap;
	
	private CoreferenceAllocation allocation;
	private CoreferenceAllocation defaultAllocation = new CoreferenceAllocation();
		
	public CoreferenceDocumentData getDocument(String documentId) {
		return idMap==null ? null : idMap.get(documentId);
	}
	
	public void add(CoreferenceDocumentData data) {
		if(idMap==null) {
			idMap = new HashMap<>();
		}
		if(idMap.containsKey(data.getDocumentIndex()))
			throw new DuplicateIdentifierException("Duplicate document id: "+data.getDocumentIndex()); //$NON-NLS-1$
		idMap.put(data.getId(), data);
		
		super.add(data);
	}
	
	public CoreferenceDocumentData newDocument(String id) {
		CoreferenceDocumentData data = new CoreferenceDocumentData(this, size());
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
}
