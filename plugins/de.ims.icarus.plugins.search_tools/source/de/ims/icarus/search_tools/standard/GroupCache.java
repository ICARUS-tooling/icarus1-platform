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
package de.ims.icarus.search_tools.standard;

import de.ims.icarus.search_tools.result.ResultEntry;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface GroupCache {

	void cacheGroupInstance(int id, Object value, boolean replace);

	void lock();

	void reset();

	void commit(ResultEntry entry);

	/**
	 *
	 */
	public static GroupCache dummyCache = new GroupCache() {

		@Override
		public void cacheGroupInstance(int id, Object value, boolean replace) {
			// do nothing
		}

		@Override
		public void lock() {
			// no-op
		}

		@Override
		public void reset() {
			// no-op
		}

		@Override
		public void commit(ResultEntry entry) {
			// no-op
		}
	};
}
