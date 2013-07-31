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
package de.ims.icarus.util.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UnboundedCache<K, V> extends HashMap<K, SoftReference<V>> implements Cache<K, V> {

	private static final long serialVersionUID = -7567083572311257863L;

	/**
	 * @see de.ims.icarus.util.cache.Cache#addItem(java.lang.Object, java.lang.Object)
	 */
	@Override
	public synchronized void addItem(K key, V value) {
		SoftReference<V> ref = get(key);
		if(ref!=null && value.equals(ref.get())) {
			return;
		}

		ref = new SoftReference<>(value);
		put(key, ref);
	}

	/**
	 * @see de.ims.icarus.util.cache.Cache#getItem(java.lang.Object)
	 */
	@Override
	public synchronized V getItem(K key) {
		SoftReference<V> ref = get(key);
		if(ref==null) {
			return null;
		}
		V value = ref.get();
		if(value==null) {
			remove(key);
		}
		return value;
	}

}
