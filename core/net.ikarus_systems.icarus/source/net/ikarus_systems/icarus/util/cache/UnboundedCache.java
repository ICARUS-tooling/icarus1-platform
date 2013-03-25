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
	 * @see net.ikarus_systems.icarus.util.cache.Cache#addItem(java.lang.Object, java.lang.Object)
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
	 * @see net.ikarus_systems.icarus.util.cache.Cache#getItem(java.lang.Object)
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
