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

 * $Revision: 244 $
 * $Date: 2014-04-10 14:09:12 +0200 (Do, 10 Apr 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/de/ims/icarus/util/collections/IntValueHashMap.java $
 *
 * $LastChangedDate: 2014-04-10 14:09:12 +0200 (Do, 10 Apr 2014) $
 * $LastChangedRevision: 244 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.model.standard.elements;

import java.util.Arrays;

import de.ims.icarus.model.api.Edge;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.Structure;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Primitive;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;


/**
 * @author Markus
 * @version $Id: Graph.java 244 2014-04-10 12:09:12Z mcgaerty $
 *
 */
@HeapMember
public class Graph {

	/**
	 * The hash table data.
	 */
	@Reference(ReferenceType.DOWNLINK)
	private transient Entry table[];

	/**
	 * The total number of entries in the hash table.
	 */
	@Primitive
	private transient int count;

	/**
	 * The table is rehashed when its size exceeds this threshold. (The value of
	 * this field is (int)(capacity * loadFactor).)
	 *
	 * @serial
	 */
	@Primitive
	private int threshold;

	/**
	 * Inner class that acts as a data structure to create a new entry in the
	 * table.
	 */
	@HeapMember
	private static class Entry {
		// Node for which edges need to be stored
		@Reference
		Markable node;
		// Array index of the first incoming edge
		@Primitive
		int countIn = 0;
		// Array index of the first outgoing edge
		@Primitive
		int countOut = 0;
		// Edges, incoming first, then outgoing
		@Reference
		Edge[] edges;
		@Link
		Entry next;

		private Entry(Markable node, Entry next) {
			this.node = node;
			this.next = next;
		}
	}

	public Graph() {
		this(CollectionUtils.DEFAULT_COLLECTION_CAPACITY);
	}


	public Graph(int initialCapacity) {

		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal capacity (negative): " //$NON-NLS-1$
					+ initialCapacity);

		if (initialCapacity == 0) {
			initialCapacity = 1;
		}

		table = new Entry[initialCapacity];
		threshold = (int) (initialCapacity * CollectionUtils.DEFAULT_LOAD_FACTOR);
	}

	private Entry get(Markable node, boolean createIfMissing) {
		if (node == null)
			throw new NullPointerException("Invalid node");  //$NON-NLS-1$

		Entry tab[] = table;
		int hash = node.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		// Search for already present entry
		for (Entry e = tab[index]; e != null; e = e.next) {
			if (e.node==node) {
				return e;
			}
		}

		// If entry is missing create if allowed
		if(createIfMissing) {

			if (count >= threshold) {
				// Rehash the table if the threshold is exceeded
				rehash();

				tab = table;
				index = (hash & 0x7FFFFFFF) % tab.length;
			}

			// Creates the new entry.
			Entry e = new Entry(node, tab[index]);
			tab[index] = e;
			count++;

			return e;
		}

		return null;
	}

	public int nodeCount() {
		return count;
	}

	public boolean isEmpty() {
		return count==0;
	}

	public int edgeCount(Markable node) {
		Entry entry = get(node, false);
		if(entry==null) {
			return 0;
		}

		return entry.countIn + entry.countOut;
	}

	public int edgeCount(Markable node, boolean incoming) {
		Entry entry = get(node, false);
		if(entry==null) {
			return 0;
		}

		return incoming ? entry.countIn : entry.countOut;
	}

	public Edge edgeAt(Markable node, int index) {
		Entry entry = get(node, false);
		if(entry==null || entry.edges==null)
			throw new IndexOutOfBoundsException("No edges for node "+node); //$NON-NLS-1$

		return entry.edges[index];
	}

	public Edge edgeAt(Markable node, boolean incoming, int index) {
		Entry entry = get(node, false);
		if(entry==null || entry.edges==null)
			throw new IndexOutOfBoundsException("No edges for node "+node); //$NON-NLS-1$

		if(!incoming) {
			index += entry.countIn;
		}

		return entry.edges[index];
	}

	/**
	 * Increases the capacity of and internally reorganizes this hash-table, in
	 * order to accommodate and access its entries more efficiently.
	 *
	 * This method is called automatically when the number of keys in the
	 * hash-table exceeds this hash-table's capacity and load factor.
	 */
	private void rehash() {
		rehash(-1);
	}

	private void rehash(int newCapacity) {
		int oldCapacity = table.length;
		Entry oldMap[] = table;

		if(newCapacity<0) {
			newCapacity = (oldCapacity * 2) + 1;
		}
		Entry newMap[] = new Entry[newCapacity];

		threshold = (int) (newCapacity * CollectionUtils.DEFAULT_LOAD_FACTOR);
		table = newMap;

		for (int i = oldCapacity; i-- > 0;) {
			for (Entry old = oldMap[i]; old != null;) {
				Entry e = old;
				old = old.next;

				int index = (e.node.hashCode() & 0x7FFFFFFF) % newCapacity;
				e.next = newMap[index];
				newMap[index] = e;
			}
		}
	}

	/**
	 * Adds the given edge to the list of edges registered for the
	 * specified node. Note that this is a particular expensive
	 * operation in terms of memory when called in rapid succession
	 * for the same node, since the internal storage will always made
	 * just big enough to store the additional edge, if required.
	 * <b>
	 * When refreshing a graph from scratch, it is preferred to rather
	 * use the {@link #rebuild(Structure)} method, which will take care
	 * of minimizing the memory waste during the process.
	 *
	 * @param node
	 * @param edge
	 * @param incoming
	 */
	public void add(Markable node, Edge edge, boolean incoming) {
		if (node == null)
			throw new NullPointerException("Invalid node"); //$NON-NLS-1$
		if (edge == null)
			throw new NullPointerException("Invalid edge"); //$NON-NLS-1$

		Entry entry = get(node, true);

		int index = incoming ? ++entry.countIn : ++entry.countOut;
		if(!incoming) {
			index += entry.countIn;
		}
		int size = entry.countIn+entry.countOut;

		if(entry.edges==null) {
			entry.edges = new Edge[size];
		} else if(size>=entry.edges.length) {
			Edge[] tmp = Arrays.copyOf(entry.edges, size);
			if(index<tmp.length-1) {
				System.arraycopy(tmp, index, tmp, index+1, size-index);
			}
			entry.edges = tmp;
		}

		entry.edges[index] = edge;
	}

	public void remove(Markable node, Edge edge, boolean incoming) {
		if (node == null)
			throw new NullPointerException("Invalid node"); //$NON-NLS-1$
		if (edge == null)
			throw new NullPointerException("Invalid edge"); //$NON-NLS-1$

		Entry entry = get(node, false);
		if(entry==null || entry.edges==null)
			throw new CorruptedStateException("Cannot remove edge for node "+node+" - no edges registered!"); //$NON-NLS-1$ //$NON-NLS-2$

		int index = incoming ? --entry.countIn : --entry.countOut;
		if(index<0)
			throw new CorruptedStateException();

		if(!incoming) {
			index += entry.countIn;
		}
		int size = entry.countIn+entry.countOut;

		if(size<=0) {
			delete(node);
		} else {
			Edge[] edges = entry.edges;
			edges[index] = null;
			if(index<size) {
				System.arraycopy(edges, index+1, edges, index, size-index);
			}
		}
	}

	public void rebuild(Structure structure) {
		if (structure == null)
			throw new NullPointerException("Invalid structure"); //$NON-NLS-1$

		// Erase entire storage
		clear();

		int edgeCount = structure.getEdgeCount();

		// First pass, calculate edge counts
		for(int i=0; i<edgeCount; i++) {
			Edge edge = structure.getEdgeAt(i);
			// Increment counters for both source and target
			get(edge.getSource(), true).countOut++;
			get(edge.getTarget(), true).countIn++;
		}

		// Second pass, ensure sufficient storage space
		Entry tab[] = table;
		for (int index = tab.length; --index >= 0;) {
			for (int i = tab.length; i-- > 0;) {
				for (Entry e = tab[i]; e != null; e = e.next) {
					// Expand storage only if insufficient
					//TODO consider if contunally expanding storage is a good idea?
					int size = e.countIn+e.countOut;
					if(e.edges==null || e.edges.length<size) {
						e.edges = new Edge[size];
					}
				}
			}
		}

		// Third pass, copy over the edge references
		for(int i=0; i<edgeCount; i++) {
			Edge edge = structure.getEdgeAt(i);

			add(edge.getSource(), edge, false);
			add(edge.getTarget(), edge, true);
		}
	}

	private void delete(Markable node) {
		Entry tab[] = table;
		int hash = node.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
			if (e.node==node) {
				if (prev != null) {
					prev.next = e.next;
				} else {
					tab[index] = e.next;
				}
				count--;
				break;
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

	public synchronized void trim() {
		int capacity = table.length;
		float load = (float)count/capacity;

		if(capacity>CollectionUtils.DEFAULT_COLLECTION_CAPACITY
				&& load<CollectionUtils.DEFAULT_MIN_LOAD) {
			rehash(count);
		}
	}
}
