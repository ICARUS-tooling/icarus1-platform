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
package de.ims.icarus.language.model.mutation.batch;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface BatchMutator {
	
	/**
	 * Tries to acquire the modification lock on the underlying
	 * corpus member and returns {@code true} if successful.
	 * 
	 * @return {@code true} iff the modification lock on the
	 * mutated corpus member has successfully been acquired
	 */
	boolean beginBatch();

	/**
	 * Discards all accumulated batch operations and releases
	 * the previously acquired modification lock.
	 * 
	 * @throws IllegalStateException if this mutator does not
	 * currently hold the modification lock on the underlying
	 * corpus member
	 */
	void discardBatch();
	
	/**
	 * 
	 * @return {@code true} iff all pending batch operations have
	 * been executed successfully
	 * @throws IllegalStateException if this mutator does not
	 * currently hold the modification lock on the underlying
	 * corpus member
	 */
	boolean executeBatch();
}
