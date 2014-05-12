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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.model.standard;

import java.util.ArrayList;

import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Primitive;
import de.ims.icarus.util.mem.Reference;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class MemberCache<E extends Markable> {

	@Reference
	private ArrayList<E> pool;

	@Primitive
	private final int poolSize;

	@Link
	private transient Entry table[];

	@Primitive
	private transient int count;

	@Primitive
	private int threshold;

	@Primitive
	private final float loadFactor;

	/**
	 * Inner class that acts as a data structure to create a new entry in the
	 * table.
	 */
	@HeapMember
	private static class Entry {
		@Primitive
		long key;
		@Link(cache=true)
		Object value;
		@Link
		Entry next;
		@Primitive
		int refCount = 1;

		/**
		 * Create a new entry with the given values.
		 *
		 * @param key The key used to enter this in the table
		 * @param value The value for this key
		 * @param next A reference to the next entry in the table
		 */
		protected Entry(long key, Object value, Entry next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

	public MemberCache() {
		this(10000, 10000, 0.85f);
	}

	/**
	 * Constructs a new, empty hash-table with the specified initial capacity and
	 * default load factor, which is <code>0.75</code>.
	 *
	 * @param initialCapacity the initial capacity of the hash-table.
	 * @throws IllegalArgumentException if the initial capacity is less than zero.
	 */
	public MemberCache(int initialCapacity) {
		this(initialCapacity, 10000, 0.85f);
	}

	public MemberCache(int initialCapacity, int poolSize, float loadFactor) {

		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal capacity (negative): " //$NON-NLS-1$
					+ initialCapacity);
		if (poolSize < 0)
			throw new IllegalArgumentException("Illegal pool-size (negative): " //$NON-NLS-1$
					+ poolSize);
		if (loadFactor <= 0)
			throw new IllegalArgumentException("Illegal load-factor (zero or less): " + loadFactor); //$NON-NLS-1$

		if (initialCapacity == 0) {
			initialCapacity = 1;
		}

		this.loadFactor = loadFactor;
		this.poolSize = poolSize;
		table = new Entry[initialCapacity];
		threshold = (int) (initialCapacity * loadFactor);
	}

	public int cachedMemberCount() {
		return count;
	}

	public int pooledMemberCount() {
		return pool==null ? 0 : pool.size();
	}

	public boolean isEmpty() {
		return count == 0;
	}

	private Entry fetch(long key) {
		Entry tab[] = table;
		int hash = (int) key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.key == key) {
				return e;
			}
		}

		return null;
	}

	private Entry add(long key, E member) {
		// Makes sure the key is not already in the hash-table.
		Entry tab[] = table;
		int hash = (int) key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.key == key) {
				return null;
			}
		}

		if (count >= threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = table;
			index = (hash & 0x7FFFFFFF) % tab.length;
		}

		// Creates the new entry.
		Entry e = new Entry(key, member, tab[index]);
		tab[index] = e;
		count++;

		return e;
	}

	private E remove(long key) {
		Entry tab[] = table;
		int hash = (int) key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
			if (e.key == key) {
				if (prev != null) {
					prev.next = e.next;
				} else {
					tab[index] = e.next;
				}
				count--;
				@SuppressWarnings("unchecked")
				E oldValue = (E) e.value;
				e.value = null;
				return oldValue;
			}
		}

		return null;
	}

	/**
	 * Checks whether or not an entry for a given key is already present
	 * @param key
	 * @return
	 */
	public boolean isCached(long key) {
		return fetch(key)!=null;
	}

	/**
	 * Retrieves the current reference counter for a given key
	 * @param key
	 * @return
	 */
	public int getRefCount(long key) {
		Entry entry = fetch(key);
		if(entry==null)
			throw new IllegalArgumentException("No member cached for key: "+key); //$NON-NLS-1$
		return entry.refCount;
	}

	/**
	 * Creates a new entry to map the given key to the given member.
	 *
	 * @param key
	 * @param member
	 * @throws IllegalArgumentException if there already is an entry for the given key
	 */
	public void registerMember(long key, E member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		Entry entry = add(key, member);
		if(entry==null)
			throw new IllegalArgumentException("Member already registered: "); //$NON-NLS-1$
	}

	/**
	 * Looks up the member registered for the given key and returns it if present.
	 * @param key
	 * @return
	 */
	public E lookupMember(long key) {
		Entry entry = fetch(key);
		if(entry==null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		E member = (E) entry.value;
		return member;
	}

	/**
	 * Much like {@link #lookupMember(long)} this methods attempts to lookup a specific
	 * member. In addition, it increments the reference counter associated with the given key.
	 *
	 * @param key
	 * @return
	 */
	public E aquireMember(long key) {
		Entry entry = fetch(key);
		if(entry==null) {
			return null;
		}

		entry.refCount++;

		@SuppressWarnings("unchecked")
		E member = (E) entry.value;
		return member;
	}

	/**
	 * Called when a segment destroys its contents and frees a markable object.
	 * This method internally decreases the reference counter for the markable and
	 * once that counter reaches {@code 0}, removes the entry from the cache.
	 * In the case the entry is removed, returns the mapped {@link Markable}, otherwise
	 * {@code null}.
	 * <p>
	 * Note that the cache does <b>not</b> automatically move released members into the
	 * object pool! Neither does it perform any special recycling operations on them to
	 * prepare them for getting pooled. It is up to surrounding client code to call
	 * the proper methods and finally push the member back to the cache again for pooling.
	 *
	 * @param key
	 * @return
	 */
	public E releaseMember(long key) {
		Entry entry = fetch(key);
		if(entry==null)
			throw new IllegalArgumentException("No member registered for key: "+key); //$NON-NLS-1$

		if (--entry.refCount <= 0) {
			@SuppressWarnings("unchecked")
			E member = (E) entry.value;
			remove(key);
			return member;
		}

		return null;
	}

	/**
	 * Adds the given member to the internal object pool.
	 * @param member
	 */
	public void recycleMember(E member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		if(pool==null) {
			synchronized (pool) {
				if(pool==null) {
					pool = new ArrayList<>(poolSize);
				}
			}
		}

		pool.add(member);
	}

	/**
	 * Returns the last pooled member or {@code null} if there currently is
	 * no object pooled.
	 * @return
	 */
	public E getPooledMember() {
		if(pool==null || pool.isEmpty()) {
			return null;
		}

		return pool.remove(pool.size()-1);
	}

	/**
	 * Increases the capacity of and internally reorganizes this hash-table, in
	 * order to accommodate and access its entries more efficiently.
	 *
	 * This method is called automatically when the number of keys in the
	 * hash-table exceeds this hash-table's capacity and load factor.
	 */
	private void rehash() {
		int oldCapacity = table.length;
		Entry oldMap[] = table;

		int newCapacity = (oldCapacity * 2) + 1;
		Entry newMap[] = new Entry[newCapacity];

		threshold = (int) (newCapacity * loadFactor);
		table = newMap;

		for (int i = oldCapacity; i-- > 0;) {
			for (Entry old = oldMap[i]; old != null;) {
				Entry e = old;
				old = old.next;

				int index = ((int)e.key & 0x7FFFFFFF) % newCapacity;
				e.next = newMap[index];
				newMap[index] = e;
			}
		}
	}

	public synchronized void clear() {
		Entry tab[] = table;
		for (int index = tab.length; --index >= 0;) {
			tab[index] = null;
		}
		count = 0;
	}
}
