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
package de.ims.icarus.language.model.api.edit;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum EditOperation {

	/**
	 * Describes the adding of a new element either
	 * at the end of a collection or as its first
	 * element. This is essentially the operation of
	 * appending an element to either the head or tail
	 * of a list.
	 */
	ADD,

	/**
	 * Adding an element is supported at every random
	 * position within the collection.
	 */
	ADD_RANDOM,

	/**
	 * Removal of an element is only possible on one of
	 * the two ends of a the list.
	 */
	REMOVE,

	/**
	 * Any element in the list can be removed at any time
	 */
	REMOVE_RANDOM,

	/**
	 * All elements can be removed with one atomic operation
	 */
	CLEAR,

	/**
	 * An element can be moved within the collection between
	 * random positions.
	 */
	MOVE,

	/**
	 * A special kind of operation only affecting edges.
	 * Allows to change the source or target terminal of an edge.
	 */
	LINK;
}
