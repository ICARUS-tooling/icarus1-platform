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
package de.ims.icarus.language.model.standard.member;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.ims.icarus.language.model.CorpusMember;
import de.ims.icarus.util.collections.LongIntHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LookupList<E extends CorpusMember> implements Iterable<E> {

	private static final int MIN_LOOKUP_SIZE = 6;

    private static final int DEFAULT_CAPACITY = 10;

    private static final Object[] EMPTY_ITEMS = {};

	private Object[] items;
	private LongIntHashMap lookup;
	private int size = 0;
	private int modCount = 0;

	public LookupList() {
		items = EMPTY_ITEMS;
	}

	public LookupList(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+capacity); //$NON-NLS-1$

        items = new Object[capacity];
	}

	public int size() {
		return size;
	}

	@SuppressWarnings("unchecked")
	public E get(int index) {
		rangeCheck(index);
		return (E) items[index];
	}

	public void add(E item) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!

		int index = size;
        items[size++] = item;
        map(item, index);
	}

	public void add(int index, E item) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);  // Increments modCount!!
        System.arraycopy(items, index, items, index + 1,
                         size - index);
        items[index] = item;
        size++;
        map(item, index);
	}

	public E set(E item, int index) {
        rangeCheck(index);

        @SuppressWarnings("unchecked")
		E oldValue = (E) items[index];
        items[index] = item;

        unmap(oldValue);
        map(item, index);

        return oldValue;
	}

	public E remove(int index) {
        rangeCheck(index);

        modCount++;
        @SuppressWarnings("unchecked")
		E oldValue = (E) items[index];

        fastRemove(index);

        return oldValue;
	}

	public boolean remove(E item) {
        if (item == null) {
            for (int index = 0; index < size; index++)
                if (items[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (item.equals(items[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
	}

	public void clear() {
        modCount++;

        // clear to let GC do its work
        for (int i = 0; i < size; i++) {
            items[i] = null;
        }

        // Directly remove the entire lookup
        lookup = null;

        size = 0;
	}

	public boolean contains(E item) {
		return indexOf(item)!=-1;
	}

	public int indexOf(E item) {
		if(lookup!=null) {
			return lookup.get(item.getId());
		}

        for (int i = 0; i < size; i++) {
            if(item.equals(items[i])) {
            	return i;
            }
        }

        return -1;
	}

	public boolean isEmpty() {
		return size==0;
	}


    private void fastRemove(int index) {
        modCount++;
        @SuppressWarnings("unchecked")
		E item = (E) items[index];
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(items, index+1, items, index,
                             numMoved);
        items[--size] = null; // clear to let GC do its work
        unmap(item);
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void ensureCapacityInternal(int minCapacity) {

        if (items == EMPTY_ITEMS) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        modCount++;

        // overflow-conscious code
        if (minCapacity - items.length > 0) {
            // overflow-conscious code
            int oldCapacity = items.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = hugeCapacity(minCapacity);
            // minCapacity is usually close to size, so this is a win:
            items = Arrays.copyOf(items, newCapacity);
        }
    }

    private boolean checkRequiresLookup() {

    	boolean requires = size>=MIN_LOOKUP_SIZE;

    	if(requires && lookup==null) {
    		// Create and fill lookup
    		lookup = new LongIntHashMap(size);

            for (int i = 0; i < size; i++) {
            	@SuppressWarnings("unchecked")
				E item = (E) items[i];
            	lookup.put(item.getId(), i);
            }
    	}

    	return requires;
    }

    private void map(E item, int index) {
    	if(checkRequiresLookup()) {
    		lookup.put(item.getId(), index);
    	}
    }

    private void unmap(E item) {
    	if(checkRequiresLookup()) {
    		lookup.remove(item.getId());
    	}
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}



	private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
            return cursor != size;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] items = LookupList.this.items;
            if (i >= items.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) items[lastRet = i];
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
            	LookupList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
		}

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
	}
}
