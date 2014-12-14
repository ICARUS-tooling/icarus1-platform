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

import de.ims.icarus.search_tools.SearchMode;
import de.ims.icarus.search_tools.SearchNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RootMatcher extends Matcher {

	public RootMatcher(SearchNode node) {
		super(node, null);

		exclusionMember = node.isNegated();
	}

	@Override
	public boolean matches() {

		int nodeCount = targetTree.size();

		boolean matched = false;

		int minIndex = getMinIndex();
		int maxIndex = getMaxIndex();

		indexIterator.setMax(nodeCount-1);

		if(minIndex<=maxIndex) {
			while(indexIterator.hasNext()) {
				targetTree.viewNode(indexIterator.next());

				// Check for precedence constraints
				if(targetTree.getNodeIndex()<minIndex
						|| targetTree.getNodeIndex()>maxIndex) {
					continue;
				}

				// Honor locked nodes that are allocated to other matchers!
				if(targetTree.isNodeLocked()) {
					continue;
				}

				// Check for type constraints
				if(!matchesType()) {
					continue;
				}

				// Check for structural constraints
				if(targetTree.getDescendantCount()<descendantCount
						|| targetTree.getHeight()<height) {
					continue;
				}

				// Check for required number of children
				if(targetTree.getEdgeCount()<childCount) {
					continue;
				}

				// Check if the current node is a potential match
				if(!matchesConstraints()) {
					continue;
				}

				// Lock allocation
				allocate();

				// Search for child matchers that serve as exclusions
				if(!matchesExclusions()) {
					// Delegate further search to the next matcher
					// or otherwise commit current match
					matched |= matchesNext();
				}

				// Release lock
				deallocate();

				// Stop search if only one successful hit is required.
				// This is the case when either a non-exhaustive search
				// takes place or the matcher is a part of a sub-tree
				// serving as exclusion
				if(matched && (exclusionMember || !exhaustive)) {
					break;
				}
			}
		}



		if(matched && !exclusionMember && previous==null && searchMode!=SearchMode.INDEPENDENT_HITS) {
			commit();
		} else if(!matched || exhaustive) {
			// If unsuccessful and part of a disjunction let the
			// alternate matcher have a try.
			if(alternate!=null) {
				alternate.matches();
			}
		}

		return matched;
	}
}
