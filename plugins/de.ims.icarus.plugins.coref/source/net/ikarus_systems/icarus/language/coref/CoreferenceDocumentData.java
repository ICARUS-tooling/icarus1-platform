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

import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.util.CompactProperties;
import net.ikarus_systems.icarus.util.data.AbstractDataList;
import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentData extends AbstractDataList<SentenceData> implements SentenceDataList {

	public static final String DOCUMENT_ID_PROPERTY = "documentId"; //$NON-NLS-1$
	public static final String DOCUMENT_HEADER_PROPERTY = "documentHeader"; //$NON-NLS-1$
	
	protected List<CoreferenceData> items;
	protected CompactProperties properties;
	
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}
	
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		
		properties.put(key, value);
	}

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
	public CoreferenceData get(int index) {
		return items==null ? null : items.get(index);
	}
	
	public void add(CoreferenceData data) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
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
		return CoreferenceUtils.getCoreferenceContentType();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#supportsType(net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return type==DataType.SYSTEM;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public CoreferenceData get(int index, DataType type) {
		return get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType, net.ikarus_systems.icarus.language.AvailabilityObserver)
	 */
	@Override
	public CoreferenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return get(index);
	}
}
