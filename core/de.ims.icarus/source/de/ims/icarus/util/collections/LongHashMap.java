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
package de.ims.icarus.util.collections;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Markus
 * @version $Id$
 *
 */
public class LongHashMap<E extends Object> {

	/**
	 * The hash table data.
	 */
	private transient Entry table[];

	/**
	 * The total number of entries in the hash table.
	 */
	private transient int count;

	/**
	 * The table is rehashed when its size exceeds this threshold. (The value of
	 * this field is (int)(capacity * loadFactor).)
	 *
	 * @serial
	 */
	private int threshold;

	/**
	 * The load factor for the hash-table.
	 *
	 * @serial
	 */
	private float loadFactor;

	/**
	 * Inner class that acts as a data structure to create a new entry in the
	 * table.
	 */
	private static class Entry {
		long key;
		Object value;
		Entry next;

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

	/**
	 * Constructs a new, empty hash-table with a default capacity and load
	 * factor, which is <code>20</code> and <code>0.75</code> respectively.
	 */
	public LongHashMap() {
		this(20, 0.75f);
	}

	/**
	 * Constructs a new, empty hash-table with the specified initial capacity and
	 * default load factor, which is <code>0.75</code>.
	 *
	 * @param initialCapacity the initial capacity of the hash-table.
	 * @throws IllegalArgumentException if the initial capacity is less than zero.
	 */
	public LongHashMap(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	/**
	 * Constructs a new, empty hash-table with the specified initial capacity and
	 * the specified load factor.
	 *
	 * @param initialCapacity the initial capacity of the hash-table.
	 * @param loadFactor the load factor of the hash-table.
	 * @throws IllegalArgumentException if the initial capacity is less than zero, or if the load
	 *             factor is non-positive.
	 */
	public LongHashMap(int initialCapacity, float loadFactor) {

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

	/**
	 * Returns the number of keys in this hash-table.
	 *
	 * @return the number of keys in this hash-table.
	 */
	public int size() {
		return count;
	}

	/**
	 * Tests if this hash-table maps no keys to values.
	 *
	 * @return {@code true} if this hash-table maps no keys to values;
	 *         {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return count == 0;
	}

	/**
	 * Tests if some key maps into the specified value in this hash-table. This
	 * operation is more expensive than the <code>containsKey</code> method.
	 *
	 * Note that this method is identical in functionality to containsValue,
	 * (which is part of the Map interface in the collections framework).
	 *
	 * @param value
	 *            a value to search for.
	 * @return {@code true} if and only if some key maps to the
	 *         {@code value} argument in this hash-table as determined by
	 *         the {@link #equals(Object)} method; {@code false} otherwise.
	 * @throws NullPointerException
	 *             if the value is {@code null}.
	 * @see #containsKey(int)
	 * @see java.util.Map
	 */
	public boolean containsValue(E value) {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$

		Entry tab[] = table;
		for (int i = tab.length; i-- > 0;) {
			for (Entry e = tab[i]; e != null; e = e.next) {
				if (value.equals(e.value)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Tests if the specified object is a key in this hash-table.
	 *
	 * @param key
	 *            possible key.
	 * @return {@code true} if and only if the specified object is a key in
	 *         this hash-table, as determined by the {@link #equals(Object)} method;
	 *         {@code false} otherwise.
	 * @see #containsValue(Object)
	 */
	public boolean containsKey(long key) {
		Entry tab[] = table;
		int hash = (int) key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.key == key) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the value to which the specified key is mapped in this map.
	 *
	 * @param key
	 *            a key in the hash-table.
	 * @return the value to which the key is mapped in this hash-table;
	 *         {@code null} if the key is not mapped to any value in this
	 *         hash-table.
	 * @see #put(int, Object)
	 */
	@SuppressWarnings("unchecked")
	public E get(long key) {
		Entry tab[] = table;
		int hash = (int) key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.key == key) {
				return (E) e.value;
			}
		}
		return null;
	}

	/**
	 * Increases the capacity of and internally reorganizes this hash-table, in
	 * order to accommodate and access its entries more efficiently.
	 *
	 * This method is called automatically when the number of keys in the
	 * hash-table exceeds this hash-table's capacity and load factor.
	 */
	protected void rehash() {
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

	/**
	 * Maps the specified {@code key} to the specified {@code value}
	 * in this hash-table.
	 *
	 * The value can be retrieved by calling the {@link #get(long)} method with a
	 * key that is equal to the original key.
	 *
	 * @param key the hash-table key.
	 * @param value the value.
	 * @return the previous value of the specified key in this hash-table, or
	 *         {@code null} if it did not have one.
	 * @see #get(int)
	 */
	public E put(long key, E value) {
		// Makes sure the key is not already in the hash-table.
		Entry tab[] = table;
		int hash = (int) key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.key == key) {
				@SuppressWarnings("unchecked")
				E old = (E) e.value;
				e.value = value;
				return old;
			}
		}

		if (count >= threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = table;
			index = (hash & 0x7FFFFFFF) % tab.length;
		}

		// Creates the new entry.
		Entry e = new Entry(key, value, tab[index]);
		tab[index] = e;
		count++;
		return null;
	}

	/**
	 * Removes the key (and its corresponding value) from this hash-table.
	 *
	 * This method does nothing if the key is not present in the hash-table.
	 *
	 * @param key the key that needs to be removed.
	 * @return the value to which the key had been mapped in this hash-table, or
	 *         {@code null} if the key did not have a mapping.
	 */
	public E remove(long key) {
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
	 * Clears this hash-table so that it contains no keys.
	 */
	public synchronized void clear() {
		Entry tab[] = table;
		for (int index = tab.length; --index >= 0;) {
			tab[index] = null;
		}
		count = 0;
	}

	@SuppressWarnings("unchecked")
	public void save(long[] keys, E[] values) {
		if (keys == null)
			throw new NullPointerException("Invalid keys"); //$NON-NLS-1$
		if (values == null)
			throw new NullPointerException("Invalid values"); //$NON-NLS-1$

		if(keys.length!=values.length)
			throw new IllegalArgumentException("Different size for keys and values buffer"); //$NON-NLS-1$
		if(keys.length!=count)
			throw new IllegalArgumentException("Buffer size does not match current size of table"); //$NON-NLS-1$

		int index = 0;
		Entry tab[] = table;
		for (int i = tab.length; i-- > 0;) {
			for (Entry e = tab[i]; e != null; e = e.next) {
				keys[index] = e.key;
				values[index] = (E) e.value;
			}
		}
	}

	public void load(long[] keys, E[] values) {
		if (keys == null)
			throw new NullPointerException("Invalid keys"); //$NON-NLS-1$
		if (values == null)
			throw new NullPointerException("Invalid values"); //$NON-NLS-1$

		if(keys.length!=values.length)
			throw new IllegalArgumentException("Different size for keys and values buffer"); //$NON-NLS-1$

		clear();

		int size = keys.length;
		for (int i = 0; i<size; i++) {
			put(keys[i], values[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public void traverse(Visitor<? super E> visitor) {
		if (visitor == null)
			throw new NullPointerException("Invalid visitor"); //$NON-NLS-1$

		Entry tab[] = table;
		for (int i = tab.length; i-- > 0;) {
			for (Entry e = tab[i]; e != null; e = e.next) {
				visitor.visit(e.key, (E) e.value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<E> values() {
		Collection<E> result = new ArrayList<>(count);

		Entry tab[] = table;
		for (int i = tab.length; i-- > 0;) {
			for (Entry e = tab[i]; e != null; e = e.next) {
				result.add((E) e.value);
			}
		}

		return result;
	}

	public interface Visitor<V extends Object> {
		void visit(long key, V value);
	}
}
