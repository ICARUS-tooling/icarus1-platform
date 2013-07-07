/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.xml.jaxb;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.ikarus_systems.icarus.xml.jaxb.JAXBUtils.EntryImp;
import net.ikarus_systems.icarus.xml.jaxb.JAXBUtils.MapImp;

/**
 * {@code XmlAdapter} implementation that converts a {@code Map} object
 * into a wrapper that holds a {@code List} of elements that correspond
 * to the entries in the map. Both the {@link #marshal(Map)} and
 * {@link #unmarshal(MapImp)} method ignore <i>null-mappings</i>, i.e. they
 * will ignore each entry that maps its key to a {@code null} value. This is
 * done to help minimize the amount of xml data being generated. If it is
 * required to preserve {@code null} values (e.g. the mere existence of a key
 * in the map is checked for) then it is recommended to implement a special
 * adapter or use dummy values.
 * 
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
			if(entry.getValue()!=null) {
				result.put(entry.getKey(), entry.getValue());
			}
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
			if(entry.getValue()!=null) {
				result.getList().add(new EntryImp<K, V>(entry));
			}
		}
		return result;
	}
}
