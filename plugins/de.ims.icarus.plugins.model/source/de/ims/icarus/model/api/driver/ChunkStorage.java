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

import de.ims.icarus.model.api.members.Markable;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ChunkStorage {

	/**
	 * Adds the given {@code member} to the storage, using the specified
	 * {@code index}. The storage should treat the markable as not being
	 * fully linked (i.e. it might for example return an actual index value
	 * different from the {@code index} argument). It is perfectly legal for
	 * a driver implementation to delay final initialization and integrity checks
	 * till the end of a batch load operation. The {@code index} argument is provided
	 * so that the storage is already able to perform a proper mapping and store the
	 * markable at the correct location.
	 *
	 * @param member
	 * @param index
	 */
	void add(Markable member, long index);
}
