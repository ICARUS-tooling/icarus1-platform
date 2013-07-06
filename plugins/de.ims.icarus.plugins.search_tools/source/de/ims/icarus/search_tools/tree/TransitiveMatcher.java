/*
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
	
	protected Stack<IndexIterator> cache = new Stack<>(); 
	
	protected boolean matched = false;

	public TransitiveMatcher(SearchNode node, SearchEdge edge) {
		super(node, edge);
	}
	
	@Override
	protected void innerClose() {
		cache.clear();
	}
	
	protected IndexIterator newIterator() {
		IndexIterator iterator = cache.isEmpty() ? null : cache.pop();
		
		if(iterator==null) {
			iterator = indexIterator.clone();
		}
		
		return iterator;
	}
	
	protected void recycleIterator(IndexIterator iterator) {
		cache.push(iterator);
	}

	@Override
	public boolean matches() {						
		int parentAllocation = parent.getAllocation();
		
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
