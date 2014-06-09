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
package de.ims.icarus.model.api.driver;


/**
 * Models an arbitrary collection of {@code long} index values. Note that
 * the elements in an index-set must always occur in sorted order! This
 * condition is mandatory to enable an easy check for continuous collections
 * of indices:<br>
 * <i>Let i_0 be the first index in the set and i_n the last, with the set holding
 * n indices, then the collection of indices is continuous, if and only if the
 * difference i_n-i_0 is exactly n-1</i>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface IndexSet {

	int size();

//	LongIterator getIterator();

	long indexAt(int index);

	/**
	 * Splits the current set of indices so that each new subset contains at most
	 * the given number of indices. This method is forced to be implemented by actual
	 * {@code IndexSet} classes to better exploit their underlying data structure since
	 * framework code cannot optimize for unknown implementation details.
	 *
	 * @param chunkSize
	 * @return
	 */
	IndexSet[] split(int chunkSize);
}
