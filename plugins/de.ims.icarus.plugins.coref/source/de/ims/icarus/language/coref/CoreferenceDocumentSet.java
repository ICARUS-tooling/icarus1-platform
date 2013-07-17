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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.io.Reader;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.DuplicateIdentifierException;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;


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
	
	public static CoreferenceDocumentSet loadDocumentSet(Reader<CoreferenceDocumentData> reader,
			Location location, Options options) throws IOException, UnsupportedLocationException, 
				UnsupportedFormatException {

		CoreferenceDocumentSet documentSet = new CoreferenceDocumentSet();
		options.put("documentSet", documentSet); //$NON-NLS-1$
		reader.init(location, options);
		
		while(reader.next()!=null);
		
		return documentSet;
	}
}
