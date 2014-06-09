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
package de.ims.icarus.language.model.standard.index;

import de.ims.icarus.language.model.api.driver.IndexSet;
import de.ims.icarus.language.model.api.driver.indexing.IndexCollector;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IndexSetBuilder implements IndexCollector {

	private final int setSizeLimit;

	public IndexSetBuilder(int setSizeLimit) {
		if(setSizeLimit<1)
			throw new IllegalArgumentException("Size limit of index sets must be greater than 0: "+setSizeLimit); //$NON-NLS-1$

		this.setSizeLimit = setSizeLimit;
	}

	@Override
	public void add(long index) {

	}

	public void add(long fromIndex, long toIndex) {

	}

	@Override
	public void add(IndexSet indexSet) {

	}

	public void add(long[] indices) {

	}

	public void add(IndexSet[] indices) {

	}

	public IndexSet[] build() {

	}
}
