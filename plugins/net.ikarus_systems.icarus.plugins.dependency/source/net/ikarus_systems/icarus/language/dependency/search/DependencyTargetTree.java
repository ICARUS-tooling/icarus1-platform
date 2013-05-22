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
import net.ikarus_systems.icarus.search_tools.TargetTree;
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

	public DependencyTargetTree() {
		buildBuffer();
	}
	
	private void buildBuffer() {
		edges = new int[bufferSize][];
		heights = new int[bufferSize];
		descendantCounts = new int[bufferSize];
		heads = new int[bufferSize];
	}

	@Override
	public void close() {
		edges = null;
		heights = null;
		descendantCounts = null;
		heads = null;
		
		data = null;
		size = 0;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#reset()
	 */
	@Override
	public void reset() {
		nodePointer = -1;
		edgePointer = -1;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#reload(java.lang.Object)
	 */
	@Override
	public void reload(Object source) {
		if(source==null)
			throw new IllegalArgumentException("Invalid source data"); //$NON-NLS-1$
		if(!(source instanceof DependencyData))
			throw new IllegalArgumentException("Invalid source data: "+source.getClass()); //$NON-NLS-1$
		
		data = (DependencyData)source;

		size = data.length();
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
		
		// rebuild edge lookup
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
					list = new int[3];
					edges[head] = list;
				} else if (list[0] >= list.length - 1) {
					tmp = new int[list.length + list.length];
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
				depth = Math.max(depth, heights[idx]);
			}
			descendantCounts[index] = value;
			heights[index] = depth + 1;
		} else {
			descendantCounts[index] = 0;
			heights[index] = 1; // MARK 1
		}
	}
	
	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getNodeIndex()
	 */
	@Override
	public int getNodeIndex() {
		return edgePointer==-1 ? nodePointer : -1;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getEdgeIndex()
	 */
	@Override
	public int getEdgeIndex() {
		return edgePointer;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		int[] list = edges[nodePointer];
		
		return list==null ? 0 : list[0];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#viewEdge(int)
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
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#viewEdge(int, int)
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
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getSourceIndex()
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
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getTargetIndex()
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
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getRootIndex()
	 */
	@Override
	public int getRootIndex() {
		return rootIndex;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getParentIndex()
	 */
	@Override
	public int getParentIndex() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return heads[nodePointer];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#viewNode(int)
	 */
	@Override
	public void viewNode(int index) {
		if(index<0 || index>=size)
			throw new IndexOutOfBoundsException("Node index out of bounds: "+index); //$NON-NLS-1$
		
		nodePointer = index;
		edgePointer = -1;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#viewParent()
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
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#viewTarget()
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
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#viewSource()
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
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getHeight()
	 */
	@Override
	public int getHeight() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		
		return heights[nodePointer];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.TargetTree#getDescendantCount()
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

		return Math.abs(nodePointer - edges[nodePointer][1+edgePointer]);
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
}
