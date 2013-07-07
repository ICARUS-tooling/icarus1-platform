/*
 * $Revision: 17 $
 * $Date: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/util/cache/UnboundedCache.java $
 *
 * $LastChangedDate: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $ 
 * $LastChangedRevision: 17 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.util.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * @author Markus Gärtner
 * @version $Id: UnboundedCache.java 17 2013-03-25 00:44:03Z mcgaerty $
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
