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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.ikarus_systems.icarus.xml.JAXBUtils.EntryImp;
import net.ikarus_systems.icarus.xml.JAXBUtils.MapImp;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class MapAdapter<K, V> extends XmlAdapter<MapImp<K, V>, Map<K, V>> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public Map<K, V> unmarshal(MapImp<K, V> v) throws Exception {
		HashMap<K, V> result = new HashMap<K, V>();
		for (EntryImp<K, V> entry : v.getList()) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public MapImp<K, V> marshal(Map<K, V> v) throws Exception {
		MapImp<K, V> result = new MapImp<K, V>();
		for (Map.Entry<K, V> entry : v.entrySet()) {
			result.getList().add(new EntryImp<K, V>(entry));
		}
		return result;
	}
}
