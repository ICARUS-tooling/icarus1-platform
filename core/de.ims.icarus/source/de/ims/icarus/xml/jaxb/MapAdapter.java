/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.xml.jaxb;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.ims.icarus.xml.jaxb.JAXBUtils.EntryImp;
import de.ims.icarus.xml.jaxb.JAXBUtils.MapImp;


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
		if(v==null) {
			return null;
		}
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
		if(v==null) {
			return null;
		}
		MapImp<K, V> result = new MapImp<K, V>();
		for (Map.Entry<K, V> entry : v.entrySet()) {
			if(entry.getValue()!=null) {
				result.getList().add(new EntryImp<K, V>(entry));
			}
		}
		return result;
	}
}
