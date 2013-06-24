/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency.search;

import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.search_tools.tree.TargetTree;
import net.ikarus_systems.icarus.util.CorruptedStateException;

/**
 * Rooted tree view on dependency data structures.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyTargetTree implements TargetTree {
	
	private int[][] edges;
	private boolean[][] locks;
	private int[] heights;
	private int[] descendantCounts;
	
	private int rootIndex;
	
	private int[] heads;
	
	private int size;
	
	private int nodePointer = -1;
	
	// When edgePointer!=-1 then there is a valid edge list
	private int edgePointer = -1;
	
	private DependencyData data;
	
	private int bufferSize = 200;
	
	private static final int LIST_START_SIZE = 3;

	public DependencyTargetTree() {
		buildBuffer();
	}
	
	private void buildBuffer() {
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
		
		data = null;
		size = 0;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#reset()
	 */
	@Override
	public void reset() {
		nodePointer = -1;
		edgePointer = -1;
		
		unlockAll();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#reload(java.lang.Object)
	 */
	@Override
	public void reload(Object source) {
		if(source==null)
			throw new IllegalArgumentException("Invalid source data"); //$NON-NLS-1$
		if(!(source instanceof DependencyData))
			throw new IllegalArgumentException("Invalid source data: "+source.getClass()); //$NON-NLS-1$
		
		data = (DependencyData)source;

		size = (int) data.length();
		int head;
		int[] list, tmp;
		
		reset();
		
		if(size>=bufferSize) {
			bufferSize *= 2;
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
			head = data.getHead(i);
			if(head == LanguageUtils.DATA_UNDEFINED_VALUE) {
				data = null;
				throw new IllegalArgumentException("Data contains undefined head at index: "+i); //$NON-NLS-1$
			} else if (head == LanguageUtils.DATA_HEAD_ROOT) {
				rootIndex = i;
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

		// refresh descendants counter and depth
		prepareDescendants0(rootIndex);
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getNodeIndex()
	 */
	@Override
	public int getNodeIndex() {
		return edgePointer==-1 ? nodePointer : -1;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getEdgeIndex()
	 */
	@Override
	public int getEdgeIndex() {
		return edgePointer;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		int[] list = edges[nodePointer];
		
		return list==null ? 0 : list[0];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#viewEdge(int)
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#viewEdge(int, int)
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getSourceIndex()
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getTargetIndex()
	 */
	@Override
	public int getTargetIndex() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$
		
		return edges[nodePointer][1+edgePointer];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getRootIndex()
	 */
	@Override
	public int getRootIndex() {
		return rootIndex;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getParentIndex()
	 */
	@Override
	public int getParentIndex() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return heads[nodePointer];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#viewNode(int)
	 */
	@Override
	public void viewNode(int index) {
		if(index<0 || index>=size)
			throw new IndexOutOfBoundsException("Node index out of bounds: "+index); //$NON-NLS-1$
		
		nodePointer = index;
		edgePointer = -1;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#viewParent()
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#viewTarget()
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#viewSource()
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getHeight()
	 */
	@Override
	public int getHeight() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return heights[nodePointer];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#getDescendantCount()
	 */
	@Override
	public int getDescendantCount() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return descendantCounts[nodePointer];
	}
	
	// NODE METHODS

	public String getForm() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return data.getForm(nodePointer);
	}

	public String getPos() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return data.getPos(nodePointer);
	}

	public String getLemma() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return data.getLemma(nodePointer);
	}

	/**
	 * Returns an always non-null array of feature expressions
	 */
	public String getFeatures() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return data.getFeatures(nodePointer);
	}
	
	// EDGE METHODS
	
	public String getRelation() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		return data.getRelation(nodePointer);
	}
	
	public int getDistance() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		return (int) Math.abs(nodePointer - edges[nodePointer][1+edgePointer]);
	}
	
	public int getDirection() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		return nodePointer>edges[nodePointer][1+edgePointer] ? 
				LanguageUtils.DATA_LEFT_VALUE : LanguageUtils.DATA_RIGHT_VALUE;
	}
	
	// GENERAL METHODS
	
	public boolean isFlagSet(long flag) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.isFlagSet(nodePointer, flag);
	}
	
	// LOCKING METHODS

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#lockNode()
	 */
	@Override
	public void lockNode() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		locks[nodePointer][0] = true;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#lockEdge()
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#lockEdge(int)
	 */
	@Override
	public void lockEdge(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		locks[nodePointer][1+index] = true;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#lockEdge(int, int)
	 */
	@Override
	public void lockEdge(int nodeIndex, int index) {
		locks[nodeIndex][1+index] = true;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#lockNode(int)
	 */
	@Override
	public void lockNode(int index) {
		locks[index][0] = true;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#unlockNode()
	 */
	@Override
	public void unlockNode() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		unlockNode(nodePointer);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#unlockEdge()
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#unlockEdge(int)
	 */
	@Override
	public void unlockEdge(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		locks[nodePointer][1+index] = false;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#unlockEdge(int, int)
	 */
	@Override
	public void unlockEdge(int nodeIndex, int index) {
		locks[nodeIndex][1+index] = false;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#unlockNode(int)
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#unlockChildren(int)
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#isNodeLocked()
	 */
	@Override
	public boolean isNodeLocked() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return locks[nodePointer][0];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#isEdgeLocked()
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#isNodeLocked(int)
	 */
	@Override
	public boolean isNodeLocked(int index) {
		return locks[index][0];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#isEdgeLocked(int)
	 */
	@Override
	public boolean isEdgeLocked(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return locks[nodePointer][1+index];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#isEdgeLocked(int, int)
	 */
	@Override
	public boolean isEdgeLocked(int nodeIndex, int index) {
		return locks[nodeIndex][1+index];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.TargetTree#unlockAll()
	 */
	@Override
	public void unlockAll() {
		for(int i=0; i<size; i++) {
			locks[i][0] = false;
			unlockChildren(i);
		}
	}
}
