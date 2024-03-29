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

import java.util.Stack;

import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchNode;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TransitiveMatcher extends Matcher {

	protected Stack<IndexIterator> iteratorCache = new Stack<>();

	protected boolean matched = false;

	public TransitiveMatcher(SearchNode node, SearchEdge edge) {
		super(node, edge);
	}

	@Override
	protected void innerClose() {
		iteratorCache.clear();
	}

	protected IndexIterator newIterator() {
		IndexIterator iterator = iteratorCache.isEmpty() ? null : iteratorCache.pop();

		if(iterator==null) {
			iterator = indexIterator.clone();
		}

		return iterator;
	}

	protected void recycleIterator(IndexIterator iterator) {
		iteratorCache.push(iterator);
	}

	@Override
	public boolean matches() {
		int parentAllocation = parent.getAllocation();

		//FIXME switch to the isLegalIndex(int) method  and traverse space instead of premature restriction
		int minIndex = getMinIndex();
		int maxIndex = getMaxIndex();

		matched = false;

		if(minIndex<=maxIndex) {
			search(parentAllocation, minIndex, maxIndex);
		}

		// Return scope to parent node
		targetTree.viewNode(parentAllocation);

		// If unsuccessful and part of a disjunction let the
		// alternate matcher have a try.
		if(!matched && alternate!=null) {
			matched = alternate.matches();
		}

		return matched;
	}

	protected void search(int index, int minIndex, int maxIndex) {

		targetTree.viewNode(index);
		indexIterator.setMax(targetTree.getEdgeCount()-1);

		// Early return in case of unfruitful path
		if(!indexIterator.hasNext()) {
			return;
		}

		while(indexIterator.hasNext()) {
			targetTree.viewNode(index);
			targetTree.viewChild(indexIterator.next());

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

			if(isDone()) {
				return;
			}
		}

		targetTree.lockNode(index);

		// Continue recursive
		targetTree.viewNode(index);
		IndexIterator iterator = newIterator();
		iterator.setMax(targetTree.getEdgeCount()-1);
		while(iterator.hasNext()) {
			search(targetTree.getChildIndexAt(index, iterator.next()), minIndex, maxIndex);

			if(isDone()) {
				break;
			}
		}

		targetTree.unlockNode(index);

		recycleIterator(iterator);
	}

	protected boolean isDone() {
		return matched && (exclusionMember || !exhaustive);
	}
}
