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
package de.ims.icarus.search_tools.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTargetTree<E extends Object> implements TargetTree {
	
	protected int[][] edges;
	protected boolean[][] locks;
	protected int[] heights;
	protected int[] descendantCounts;
	
	protected List<Integer> roots;
	protected int rootCount = 0;
	
	protected int[] heads;
	
	protected int size;
	
	protected E data;
	
	protected int nodePointer = -1;
	
	// When edgePointer!=-1 then there is a valid edge list
	protected int edgePointer = -1;
	
	protected int bufferSize = 200;
	
	protected static final int LIST_START_SIZE = 3;

	protected AbstractTargetTree() {
		buildBuffer();
	}
	
	protected void buildBuffer() {
		edges = new int[bufferSize][];
		locks = new boolean[bufferSize][];
		heights = new int[bufferSize];
		descendantCounts = new int[bufferSize];
		heads = new int[bufferSize];
	}

	@Override
	public void close() {
		edges = null;
		locks = null;
		heights = null;
		descendantCounts = null;
		heads = null;
		roots = null;
		
		data = null;
		size = 0;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#reset()
	 */
	@Override
	public void reset() {
		nodePointer = -1;
		edgePointer = -1;
		
		unlockAll();
	}

	@Override
	public E getSource() {
		return data;
	}
	
	protected abstract int fetchSize();
	
	protected abstract int fetchHead(int index);
	
	protected void prepare(Options options) {
		// for subclasses
	}
	
	protected abstract boolean supports(Object data);

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#reload(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void reload(Object source, Options options) {
		if(source==null)
			throw new NullPointerException("Invalid source data"); //$NON-NLS-1$
		if(!supports(source))
			throw new NullPointerException("Invalid source data: "+source.getClass()); //$NON-NLS-1$
		
		data = (E)source;
		
		prepare(options);

		size = fetchSize();
		int head;
		int[] list, tmp;

		if(size<edges.length) {
			// If buffer is sufficient reset all data
			reset();
		} else {
			// Otherwise expand buffer
			bufferSize = Math.max(size, bufferSize*2);
			buildBuffer();
		}

		// reset internal stuff
		for (int i = 0; i < size; i++) {
			descendantCounts[i] = 0;
			heights[i] = 0;

			list = edges[i];
			if (list != null) {
				list[0] = 0;
			}
		}
		
		// rebuild edge lookup and locks
		for (int i = 0; i < size; i++) {
			head = fetchHead(i);
			if(head == LanguageUtils.DATA_UNDEFINED_VALUE) {
				data = null;
				throw new IllegalArgumentException("Data contains undefined head at index: "+i); //$NON-NLS-1$
			} else if (head == LanguageUtils.DATA_HEAD_ROOT) {
				if(roots==null) {
					roots = new ArrayList<>();
				}
				roots.add(i);
			} else {
				list = edges[head];
				if (list == null) {
					// TODO validate initial list size (run corpus and count
					// number of arraycopy calls per data)
					list = new int[LIST_START_SIZE];
					edges[head] = list;
					locks[head] = new boolean[LIST_START_SIZE];
				} else if (list[0] >= list.length - 1) {
					tmp = new int[list.length + list.length];
					locks[head] = new boolean[tmp.length];
					System.arraycopy(list, 0, tmp, 0, list.length);
					list = tmp;
					edges[head] = list;
					tmp = null;
				}

				list[0]++;
				list[list[0]] = i;
				// System.out.printf("%d %s: %d %s\n", i, data.forms[i], head,
				// Arrays.toString(list));
			}
			
			if(locks[i]==null) {
				locks[i] = new boolean[LIST_START_SIZE];
			}
			
			heads[i] = head;
		}
		
		if(roots==null || roots.isEmpty())
			throw new IllegalArgumentException("Structure is not a tree - no root defined"); //$NON-NLS-1$

		// refresh descendants counter and depth
		for(int root : roots) {
			prepareDescendants0(root);
		}
	}

	protected void prepareDescendants0(int index) {
		int[] list = edges[index];

		// System.out.printf("preparing %d\n", index);

		if (list != null && list[0] != 0) {
			int idx;
			int value = list[0];
			int depth = 0;
			for (int i = 1; i <= list[0]; i++) {
				idx = list[i];
				prepareDescendants0(idx);
				value += descendantCounts[idx];
				depth = (int) Math.max(depth, heights[idx]);
			}
			descendantCounts[index] = value;
			heights[index] = (int) (depth + 1);
		} else {
			descendantCounts[index] = 0;
			heights[index] = 1; // MARK 1
		}
	}
	
	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getNodeIndex()
	 */
	@Override
	public int getNodeIndex() {
		return edgePointer==-1 ? nodePointer : -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getEdgeIndex()
	 */
	@Override
	public int getEdgeIndex() {
		return edgePointer;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		int[] list = edges[nodePointer];
		
		return list==null ? 0 : list[0];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewEdge(int)
	 */
	@Override
	public void viewEdge(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		int[] list = edges[nodePointer];
		
		if(list==null || index<0 || index>=list[0])
			throw new IndexOutOfBoundsException("Edge index out of bounds: "+index); //$NON-NLS-1$
		
		edgePointer = index;
	}

	/**
	 * 
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewEdge(int, int)
	 */
	@Override
	public void viewEdge(int nodeIndex, int edgeIndex) {
		if(nodeIndex<0 || nodeIndex>=size)
			throw new IndexOutOfBoundsException("Node index out of bounds: "+nodeIndex); //$NON-NLS-1$
		
		int[] list = edges[nodeIndex];		

		if(list==null || edgeIndex<0 || edgeIndex>=list[0])
			throw new IndexOutOfBoundsException("Edge index out of bounds: "+edgeIndex); //$NON-NLS-1$
		
		nodePointer = nodeIndex;
		edgePointer = edgeIndex;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getSourceIndex()
	 */
	@Override
	public int getSourceIndex() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$
		
		return nodePointer;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getTargetIndex()
	 */
	@Override
	public int getTargetIndex() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$
		
		return edges[nodePointer][1+edgePointer];
	}

	@Override
	public boolean isRoot() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return heads[nodePointer]==LanguageUtils.DATA_HEAD_ROOT;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getParentIndex()
	 */
	@Override
	public int getParentIndex() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return heads[nodePointer];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewNode(int)
	 */
	@Override
	public void viewNode(int index) {
		if(index<0 || index>=size)
			throw new IndexOutOfBoundsException("Node index out of bounds: "+index); //$NON-NLS-1$
		
		nodePointer = index;
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewChild(int)
	 */
	@Override
	public void viewChild(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		int[] list = edges[nodePointer];
		
		if(list==null || index<0 || index>=list[0])
			throw new IndexOutOfBoundsException("Child index out of bounds: "+index); //$NON-NLS-1$
		
		nodePointer = list[1+index];
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getChildIndexAt(int, int)
	 */
	@Override
	public int getChildIndexAt(int nodeIndex, int index) {
		if(nodeIndex<0 || nodeIndex>=size)
			throw new IndexOutOfBoundsException("Node index out of bounds: "+nodeIndex); //$NON-NLS-1$
		
		int[] list = edges[nodeIndex];
		
		if(list==null || index<0 || index>=list[0])
			throw new IndexOutOfBoundsException("Child index out of bounds: "+index); //$NON-NLS-1$
		
		return list[1+index];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewParent()
	 */
	@Override
	public void viewParent() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		if(heads[nodePointer]==-1)
			throw new IllegalStateException("Current node is the root node"); //$NON-NLS-1$
		
		nodePointer = heads[nodePointer];
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewTarget()
	 */
	@Override
	public void viewTarget() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$
		
		nodePointer = edges[nodePointer][1+edgePointer];
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewSource()
	 */
	@Override
	public void viewSource() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		// nodePointer is already set to the source of the current edge!
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getHeight()
	 */
	@Override
	public int getHeight() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return heights[nodePointer];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getDescendantCount()
	 */
	@Override
	public int getDescendantCount() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return descendantCounts[nodePointer];
	}
	
	// LOCKING METHODS

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockNode()
	 */
	@Override
	public void lockNode() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		locks[nodePointer][0] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockEdge()
	 */
	@Override
	public void lockEdge() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$
		
		locks[nodePointer][1+edgePointer] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockEdge(int)
	 */
	@Override
	public void lockEdge(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		locks[nodePointer][1+index] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockEdge(int, int)
	 */
	@Override
	public void lockEdge(int nodeIndex, int index) {
		locks[nodeIndex][1+index] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockNode(int)
	 */
	@Override
	public void lockNode(int index) {
		locks[index][0] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockNode()
	 */
	@Override
	public void unlockNode() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		unlockNode(nodePointer);
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockEdge()
	 */
	@Override
	public void unlockEdge() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$
		
		locks[nodePointer][1+edgePointer] = false;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockEdge(int)
	 */
	@Override
	public void unlockEdge(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		locks[nodePointer][1+index] = false;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockEdge(int, int)
	 */
	@Override
	public void unlockEdge(int nodeIndex, int index) {
		locks[nodeIndex][1+index] = false;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockNode(int)
	 */
	@Override
	public void unlockNode(int index) {
		locks[index][0] = false;
		int[] list = edges[index];
		
		// Unlock all edges for this node!
		if(list!=null) {
			boolean[] lock = locks[index];
			for(int i=1; i<=list[0]; i++) {
				lock[i] = false;
			}
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockChildren(int)
	 */
	@Override
	public void unlockChildren(int index) {
		int[] list = edges[index];
		
		if(list!=null) {
			for(int i=1; i<=list[0]; i++) {
				unlockNode(list[i]);
			}
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isNodeLocked()
	 */
	@Override
	public boolean isNodeLocked() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return locks[nodePointer][0];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isEdgeLocked()
	 */
	@Override
	public boolean isEdgeLocked() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$
		
		return locks[nodePointer][1+edgePointer];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isNodeLocked(int)
	 */
	@Override
	public boolean isNodeLocked(int index) {
		return locks[index][0];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isEdgeLocked(int)
	 */
	@Override
	public boolean isEdgeLocked(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return locks[nodePointer][1+index];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isEdgeLocked(int, int)
	 */
	@Override
	public boolean isEdgeLocked(int nodeIndex, int index) {
		return locks[nodeIndex][1+index];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockAll()
	 */
	@Override
	public void unlockAll() {
		for(int i=0; i<size; i++) {
			int[] list = edges[i];
			if(list!=null) {
				Arrays.fill(locks[i], 0, list[0], false);
			}
		}
	}
}
