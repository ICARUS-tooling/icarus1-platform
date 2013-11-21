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
package de.ims.icarus.language.model;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Container extends Markable, Iterable<Markable> {
	
	/**
	 * @return The underlying container if this container relies on the
	 * elements of another container object or {@code null} otherwise. 
	 */
	Container getBaseContainer();
	
	/**
	 * Returns the number of {@code Markable} objects hosted within this
	 * container.
	 * 
	 * @return The number of {@code Markable}s in this container
	 */
	int getMarkableCount();
	
	/**
	 * Returns the {@code Markable} stored at position {@code index} within
	 * this {@code Container}. Note that however elements in a container may
	 * be unordered depending on the {@code ContainerType} as returned by
	 * {@link #getType()}, the same index has always to be mapped to
	 * the exact same {@code Markable} within a single container!
	 * 
	 * @param index The index of the {@code Markable} to be returned
	 * @return The {@code Markable} at position {@code index} within this container
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getMarkableCount()</tt>)
	 */
	Markable getMarkableAt(int index);
	
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
	 * Returns the index of the given {@code Markable} within this containers
	 * list of markables or {@code -1} if the markable is not hosted within this
	 * container.
	 * <p>
	 * Note that for every markable <i>m</i> that is hosted within some container the 
	 * following will always return a result different from {@code -1}:<br>
	 * {@code m.getContainer().indexOfMarkable(m)}
	 * 
	 * @param markable The {@code Markable} whose index is to be returned
	 * @return The index at which the {@code Markable} appears within this
	 * container or {@code -1} if the markable is not hosted within this container.
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 */
	int indexOfMarkable(Markable markable);
}
