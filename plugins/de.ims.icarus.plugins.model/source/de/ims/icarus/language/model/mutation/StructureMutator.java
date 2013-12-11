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
package de.ims.icarus.language.model.mutation;

import de.ims.icarus.language.model.Edge;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.Structure;
import de.ims.icarus.language.model.StructureType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface StructureMutator extends ContainerMutator {

	@Override
	Structure getSubject();

	/**
	 * Changes the type of the mutating structure
	 * @param structureType
	 */
	void setStructureType(StructureType structureType);

	/**
	 * Removes from the mutating structure all edges.
	 */
	void removeAllEdges();

	/**
	 * Creates a new edge as member of the mutating structure
	 * and appends it to the end of the internal storage.
	 * 
	 * @return The newly created member of the container
	 * @param source
	 * @param target
	 */
	Markable addEdge(Markable source, Markable target);

	/**
	 * Creates a new markable as member of the mutating container
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
	 */
	Edge addEdge(Markable source, Markable target, int index);

	/**
	 * Removes and returns the edge at the given index. Shifts the
	 * indices of all edges after the given position to account
	 * for the missing member.
	 * 
	 * @param index The position of the edge to be removed
	 * @return The markable previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getSubject().getEdgeCount()</tt>)
	 */
	Edge removeEdge(int index);

	/**
	 * First determines the index of the given edge object within the
	 * mutating structure and then calls {@link #removeEdge(int)}.
	 * 
	 * @param markable
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
	 */
	void moveEdge(int index0, int index1);

	/**
	 * Shorthand method for moving a given edge object.
	 * 
	 * First determines the index of the given edge object within the
	 * mutated container and then calls {@link #moveEdge(int, int)}.
	 * 
	 * @param markable The markable to be moved
	 * @param index The position the {@code edge} argument should be moved to
	 * @see Structure#indexOfEdge(Edge)
	 */
	void moveEdge(Edge edge, int index);

	// BATCH OPERATIONS

	void batchSetStructureType(StructureType structureType);

	void batchRemoveAllEdges();

	void batchAddEdge(Markable source, Markable target);

	void batchAddEdge(Markable source, Markable target, int index);

	void batchRemoveEdge(int index);

	void batchRemoveEdge(Markable markable);

	void batchMoveEdge(int index0, int index1);

	void batchMoveEdge(Edge edge, int index1);
}
