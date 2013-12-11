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

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.ContainerType;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.mutation.batch.BatchMutator;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ContainerMutator extends Mutator<Container>, BatchMutator {

	/**
	 * Changes the type of the mutating container
	 * @param containerType The new type of the mutating container
	 */
	void setContainerType(ContainerType containerType);

	/**
	 * Removes from the mutating container all elements.
	 */
	void removeAllMarkables();

	/**
	 * Creates a new markable as member of the mutating container
	 * and appends it to the end of the internal storage.
	 * 
	 * @return The newly created member of the container
	 */
	Markable addMarkable();

	/**
	 * Creates a new markable as member of the mutating container
	 * and inserts it at the specified position in the internal
	 * storage.
	 * 
	 * Note that calling this method with an {@code index} parameter
	 * equal to the size of the mutating container as returned by
	 * {@link Container#getMarkableCount()} is equivalent to
	 * using {@link #addMarkable()}.
	 * 
	 * @param index The position to insert the new markable at
	 * @return The newly created member of the container
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt; getSubject().getMarkableCount()</tt>)
	 */
	Markable addMarkable(int index);

	/**
	 * Removes and returns the markable at the given index. Shifts the
	 * indices of all markables after the given position to account
	 * for the missing member.
	 * 
	 * @param index The position of the markable to be removed
	 * @return The markable previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getSubject().getMarkableCount()</tt>)
	 */
	Markable removeMarkable(int index);

	/**
	 * First determines the index of the given markable object within the
	 * mutated container and then calls {@link #removeMarkable(int)}.
	 * 
	 * @param markable
	 * @return
	 * @see Container#indexOfMarkable(Markable)
	 */
	Markable removeMarkable(Markable markable);

	/**
	 * Moves the markable currently located at position {@code index0}
	 * over to position {@code index1}.
	 * 
	 * @param index0
	 * @param index1
	 * @throws IllegalArgumentException if <tt>index0 == index1</tt>
	 * @throws IndexOutOfBoundsException if either {@code index0} or {@code index1}
	 * is out of range (<tt>index &lt; 0 || index &gt;= getSubject().getMarkableCount()</tt>)
	 */
	void moveMarkable(int index0, int index1);

	/**
	 * Shorthand method for moving a given markable object.
	 * 
	 * First determines the index of the given markable object within the
	 * mutated container and then calls {@link #moveMarkable(int, int)}.
	 * 
	 * @param markable The markable to be moved
	 * @param index The position the {@code markable} argument should be moved to
	 * @see Container#indexOfMarkable(Markable)
	 */
	void moveMarkable(Markable markable, int index);

	// BATCH OPERATIONS

	void batchSetContainerType(ContainerType containerType);

	void batchRemoveAll();

	void batchAddMarkable();

	void batchAddMarkable(int index);

	void batchRemoveMarkable(int index);

	void batchRemoveMarkable(Markable markable);

	void batchMoveMarkable(int index0, int index1);

	void batchMoveMarkable(Markable markable, int index1);
}
