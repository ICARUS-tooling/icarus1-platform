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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {

	private static final long serialVersionUID = -6810323016197022553L;
	
	protected int maxSize = Integer.MAX_VALUE;

	/**
	 * Creates an {@code LRUCache} with an initial maximum
	 * size of 20.
	 */
	public LRUCache() {
		this(20);
	}

	/**
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public LRUCache(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor, true);
		
		setMaxSize(initialCapacity-1);
	}

	/**
	 * @param initialCapacity
	 */
	public LRUCache(int initialCapacity) {
		this(initialCapacity, 1.0f);
	}

	/**
	 * @param m
	 */
	public LRUCache(Map<? extends K, ? extends V> m) {
		super(m);
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > maxSize;
     }

	/**
	 * @return the maxSize
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * @param maxSize the maxSize to set
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * @see de.ims.icarus.util.cache.Cache#addItem(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void addItem(K key, V value) {
		put(key, value);
	}

	/**
	 * @see de.ims.icarus.util.cache.Cache#getItem(java.lang.Object)
	 */
	@Override
	public V getItem(K key) {
		return get(key);
	}
}
