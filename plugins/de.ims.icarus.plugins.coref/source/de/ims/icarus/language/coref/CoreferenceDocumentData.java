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

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.data.AbstractDataList;
import de.ims.icarus.util.data.ContentType;


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
}
