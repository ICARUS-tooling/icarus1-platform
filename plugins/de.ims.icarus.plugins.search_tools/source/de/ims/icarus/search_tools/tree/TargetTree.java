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

import de.ims.icarus.util.Options;

/**
 * A rooted tree view on arbitrary underlying data. This interface
 * is merely a viewport on the data structure and serves as navigable
 * scope to point access on certain nodes or edges in the graph.
 * <p>
 * Subclasses are responsible for providing the methods to actually access
 * the content of nodes and edges since this interface only specifies the
 * methods for <i>traversing</i> the tree structure.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TargetTree {

	/**
	 * Returns the number of nodes (vertices) in the tree
	 */
	int size();

	/**
	 * Clears any pointer structures and unlocks all locking flags
	 */
	void reset();

	/**
	 * Releases all internal data structures
	 */
	void close();

	/**
	 * Rebuild the graph representation based on the given data input.
	 * This effectively clears all previously set pointers and locks
	 */
	void reload(Object source, Options options);

	Object getSource();

	/**
	 * Returns the index of the node currently being viewed or
	 * {@code -1} if the current scope is not pointed on a node.
	 */
	int getNodeIndex();

	/**
	 * Returns the index of the edge currently being viewed or
	 * {@code -1} if the current scope is not pointed on an edge.
	 */
	int getEdgeIndex();

	int getChildIndexAt(int nodeIndex, int index);

	/**
	 * Returns the number of outgoing edges on the currently viewed
	 * node.
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 */
	int getEdgeCount();

	/**
	 * Moves the scope to the edge at position {@code index} of the
	 * current node.
	 * <p>
	 * Precondition: {@code #getNodeIndex()}!=-1 && {@code #getEdgeIndex()}==-1
	 * Postcondition: {@code #getEdgeIndex()}==index && {@code #getNodeIndex()}==-1
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 * @throws IndexOutOfBoundsException in case the provided {@code index} is
	 * out of bounds: 0&le;index&lt;edgeCount
	 */
	void viewEdge(int index);

	/**
	 * Moves the scope to the edge at position {@code index} of the
	 * node specified by {@code nodeIndex}.
	 * <p>
	 * Postcondition: {@code #getEdgeIndex()}==edgeIndex && {@code #getNodeIndex()}==-1
	 *
	 * @throws IndexOutOfBoundsException in case the provided {@code nodeIndex} is
	 * not in the range of 0 &le; nodeIndex &lt; {@code size()} or {@code edgeIndex} is
	 * out of bounds: 0 &le; index &lt; {@code getEdgeCount()} when moved to the
	 * new node.
	 */
	void viewEdge(int nodeIndex, int edgeIndex);

	/**
	 * Returns the index of the source node of the currently viewed edge.
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * an edge
	 */
	int getSourceIndex();

	/**
	 * Returns the index of the target node of the currently viewed edge.
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * an edge
	 */
	int getTargetIndex();

	/**
	 * Returns the index of the one single root node in this graph.
	 */
	//int getRootIndex();

	/**
	 * Returns the index of the current node's parent node or {@code -1}
	 * if the current node is the root node.
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 */
	int getParentIndex();

	/**
	 * Moves the scope to the node at position {@code index} in the graph.
	 * <p>
	 * Precondition: {@code #getNodeIndex()}!=-1 && {@code #getEdgeIndex()}==-1
	 * Postcondition: {@code #getEdgeIndex()}==index && {@code #getNodeIndex()}==-1
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 * @throws IndexOutOfBoundsException in case the provided {@code index} is
	 * out of bounds: 0&le;index&lt;edgeCount
	 */
	void viewNode(int index);

	void viewChild(int index);

	/**
	 * Returns the height of the sub-tree whose root node is the node currently
	 * being viewed. For a leaf node this method must return {@code 1} and for any
	 * other node it is {@code 1} plus the maximum of any of its child nodes height.
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 */
	int getHeight();

	/**
	 * Returns the number of nodes in the sub-tree whose root the node currently
	 * being viewed is. This count does not include the node itself and is {@code 0}
	 * for leaf nodes.
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 */
	int getDescendantCount();

	// Shorthand methods

	/**
	 * Moves the scope to the node that is the parent of the current node.
	 * <p>
	 * Precondition: {@code #getNodeIndex()}!=-1 && {@code #getEdgeIndex()}==-1 && {@code i}=={@code #getParentIndex()}
	 * Postcondition: {@code #getEdgeIndex()}==-1 && {@code #getNodeIndex()}=={@code i}
	 *
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node or the current node is the root node
	 */
	void viewParent();

	void viewTarget();

	void viewSource();

	// Query methods

	boolean isRoot();

	Object getProperty(String key);

	// Locking methods

	void lockNode();

	void lockEdge();

	void lockEdge(int index);

	void lockEdge(int nodeIndex, int index);

	void lockNode(int index);

	void unlockNode();

	void unlockEdge();

	void unlockEdge(int index);

	void unlockEdge(int nodeIndex, int index);

	void unlockNode(int index);

	void unlockChildren(int index);

	boolean isNodeLocked();

	boolean isEdgeLocked();

	boolean isNodeLocked(int index);

	boolean isEdgeLocked(int index);

	boolean isEdgeLocked(int nodeIndex, int index);

	void unlockAll();
}
