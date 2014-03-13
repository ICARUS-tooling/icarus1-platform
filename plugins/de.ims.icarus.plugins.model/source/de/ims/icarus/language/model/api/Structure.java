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
package de.ims.icarus.language.model.api;

import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.api.manifest.StructureManifest;

/**
 * Provides a structural view on a {@link MarkableLayer} by specifying a
 * set of nodes connected by edges. Typically a {@code Structure} object
 * will serve as a kind of <i>augmentation</i> of an existing {@code Container}:
 * <br>
 * It holds the required markables from the original container (either
 * directly or via a general reference to the other container) and
 * (optionally) defines a set of virtual markables. Over all those markables
 * it then spans a collection of edges, thereby creating the <i>structural</i>
 * information.
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Structure extends Container {

	@Override
	StructureManifest getManifest();

	/**
	 * Returns the <i>type</i> of this structure.
	 * @return the type of this structure
	 */
	StructureType getStructureType();

	//FIXME moved to StructureManifest
//	/**
//	 * Returns {@code true} if this structure is allowed to have multiple root nodes.
//	 *
//	 * @return {@code true} if and only if the structure represented
//	 * is allowed to contain multiple root nodes.
//	 * @throws UnsupportedOperationException if this container is not a structure
//	 */
//	boolean isMultiRoot();

	/**
	 * Returns the total number of edges this structure hosts.
	 * @return the total number of edges this structure hosts.
	 */
	int getEdgeCount();

	/**
	 * Returns the {@link Edge} stored at the given position within this
	 * structure.
	 *
	 * @param index The position of the desired {@code Edge} within this structure
	 * @return The {@code Edge} at position {@code index}
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 */
	Edge getEdgeAt(int index);


	/**
	 * Returns the index of the given {@code Edge} within this structure's
	 * list of edges or {@code -1} if the markable is not hosted within this
	 * structure.
	 * <p>
	 * Note that for every edge <i>m</i> that is hosted within some structure the
	 * following will always return a result different from {@code -1}:<br>
	 * {@code e.getStructure().indexOfEdge(e)}
	 *
	 * @param edge The {@code Edge} whose index is to be returned
	 * @return The index at which the {@code Edge} appears within this
	 * structure or {@code -1} if the edge is not hosted within this structure.
	 * @throws NullPointerException if the {@code edge} argument is {@code null}
	 */
	int indexOfEdge(Edge edge);

	/**
	 * Returns {@code true} if this structure hosts the specified edge.
	 *
	 * @param edge The edge to check
	 * @return {@code true} iff this structure hosts the given edge
	 * @throws NullPointerException if the {@code edge} argument is {@code null}
	 */
	boolean containsEdge(Edge edge);

	/**
	 * Return the number of <b>outgoing</i> edges for a given node.
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is not {@value StructureType#SET}.
	 *
	 * @param node the node to query for the number of outgoing edges.
	 * @return the number of <b>outgoing</i> edges for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member
	 * of this structure's node-container
	 */
	int getEdgeCount(Markable node);

	/**
	 * Return the <b>outgoing</i> edge at position {@code index} for a given node.
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is not {@value StructureType#SET}.
	 *
	 * @param node the {@code Markable} in question
	 * @param index the position of the desired {@code Edge} in the list of
	 * <i>outgoing</i> edges for the given node
	 * @return the <b>outgoing</i> edge at position {@code index} for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount(Markable)</tt>)
	 */
	Edge getEdgeAt(Markable node, int index);

	/**
	 * Utility method to fetch the <i>parent</i> of a given markable in this
	 * structure. The meaning of the term <i>parent</i> is depending on the
	 * {@code StructureType} as defined in this structure's {@code ContainerManifest}
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is neither {@value StructureType#SET} nor
	 * {@value StructureType#GRAPH}.
	 *
	 * @param node the node whose parent is to be returned
	 * @return the node's parent or {@code null} if the node has no parent
	 */
	Markable getParent(Markable node);

	/**
	 * For non-trivial structures returns the <i>generic root</i> node.
	 * To allow actual root nodes of the structure to contain edge
	 * annotations, they should all be linked to the single
	 * <i>generic root</i> which makes it easier for application code
	 * to collect them in a quick lookup manner.
	 * <p>
	 * What the actual root of a structure is meant to be depends on that
	 * structure's {@code StructureType}:<br>
	 * For a {@value StructureType#CHAIN} this is the first item in the chain,
	 * for a {@value StructureType#TREE} it is the one tree-root. In the case
	 * of general {@value StructureType#GRAPH} structures it will be either a
	 * single node specifically marked as root or each node that has no
	 * incoming edges.
	 *
	 * @return the <i>generic root</i> of this structure or {@code null} if this
	 * structure is of type {@value StructureType#SET}
	 */
	Markable getRoot();

	/**
	 * Returns whether or not the given {@code Markable} is a root in this structure
	 *
	 * @param node The {@code Markable} in question
	 * @return {@code true} iff the given {@code node} is a root in this structure
	 * @throws NullPointerException if the {@code node} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member of this
	 * structure
	 */
	boolean isRoot(Markable node);

	// EDIT METHODS

	/**
	 * Removes from this structure all edges.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void removeAllEdges();

	/**
	 * Adds the given {@code edge} to the internal edge storage
	 *
	 * @param edge
	 * @return
	 */
	Edge addEdge(Edge edge);

	/**
	 * Adds the given {@code edge} to the internal edge storage at
	 * the given position
	 *
	 * @param edge
	 * @param index
	 * @return
	 *
	 * @see #addEdge(Edge)
	 */
	Edge addEdge(Edge edge, int index);

	/**
	 * Creates a new edge as member of this structure
	 * and appends it to the end of the internal storage.
	 *
	 * @return The newly created member of the container
	 * @param source
	 * @param target
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 *
	 * @see #addEdge(Edge)
	 */
	Edge addEdge(Markable source, Markable target);

	/**
	 * Creates a new edge as member of this structure
	 * and inserts it at the specified position in the internal
	 * storage.
	 *
	 * Note that calling this method with an {@code index} parameter
	 * equal to the size of the mutating structure as returned by
	 * {@link Structure#getEdgeCount()} is equivalent to
	 * using {@link #addEdge(Markable, Markable)}.
	 *
	 * @param source
	 * @param target
	 * @param index The position to insert the new edge at
	 * @return The newly created edge of the structure
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt; getSubject().getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 *
	 * @see #addEdge(Edge, int)
	 */
	Edge addEdge(Markable source, Markable target, int index);

	/**
	 * Removes and returns the edge at the given index. Shifts the
	 * indices of all edges after the given position to account
	 * for the missing member.
	 *
	 * @param index The position of the edge to be removed
	 * @return The edge previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getSubject().getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	Edge removeEdge(int index);

	/**
	 * First determines the index of the given edge object within
	 * this structure and then calls {@link #removeEdge(int)}.
	 *
	 * @param edge
	 * @return
	 * @see Structure#indexOfEdge(Edge)
	 */
	Edge removeEdge(Edge edge);

	/**
	 * Moves the edge currently located at position {@code index0}
	 * over to position {@code index1}.
	 *
	 * @param index0
	 * @param index1
	 * @throws IllegalArgumentException if <tt>index0 == index1</tt>
	 * @throws IndexOutOfBoundsException if either {@code index0} or {@code index1}
	 * is out of range (<tt>index &lt; 0 || index &gt;= getSubject().getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void moveEdge(int index0, int index1);

	/**
	 * Shorthand method for moving a given edge object.
	 *
	 * First determines the index of the given edge object within
	 * this structure and then calls {@link #moveEdge(int, int)}.
	 *
	 * @param markable The markable to be moved
	 * @param index The position the {@code edge} argument should be moved to
	 * @see Structure#indexOfEdge(Edge)
	 */
	void moveEdge(Edge edge, int index);

	/**
	 * Changes the specified terminal (source or target) of the given edge to
	 * the supplied markable. Note that the {@code markable} argument has to
	 * be already contained within the "node" container of this structure.
	 *
	 * @param edge The edge whose terminal should be changed
	 * @param markable The new terminal for the edge
	 * @param isSource Specifies which terminal (source or target) should be changed
	 * @throws NullPointerException if either one the {@code edge} or {@code markable}
	 * argument is {@code null}
	 * @throws IllegalArgumentException if the given {@code markable} is unknown to
	 * this structure (i.e. not a member of its "node" container")
	 * @throws IllegalArgumentException if the given {@code markable} is not a valid
	 * candidate for the specified terminal
	 */
	void setTerminal(Edge edge, Markable markable, boolean isSource);
}
