/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.cache;

import java.util.HashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UnboundedCache<K, V> extends HashMap<K, V> implements Cache<K, V> {

	private static final long serialVersionUID = -7567083572311257863L;

	/**
	 * @see net.ikarus_systems.icarus.util.cache.Cache#addItem(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void addItem(K key, V value) {
		put(key, value);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.cache.Cache#getItem(java.lang.Object)
	 */
	@Override
	public V getItem(K key) {
		return get(key);
	}

}
