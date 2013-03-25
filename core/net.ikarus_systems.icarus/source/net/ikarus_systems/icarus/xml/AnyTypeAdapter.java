/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnyTypeAdapter extends XmlAdapter<Object, Object> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public Object unmarshal(Object v) throws Exception {
		return v;
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public Object marshal(Object v) throws Exception {
		return v;
	}

}
