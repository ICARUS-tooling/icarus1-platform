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
package de.ims.icarus.model.standard.driver.cache;

import de.ims.icarus.model.api.members.Markable;
import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Primitive;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
@AccessControl(AccessPolicy.DENY)
public class MemberCache {

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
	protected static class Entry {
		@Primitive
		long key;
		@Link(cache=true)
		Markable value;
		@Link
		Entry next;

		/**
		 * Create a new entry with the given values.
		 *
		 * @param key The key used to enter this in the table
		 * @param value The value for this key
		 * @param next A reference to the next entry in the table
		 */
		protected Entry(long key, Markable value, Entry next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

	public MemberCache() {
		this(10000, 0.85f);
	}

	/**
	 * Constructs a new, empty hash-table with the specified initial capacity and
	 * default load factor, which is <code>0.75</code>.
	 *
	 * @param initialCapacity the initial capacity of the hash-table.
	 * @throws IllegalArgumentException if the initial capacity is less than zero.
	 */
	public MemberCache(int initialCapacity) {
		this(initialCapacity, 0.85f);
	}

	public MemberCache(int initialCapacity, float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal capacity (negative): " //$NON-NLS-1$
					+ initialCapacity);
		if (loadFactor <= 0)
			throw new IllegalArgumentException("Illegal load-factor (zero or less): " + loadFactor); //$NON-NLS-1$

		if (initialCapacity == 0) {
			initialCapacity = 1;
		}

		this.loadFactor = loadFactor;
		table = new Entry[initialCapacity];
		threshold = (int) (initialCapacity * loadFactor);
	}

	@AccessRestriction(AccessMode.ALL)
	public int size() {
		return count;
	}

	@AccessRestriction(AccessMode.ALL)
	public boolean isEmpty() {
		return count == 0;
	}

	@AccessRestriction(AccessMode.ALL)
	protected Entry fetch(long key) {
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

	@AccessRestriction(AccessMode.MANAGE)
	protected Entry add(long key, Markable member) {
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
		Entry e = newEntry(key, member, tab[index]);
		tab[index] = e;
		count++;

		return e;
	}

	protected Entry newEntry(long key, Markable member, Entry next) {
		return new Entry(key, member, next);
	}

	@AccessRestriction(AccessMode.MANAGE)
	protected Markable remove(long key) {
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
				Markable oldValue = e.value;
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
	@AccessRestriction(AccessMode.ALL)
	public boolean isCached(long key) {
		return fetch(key)!=null;
	}

	/**
	 * Creates a new entry to map the given key to the given member.
	 *
	 * @param key
	 * @param member
	 * @throws IllegalArgumentException if there already is an entry for the given key
	 */
	@AccessRestriction(AccessMode.MANAGE)
	public void registerMember(long key, Markable member) {
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
	@AccessRestriction(AccessMode.ALL)
	public Markable lookupMember(long key) {
		Entry entry = fetch(key);
		if(entry==null) {
			return null;
		}

		return entry.value;
	}

	@AccessRestriction(AccessMode.MANAGE)
	public Markable removeMember(long key) {
		return remove(key);
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

	@AccessRestriction(AccessMode.MANAGE)
	public synchronized void clear() {
		Entry tab[] = table;
		for (int index = tab.length; --index >= 0;) {
			tab[index] = null;
		}
		count = 0;
	}

	@AccessRestriction(AccessMode.MANAGE)
	public synchronized void recycle(MemberPool pool) {

		Entry tab[] = table;
		for (int i = tab.length; i-- > 0;) {
			for (Entry e = tab[i]; e != null; e = e.next) {
				if(!pool.recycle(e.value)) {
					break;
				}
			}
			tab[i] = null;
		}
	}
}
