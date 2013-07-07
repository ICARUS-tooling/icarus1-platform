/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContentTypeAdapter extends XmlAdapter<String, ContentType> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public ContentType unmarshal(String v) throws Exception {
		return ContentTypeRegistry.getInstance().getType(v);
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public String marshal(ContentType v) throws Exception {
		return v.getId();
	}

}
