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
package de.ims.icarus.search_tools.tree;

import de.ims.icarus.search_tools.standard.DefaultGraphNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProxyRootMatcher extends Matcher {

	public ProxyRootMatcher() {
		super(new DefaultGraphNode(), null);
	}

	@Override
	public boolean matches() {
		if(!matchesExclusions()) {
			if(next!=null) {
				return next.matches();
			} else {
				// In case all the root matchers are negated
				commit();
				return true;
			}
		} else if(alternate!=null) {
			return alternate.matches();
		} else {
			return false;
		}
	}

	@Override
	public int getAllocation() {
		return -1;
	}

	@Override
	protected boolean matchesNext() {
		return false;
	}

	@Override
	protected boolean matchesConstraints() {
		return false;
	}

	/*@Override
	protected void commit() {
		// no-op
	}*/

	@Override
	protected void cacheHits() {
		// no-op
	}

	@Override
	public void deallocate() {
		// no-op
	}

	@Override
	protected void allocate() {
		// no-op
	}

}
