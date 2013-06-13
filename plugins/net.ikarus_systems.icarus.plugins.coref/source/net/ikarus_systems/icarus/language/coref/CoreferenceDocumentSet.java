/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.coref;

import java.util.ArrayList;
import java.util.List;

import net.ikarus_systems.icarus.util.CompactProperties;
import net.ikarus_systems.icarus.util.data.AbstractDataList;
import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSet extends AbstractDataList<CoreferenceDocumentData> {
	
	private List<CoreferenceDocumentData> items;
	
	private CompactProperties properties;

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return items==null ? 0 : items.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#get(int)
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
		
		properties.setProperty(key, value);
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
	 * @see net.ikarus_systems.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

}
