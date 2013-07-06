/**
 * 
 */
package net.ikarus_systems.icarus.util.cache;

/**
 * 
 * @author Markus Gärtner
 * @version $Id: Cache.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public interface Cache<K, V> {

	void clear();
	
	void addItem(K key, V value);
	
	V getItem(K key);
}
