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

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.data.AbstractDataList;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSet extends AbstractDataList<CoreferenceDocumentData> {
	
	private List<CoreferenceDocumentData> items;
	
	private CompactProperties properties;

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return items==null ? 0 : items.size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public CoreferenceDocumentData get(int index) {
		return items==null ? null : items.get(index);
	}
	
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}
	
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		
		properties.put(key, value);
	}
	
	public void add(CoreferenceDocumentData data) {
		if(data==null)
			throw new IllegalArgumentException("Invalid document data"); //$NON-NLS-1$
		
		if(items==null) {
			items = new ArrayList<>();
		}
		
		items.add(data);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

}
