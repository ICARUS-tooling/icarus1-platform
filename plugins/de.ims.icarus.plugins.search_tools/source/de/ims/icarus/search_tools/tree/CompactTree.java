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
package de.ims.icarus.search_tools.tree;

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
			throw new NullPointerException("Invalid heads array"); //$NON-NLS-1$
		
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
