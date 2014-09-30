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
package de.ims.icarus.model.api.members;

import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ManifestOwner;
import de.ims.icarus.model.standard.elements.MemberSets;


/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Container extends Markable, Iterable<Markable>, ManifestOwner<ContainerManifest> {

	public static final MemberSet<Container> EMPTY_BASE_SET = MemberSets.emptySet();

	/**
	 * Returns the type of this container. This provides
	 * information about how contained {@code Markable}s are ordered and
	 * if they represent a continuous subset of the corpus.
	 *
	 * @return The {@code ContainerType} of this {@code Container}
	 * @see ContainerType
	 */
	ContainerType getContainerType();

	/**
	 * Returns the {@link ContainerManifest} object that holds additional
	 * information about this container.
	 *
	 * @return
	 */
	@Override
	ContainerManifest getManifest();

	/**
	 * @return The underlying containers if this container relies on the
	 * elements of other container objects. If the container is independent of any
	 * other containers it should return the shared empty {@code MemberSet} available
	 * via {@link #EMPTY_BASE_SET}.
	 */
	MemberSet<Container> getBaseContainers();

	/**
	 * Returns the {@code Container} that serves as bounding
	 * box for the markables in this container. In most cases
	 * this will be a member of another {@code MarkableLayer}
	 * that represents the sentence or document level. If this
	 * {@code Container} object only builds a virtual collection
	 * atop of other markables and is not limited by previously
	 * defined <i>boundary containers</i> then this method should
	 * return {@code null}.
	 * <p>
	 * This is an optional method.
	 *
	 * @return
	 */
	Container getBoundaryContainer();

	/**
	 * Returns the number of {@code Markable} objects hosted within this
	 * container.
	 * <p>
	 * Note that this does <b>not</b> include possible {@code Edge}s stored
	 * within this container in case it is a {@link Structure}!
	 *
	 * @return The number of {@code Markable}s in this container
	 */
	int getMarkableCount();

	/**
	 * Returns the {@code Markable} stored at position {@code index} within
	 * this {@code Container}. Note that however elements in a container may
	 * be unordered depending on the {@code ContainerType} as returned by
	 * {@link #getErrorType()}, the same index has always to be mapped to
	 * the exact same {@code Markable} within a single container!
	 *
	 * @param index The index of the {@code Markable} to be returned
	 * @return The {@code Markable} at position {@code index} within this container
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getMarkableCount()</tt>)
	 */
	Markable getMarkableAt(int index);

	/**
	 * Returns the index of the given {@code Markable} within this container's
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
	 * @param markable The {@code Markable} whose index is to be returned
	 * @return The index at which the {@code Markable} appears within this
	 * container or {@code -1} if the markable is not hosted within this container.
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 */
	int indexOfMarkable(Markable markable);

	/**
	 * Returns {@code true} if this container hosts the specified markable.
	 * Essentially equal to receiving {@code -1} as result to a {@link #indexOfMarkable(Markable)}
	 * call.
	 *
	 * @param markable The markable to check
	 * @return {@code true} iff this container hosts the given markable
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 *
	 * @see #indexOfMarkable(Markable)
	 */
	boolean containsMarkable(Markable markable);

	/**
	 * Removes from the mutating container all elements.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void removeAllMarkables();

	/**
	 * Adds a new markable to this container
	 *
	 * @param markable
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void addMarkable(Markable markable);

	/**
	 * Adds a new markable to this container
	 *
	 * Note that calling this method with an {@code index} parameter
	 * equal to the size of the mutating container as returned by
	 * {@link Container#getMarkableCount()} is equivalent to
	 * using {@link #addMarkable()}.
	 *
	 * @param index The position to insert the new markable at
	 * @param markable
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt; getMarkableCount()</tt>)
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void addMarkable(int index, Markable markable);

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
	Markable removeMarkable(int index);

	/**
	 * First determines the index of the given markable object within
	 * this container and then calls {@link #removeMarkable(int)}.
	 *
	 * @param markable
	 * @return
	 * @see Container#indexOfMarkable(Markable)
	 */
	Markable removeMarkable(Markable markable);

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
	void moveMarkable(int index0, int index1);

	/**
	 * Shorthand method for moving a given markable object.
	 *
	 * First determines the index of the given markable object within
	 * this container and then calls {@link #moveMarkable(int, int)}.
	 *
	 * @param markable The markable to be moved
	 * @param index The position the {@code markable} argument should be moved to
	 * @see Container#indexOfMarkable(Markable)
	 */
	void moveMarkable(Markable markable, int index);
}
