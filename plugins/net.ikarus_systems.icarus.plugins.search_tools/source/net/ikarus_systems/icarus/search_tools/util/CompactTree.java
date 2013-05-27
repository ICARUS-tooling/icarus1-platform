/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.util;

import java.util.Arrays;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CompactTree {
	
	private static final int INITIAL_EDGE_BUFFER_SIZE = 3;

	int[][] edges;
	int[] heads;
	
	int root;
	int size;
	
	protected int rootValue = -1;

	public CompactTree() {
		// no-op
	}
	
	public CompactTree(int bufferSize) {
		edges = new int[bufferSize][];
		heads = new int[bufferSize];
	}

	public void init(int[] heads) {
		if(heads==null)
			throw new IllegalArgumentException("Invalid heads array"); //$NON-NLS-1$
		
		if(this.heads==null || this.heads.length<heads.length) {
			this.heads = new int[heads.length+heads.length];
		}
		
		if(edges==null || edges.length<heads.length) {
			edges = new int[heads.length+heads.length][];
		}
		
		size = heads.length;
		
		for (int i = 0; i < size; i++) {			
			this.heads[i] = heads[i];
			int[] list = edges[i];
			if(list!=null) {
				list[0] = 0;
			}
		}
		
		for (int i = 0; i < size; i++) {
			int head = heads[i];
			if(head == rootValue) {
				root = i;
			} else if (head<0 || head>=size) {
				throw new IllegalArgumentException("Head value at index "+i+" out of bounds: "+head); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				int[] list = edges[head];
				if (list == null) {
					list = new int[INITIAL_EDGE_BUFFER_SIZE];
					edges[head] = list;
				} else if (list[0] >= list.length - 1) {
					edges[head] = Arrays.copyOf(list, list.length+list.length);
				}

				list[0]++;
				list[list[0]] = i;
			}
		}
	}
	
	public int getSize() {
		return size;
	}
	
	public int getHead(int index) {
		return heads[index];
	}
	
	public int getChildCount(int index) {
		int[] list = edges[index];
		return list==null ? 0 : list[0];
	}
	
	public int getChildAt(int index, int childIndex) {
		return edges[index][childIndex];
	}

	public int getRootValue() {
		return rootValue;
	}

	public void setRootValue(int rootValue) {
		this.rootValue = rootValue;
	}
}
