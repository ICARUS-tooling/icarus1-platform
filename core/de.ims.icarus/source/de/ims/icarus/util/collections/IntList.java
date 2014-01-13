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

import java.util.Arrays;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IntList extends AbstractPrimitiveList {

    private static final int DEFAULT_CAPACITY = 10;

    private static final int[] EMPTY_ITEMS = {};

	private int[] items;

	public void addElement(int value) {
		ensureCapacity(size+1);

		items[size++] = value;
	}

	public void addElement(int index, int value) {
		rangeCheckForAdd(index);

        ensureCapacity(size + 1);  // Increments modCount!!
        System.arraycopy(items, index, items, index + 1,
                         size - index);
        items[index] = value;
        size++;
	}

	public int setElement(int index, int value) {
		rangeCheck(index);

		int result = items[index];
		items[index] = value;

		return result;
	}

	public int remove(int index) {
		int value = items[index];
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(items, index+1, items, index,
                             numMoved);
        size--;

        return value;
	}

	public boolean removeElement(int value) {
		for(int index = 0; index < size; index++) {
			if(items[index]==value) {
				remove(index);
				return true;
			}
		}

		return false;
	}

	public boolean contains(int value) {
		for(int index = 0; index < size; index++) {
			if(items[index]==value) {
				return true;
			}
		}

		return false;
	}

	public void setElements(int[] values) {
		if(values==null) {
			values = EMPTY_ITEMS;
		}

		items = values;
		size = items.length;
	}

	public void clear() {
		items = EMPTY_ITEMS;
		size = 0;
	}

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void ensureCapacity(int minCapacity) {

        if (items == EMPTY_ITEMS) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

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

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

}
