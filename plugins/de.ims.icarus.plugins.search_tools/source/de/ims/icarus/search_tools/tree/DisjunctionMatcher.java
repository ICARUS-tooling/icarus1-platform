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

import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DisjunctionMatcher extends Matcher {

	public DisjunctionMatcher(SearchNode node, SearchEdge edge) {
		super(node, edge);
	}

	/**
	 * Returns {@code true} if at least on of the
	 * {@code Matcher} instances registered as exclusions
	 * does {@code not} return a successful match or if there
	 * are no matchers registered as exclusion.
	 */
	@Override
	protected boolean matchesExclusions() {
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				if(!matcher.matches()) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	protected boolean matchesType() {
		return true;
	}

	@Override
	protected boolean matchesConstraints() {
		return true;
	}

	@Override
	protected void commit() {
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

	@Override
	public int getAllocation() {
		return parent==null ? -1 : parent.getAllocation();
	}

	@Override
	public boolean matches() {
		
		boolean matched = false;
		
		// Check exclusions
		matched = matchesExclusions();
		
		if(matched) {
			matched = matchesNext();
		}
		
		if(options!=null && (!matched || exhaustive)) {
			for(Matcher option : options) {
				matched |= option.matches();
				
				if(matched && !exhaustive) {
					break;
				}
			}
		}
			
		return matched;
	}
}
