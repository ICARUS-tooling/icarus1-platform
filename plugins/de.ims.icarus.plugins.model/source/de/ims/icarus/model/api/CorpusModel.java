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
package de.ims.icarus.model.api;

import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.api.members.Annotation;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.ContainerType;
import de.ims.icarus.model.api.members.CorpusMember;
import de.ims.icarus.model.api.members.Edge;
import de.ims.icarus.model.api.members.Fragment;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.MemberSet;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.api.members.Structure;
import de.ims.icarus.model.api.members.StructureType;
import de.ims.icarus.model.api.raster.Metric;
import de.ims.icarus.model.api.raster.Position;
import de.ims.icarus.model.api.raster.PositionOutOfBoundsException;
import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;
import de.ims.icarus.model.standard.elements.MemberSets;
import de.ims.icarus.util.Collector;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface CorpusModel {

	public static final MemberSet<Container> EMPTY_BASE_SET = MemberSets.emptySet();

	//---------------------------------------------
	//			GENERAL METHODS
	//---------------------------------------------

	/**
	 *
	 * @return {@code true} iff this model was created from a sub corpus that has an
	 * access mode of {@value CorpusAccessMode#WRITE} or {@value CorpusAccessMode#READ_WRITE}.
	 */
	boolean isEditable();

	/**
	 * Indicates whether the sub corpus backing this model is complete, i.e. it is holding
	 * the entirety of data that represents its original corpus.
	 *
	 * @return {@code true} iff this model contains the entire corpus.
	 */
	boolean isComplete();

	Corpus getCorpus();

	CorpusView getSource();

	//---------------------------------------------
	//			MEMBER METHODS
	//---------------------------------------------

	MemberType getMemberType(CorpusMember member);

	boolean isMarkable(CorpusMember member);

	boolean isContainer(CorpusMember member);

	boolean isStructure(CorpusMember member);

	boolean isEdge(CorpusMember member);

	boolean isFragment(CorpusMember member);

	boolean isLayer(CorpusMember member);

	//---------------------------------------------
	//			LAYER METHODS
	//---------------------------------------------

	int getSize(MarkableLayer layer);

	int getMarkableCount(MarkableLayer layer);

	Item getItemAt(MarkableLayer layer, int index);

	Metric getMetric(MarkableLayer layer);

	//---------------------------------------------
	//			MARKABLE METHODS
	//---------------------------------------------

	/**
	 * If this markable is hosted within a container, returns that enclosing
	 * container. Otherwise it represents a top-level markable and returns
	 * {@code null}.
	 * <p>
	 * Note that this method returns the container that <b>owns</b> this markable
	 * and not necessarily the one through which it was obtained! It is perfectly
	 * legal for a container to reuse the elements of another container and to
	 * augment the collection with its own intermediate markables. For this
	 * reason it is advised to keep track of the container the markable was
	 * fetched from when this method is called.
	 *
	 * @return The enclosing container of this markable or {@code null} if this
	 * markable is not hosted within a container.
	 */
	@AccessRestriction(AccessMode.ALL)
	Container getContainer(Item item);

	/**
	 * Returns the {@code MarkableLayer} this markable is hosted in. For nested
	 * markables this call should simply forward to the {@code Container} obtained
	 * via {@link #getContainer()} since storing a reference to the layer in each
	 * markable in addition to the respective container is expensive. Top-level
	 * markables should always store a direct reference to the enclosing layer.
	 *
	 * @return The enclosing {@code MarkableLayer} that hosts this markable object.
	 */
	@AccessRestriction(AccessMode.ALL)
	MarkableLayer getLayer(Item item);

	/**
	 * Returns the markable's global position in the hosting container. For base markables
	 * this value will be equal to the begin and end offsets, but for aggregating objects
	 * like containers or structures the returned value will actually differ from their
	 * bounding offsets.
	 * <p>
	 * Do <b>not</b> mix up the returned index with the result of a call to
	 * {@link Container#indexOfItem(Item)}! The latter is limited to integer values
	 * and returns the <i>current</i> position of a markable within that container's internal storage.
	 * This index can change over time and is most likely different when using containers from
	 * multiple {@link CorpusView}s.
	 * The result of the {@code #getIndex()} method on the other features a much larger value space
	 * and is constant, no matter where the markable in question is stored. The only way to modify
	 * a markable's index is to remove or insert other markables into the underlying data.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.ALL)
	long getIndex(Item item);

	/**
	 * Changes the index value associated with this markable object to {@code newIndex}.
	 * Note that inserting or removing markables from containers or structures might result
	 * in huge numbers of index changes!
	 *
	 * @param newIndex
	 */
	@AccessRestriction(AccessMode.WRITE)
	void setIndex(Item item, long newIndex);

	/**
	 * Returns the zero-based offset of this markable's begin within the corpus.
	 * The first {@code Item} in the {@link MarkableLayer} obtained via
	 * {@link Corpus#getBaseLayer()} is defined to have offset {@code 0}. All other
	 * offsets are calculated relative to this. If this {@code Item} is a
	 * {@link Container} or {@link Structure} then the returned offset is the
	 * result of calling {@link Item#getBeginOffset()} on the left-most markable
	 * hosted within this object.
	 * <p>
	 * Note that is perfectly legal for <i>virtual</i> markables to return
	 * {@code -1} indicating that they are not really placed within the corpus.
	 *
	 * @return The zero-based offset of this markable's begin within the corpus
	 * or {@code -1} if the markable is <i>virtual</i>
	 */
	@AccessRestriction(AccessMode.ALL)
	long getBeginOffset(Item item);

	/**
	 * Returns the zero-based offset of this markable's end within the corpus.
	 * The first {@code Item} in the {@link MarkableLayer} obtained via
	 * {@link Corpus#getBaseLayer()} is defined to have offset {@code 0}. All other
	 * offsets are calculated relative to this. If this {@code Item} is a
	 * {@link Container} or {@link Structure} then the returned offset is the
	 * result of calling {@link Item#getEndOffset()} on the right-most markable
	 * hosted within this object.
	 * <p>
	 * Note that is perfectly legal for <i>virtual</i> markables to return
	 * {@code -1} indicating that they are not really placed within the corpus.
	 *
	 * @return The zero-based offset of this markable's end within the corpus
	 * or {@code -1} if the markable is <i>virtual</i>
	 */
	@AccessRestriction(AccessMode.ALL)
	long getEndOffset(Item item);

	boolean isVirtual(Item item);

	//---------------------------------------------
	//			CONTAINER METHODS
	//---------------------------------------------

	/**
	 * Returns the type of this container. This provides
	 * information about how contained {@code Item}s are ordered and
	 * if they represent a continuous subset of the corpus.
	 *
	 * @return The {@code ContainerType} of this {@code Container}
	 * @see ContainerType
	 */
	ContainerType getContainerType(Container container);

	/**
	 * @return The underlying containers if this container relies on the
	 * elements of other container objects. If the container is independent of any
	 * other containers returns the shared empty {@code MemberSet} available
	 * via {@link #EMPTY_BASE_SET}.
	 */
	MemberSet<Container> getBaseContainers(Container container);

	/**
	 * Returns the {@code Container} that serves as bounding
	 * box for the markables in the given container. In most cases
	 * this will be a member of another {@code MarkableLayer}
	 * that represents the sentence or document level. If the
	 * {@code Container} object only builds a virtual collection
	 * atop of other markables and is not limited by previously
	 * defined <i>boundary containers</i> then this method
	 * returns {@code null}.
	 *
	 * @return
	 */
	Container getBoundaryContainer(Container container);

	/**
	 * Returns the number of {@code Item} objects hosted within the given
	 * container.
	 * <p>
	 * Note that this does <b>not</b> include possible {@code Edge}s stored
	 * within the container in case it is a {@link Structure}!
	 *
	 * @return The number of {@code Item}s in this container
	 *
	 * @see #getSize(MarkableLayer)
	 */
	int getMarkableCount(Container container);

	/**
	 * Returns the {@code Item} stored at position {@code index} within
	 * this {@code Container}. Note that however elements in a container may
	 * be unordered depending on the {@code ContainerType} as returned by
	 * {@link #getErrorType()}, the same index has always to be mapped to
	 * the exact same {@code Item} within a single container!
	 *
	 * @param index The index of the {@code Item} to be returned
	 * @return The {@code Item} at position {@code index} within this container
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getMarkableCount()</tt>)
	 */
	Item getItemAt(Container container, int index);

	/**
	 * Returns the index of the given {@code Item} within this container's
	 * list of markables or {@code -1} if the markable is not hosted within this
	 * container.
	 * <p>
	 * Note that for every markable <i>m</i> that is hosted within some container the
	 * following will always return a result different from {@code -1}:<br>
	 * {@code m.getContainer().indexOfMarkable(m)}
	 * <p>
	 * Implementations are advised to ensure that lookup operations such as this one
	 * scale well with the number of markables contained. Constant execution cost
	 * should be the standard goal!
	 *
	 * @param item The {@code Item} whose index is to be returned
	 * @return The index at which the {@code Item} appears within this
	 * container or {@code -1} if the markable is not hosted within this container.
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 */
	int indexOfItem(Container container, Item item);

	/**
	 * Returns {@code true} if this container hosts the specified markable.
	 * Essentially equal to receiving {@code -1} as result to a {@link #indexOfMarkable(Item)}
	 * call.
	 *
	 * @param item The markable to check
	 * @return {@code true} iff this container hosts the given markable
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 *
	 * @see #indexOfMarkable(Item)
	 */
	boolean containsItem(Container container, Item item);

	/**
	 * Removes from the mutating container all elements.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void removeAllMarkables(Container container);

	/**
	 * Adds a new markable to this container
	 *
	 * @param item
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void addItem(Container container, Item item);

	/**
	 * Adds a new markable to this container
	 *
	 * Note that calling this method with an {@code index} parameter
	 * equal to the size of the mutating container as returned by
	 * {@link Container#getMarkableCount()} is equivalent to
	 * using {@link #addMarkable()}.
	 *
	 * @param index The position to insert the new markable at
	 * @param item
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt; getMarkableCount()</tt>)
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void addItem(Container container, int index, Item item);

	/**
	 * Removes and returns the markable at the given index. Shifts the
	 * indices of all markables after the given position to account
	 * for the missing member.
	 *
	 * @param index The position of the markable to be removed
	 * @return The markable previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getMarkableCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	Item removeItem(Container container, int index);

	/**
	 * First determines the index of the given markable object within
	 * this container and then calls {@link #removeMarkable(int)}.
	 *
	 * @param item
	 * @return
	 * @see Container#indexOfItem(Item)
	 */
	boolean removeItem(Container container, Item item);

	/**
	 * Moves the markable currently located at position {@code index0}
	 * over to position {@code index1}. The markable previously located
	 * at position {@code index1} will then be moved to {@code index0}.
	 *
	 * @param index0
	 * @param index1
	 * @throws IllegalArgumentException if <tt>index0 == index1</tt>
	 * @throws IndexOutOfBoundsException if either {@code index0} or {@code index1}
	 * is out of range (<tt>index &lt; 0 || index &gt;= getMarkableCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void moveMarkable(Container container, int index0, int index1);

	/**
	 * Shorthand method for moving a given markable object.
	 *
	 * First determines the index of the given markable object within
	 * this container and then calls {@link #moveMarkable(int, int)}.
	 *
	 * @param item The markable to be moved
	 * @param index The position the {@code markable} argument should be moved to
	 * @see Container#indexOfItem(Item)
	 */
	void moveItem(Container container, Item item, int index);

	//---------------------------------------------
	//			STRUCTURE METHODS
	//---------------------------------------------

	/**
	 * Returns the <i>type</i> of this structure.
	 * @return the type of this structure
	 */
	StructureType getStructureType(Structure structure);

	boolean isMultiRoot(Structure structure);

	/**
	 * Returns the total number of edges this structure hosts.
	 * @return the total number of edges this structure hosts.
	 */
	int getEdgeCount(Structure structure);

	/**
	 * Returns the {@link Edge} stored at the given position within this
	 * structure.
	 *
	 * @param index The position of the desired {@code Edge} within this structure
	 * @return The {@code Edge} at position {@code index}
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 */
	Edge getEdgeAt(Structure structure, int index);


	/**
	 * Returns the index of the given {@code Edge} within this structure's
	 * list of edges or {@code -1} if the markable is not hosted within this
	 * structure.
	 * <p>
	 * Note that for every edge <i>e</i> that is hosted within some structure the
	 * following will always return a result different from {@code -1}:<br>
	 * {@code e.getStructure().indexOfEdge(e)}
	 *
	 * @param edge The {@code Edge} whose index is to be returned
	 * @return The index at which the {@code Edge} appears within this
	 * structure or {@code -1} if the edge is not hosted within this structure.
	 * @throws NullPointerException if the {@code edge} argument is {@code null}
	 */
	int indexOfEdge(Structure structure, Edge edge);

	/**
	 * Returns {@code true} if this structure hosts the specified edge.
	 *
	 * @param edge The edge to check
	 * @return {@code true} iff this structure hosts the given edge
	 * @throws NullPointerException if the {@code edge} argument is {@code null}
	 */
	boolean containsEdge(Structure structure, Edge edge);

	/**
	 * Return the total number of edges for a given node.
	 *
	 * @param node the node to query for the number of outgoing edges.
	 * @return the total number of edges for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member
	 * of this structure's node-container
	 */
	int getEdgeCount(Structure structure, Item node);

	/**
	 * Return the number of either outgoing or incoming edges for a given node
	 * depending on the {@code isSource} argument.
	 *
	 * @param node the node to query for the number of outgoing edges.
	 * @return the number of <b>outgoing</i> edges for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member
	 * of this structure's node-container
	 */
	int getEdgeCount(Structure structure, Item node, boolean isSource);

	/**
	 * Return the either outgoing or incoming edge at position {@code index}
	 * for a given node depending on the {@code isSource} argument.
	 *
	 * @param node the {@code Item} in question
	 * @param index the position of the desired {@code Edge} in the list of
	 * <i>outgoing</i> edges for the given node
	 * @return the edge at position {@code index} for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount(Item,boolean)</tt>)
	 *         with the given {@code node} and {@code isSource} parameters
	 */
	Edge getEdgeAt(Structure structure, Item node, int index, boolean isSource);

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
	Item getParent(Structure structure, Item node);

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
	Item getRoot(Structure structure);

	/**
	 * Returns whether or not the given {@code Item} is a root in this structure.
	 * The {@code root} property is determined by a node being directly linked to the
	 * <i>generic root</i> node as returned by {@link #getRoot()}.
	 *
	 * @param node The {@code Item} in question
	 * @return {@code true} iff the given {@code node} is a root in this structure
	 * @throws NullPointerException if the {@code node} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member of this
	 * structure
	 */
	boolean isRoot(Structure structure, Item node);

	// EDIT METHODS

	/**
	 * Removes from this structure all edges.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void removeAllEdges(Structure structure);

	/**
	 * Adds the given {@code edge} to the internal edge storage
	 *
	 * @param edge
	 * @return
	 */
	Edge addEdge(Structure structure, Edge edge);

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
	Edge addEdge(Structure structure, Edge edge, int index);

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
	Edge addEdge(Structure structure, Item source, Item target);

	/**
	 * Creates a new edge as member of this structure
	 * and inserts it at the specified position in the internal
	 * storage.
	 *
	 * Note that calling this method with an {@code index} parameter
	 * equal to the size of the mutating structure as returned by
	 * {@link Structure#getEdgeCount()} is equivalent to
	 * using {@link #addEdge(Item, Item)}.
	 *
	 * @param source
	 * @param target
	 * @param index The position to insert the new edge at
	 * @return The newly created edge of the structure
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt; getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 *
	 * @see #addEdge(Edge, int)
	 */
	Edge addEdge(Structure structure, Item source, Item target, int index);

	/**
	 * Removes and returns the edge at the given index. Shifts the
	 * indices of all edges after the given position to account
	 * for the missing member.
	 *
	 * @param index The position of the edge to be removed
	 * @return The edge previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	Edge removeEdge(Structure structure, int index);

	/**
	 * First determines the index of the given edge object within
	 * this structure and then calls {@link #removeEdge(int)}.
	 *
	 * @param edge
	 * @return
	 * @see Structure#indexOfEdge(Edge)
	 */
	boolean removeEdge(Structure structure, Edge edge);

	/**
	 * Moves the edge currently located at position {@code index0}
	 * over to position {@code index1}.
	 *
	 * @param index0
	 * @param index1
	 * @throws IllegalArgumentException if <tt>index0 == index1</tt>
	 * @throws IndexOutOfBoundsException if either {@code index0} or {@code index1}
	 * is out of range (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void moveEdge(Structure structure, int index0, int index1);

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
	void moveEdge(Structure structure, Edge edge, int index);

	/**
	 * Changes the specified terminal (source or target) of the given edge to
	 * the supplied markable. Note that the {@code markable} argument has to
	 * be already contained within the "node" container of this structure.
	 *
	 * @param edge The edge whose terminal should be changed
	 * @param item The new terminal for the edge
	 * @param isSource Specifies which terminal (source or target) should be changed
	 * @throws NullPointerException if either one the {@code edge} or {@code markable}
	 * argument is {@code null}
	 * @throws IllegalArgumentException if the given {@code markable} is unknown to
	 * this structure (i.e. not a member of its "node" container")
	 * @throws IllegalArgumentException if the given {@code markable} is not a valid
	 * candidate for the specified terminal
	 */
	void setTerminal(Structure structure, Edge edge, Item item, boolean isSource);

	//---------------------------------------------
	//			EDGE METHODS
	//---------------------------------------------

	Structure getStructure(Edge edge);

	Item getSource(Edge edge);

	Item getTarget(Edge edge);

	void setSource(Edge edge, Item item);

	void setTarget(Edge edge, Item item);

	boolean isDirected();

	//---------------------------------------------
	//			FRAGMENT METHODS
	//---------------------------------------------

	/**
	 * Returns the markable this fragment is a part of.
	 *
	 * @return
	 */
	Item getItem(Fragment fragment);

	/**
	 * Returns the position within the surrounding markable of
	 * this fragment that denotes the actual begin of the fragment itself.
	 *
	 * @return
	 */
	Position getFragmentBegin(Fragment fragment);

	/**
	 * Returns the position within the surrounding markable of
	 * this fragment that denotes the actual end of the fragment itself.
	 *
	 * @return
	 */
	Position getFragmentEnd(Fragment fragment);

	// Modification methods

	/**
	 * Changes the begin position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @return the previous begin position of the fragment
	 * @throws PositionOutOfBoundsException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting markable
	 */
	Position setFragmentBegin(Fragment fragment, Position position);

	/**
	 * Changes the end position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @return the previous end position of the fragment
	 * @throws PositionOutOfBoundsException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting markable
	 */
	Position setFragmentEnd(Fragment fragment, Position position);

	//---------------------------------------------
	//			ANNOTATION METHODS
	//---------------------------------------------

	/**
	 * Collects all the keys in this layer which are mapped to valid annotation values for
	 * the given markable. This method returns {@code true} iff at least one key was added
	 * to the supplied {@code buffer}. Note that this method does <b>not</b> take
	 * default annotations into consideration, since they are not accessed via a dedicated
	 * key!
	 *
	 * @param item
	 * @param buffer
	 * @return
	 * @throws NullPointerException if any one of the two arguments is {@code null}
	 * @throws UnsupportedOperationException if this layer does not support additional keys
	 */
	boolean collectKeys(AnnotationLayer layer, Item item, Collector<String> buffer);

	/**
	 * Returns the annotation for a given markable and key or {@code null} if that markable
	 * has not been assigned an annotation value for the specified key in this layer.
	 * Note that the returned object can be either an actual value or an {@link Annotation}
	 * instance that wraps a value and provides further information.
	 *
	 * @param item
	 * @param key
	 * @return
	 * @throws NullPointerException if either the {@code markable} or {@code key}
	 * is {@code null}
	 * @throws UnsupportedOperationException if this layer does not support additional keys
	 */
	Object getValue(AnnotationLayer layer, Item item, String key);

	int getIntegerValue(AnnotationLayer layer, Item item, String key);
	long getLongValue(AnnotationLayer layer, Item item, String key);
	float getFloatValue(AnnotationLayer layer, Item item, String key);
	double getDoubleValue(AnnotationLayer layer, Item item, String key);
	boolean getBooleanValue(AnnotationLayer layer, Item item, String key);

	/**
	 * Deletes all annotations in this layer
	 * <p>
	 * Note that this does include all annotations for all keys,
	 * not only those declares for the default annotation.
	 *
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues(AnnotationLayer layer);

	/**
	 * Deletes in this layer all annotations for
	 * the given {@code key}.
	 *
	 * @param key The key for which annotations should be
	 * deleted
	 * @throws UnsupportedOperationException if this layer does not allow multiple keys
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues(AnnotationLayer layer, String key);

	/**
	 * Removes from this layer all annotations for the given
	 * markable.
	 * <p>
	 * If the {@code recursive} parameter is {@code true} and the supplied
	 * {@code markable} is a {@link Container} or {@link Structure} then all
	 * annotations defined for members of it should be removed as well.
	 *
	 * @param item the {@code Item} for which annotations should be removed
	 * @param recursive if {@code true} removes all annotations defined for
	 * elements ({@code Item}s and {@code Edge}s alike) in the supplied
	 * {@code Item}
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues(AnnotationLayer layer, Item item, boolean recursive);

	/**
	 * Assigns the given {@code value} as new annotation for the specified
	 * {@code Item} and {@code key}, replacing any previously defined value.
	 * If the {@code value} argument is {@code null} any stored annotation
	 * for the combination of {@code markable} and {@code key} will be deleted.
	 * <p>
	 * This is an optional method
	 *
	 * @param item The {@code Item} to change the annotation value for
	 * @param key the key for which the annotation should be changed
	 * @param value the new annotation value or {@code null} if the annotation
	 * for the given {@code markable} and {@code key} should be deleted
	 * @throws NullPointerException if the {@code markable} or {@code key}
	 * argument is {@code null}
	 * @throws IllegalArgumentException if the supplied {@code value} is not
	 * contained in the {@link ValueSet} of this layer's manifest for the given {@code key}.
	 * This is only checked if the manifest actually defines such restrictions.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	Object setValue(AnnotationLayer layer, Item item, String key, Object value);

	int setIntegerValue(AnnotationLayer layer, Item item, String key, int value);
	long setLongValue(AnnotationLayer layer, Item item, String key, long value);
	float setFloatValue(AnnotationLayer layer, Item item, String key, float value);
	double setDoubleValue(AnnotationLayer layer, Item item, String key, double value);
	boolean setBooleanValue(AnnotationLayer layer, Item item, String key, boolean value);

	/**
	 *
	 * @return {@code true} iff the layer holds at least one valid annotation object.
	 */
	boolean hasAnnotations(AnnotationLayer layer);

	/**
	 *
	 * @return {@code true} iff the layer holds at least one valid annotation object
	 * for the specified markable.
	 */
	boolean hasAnnotations(AnnotationLayer layer, Item item);
}
