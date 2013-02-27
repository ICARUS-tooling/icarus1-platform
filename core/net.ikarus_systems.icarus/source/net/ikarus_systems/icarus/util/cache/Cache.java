/**
 * 
 */
package net.ikarus_systems.icarus.util.cache;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Cache<K, V> {

	void clear();
	
	void addItem(K key, V value);
	
	V getItem(K key);
}
