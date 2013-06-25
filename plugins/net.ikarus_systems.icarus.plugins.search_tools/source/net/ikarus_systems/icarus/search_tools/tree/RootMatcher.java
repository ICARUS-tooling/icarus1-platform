/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.tree;

import net.ikarus_systems.icarus.search_tools.SearchMode;
import net.ikarus_systems.icarus.search_tools.SearchNode;

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
		boolean excluded = false;
		
		indexIterator.setMax(nodeCount-1);
		
		while(indexIterator.hasNext()) {
			targetTree.viewNode(indexIterator.next());
			
			// Honor locked nodes that are allocated to other matchers!
			if(targetTree.isNodeLocked()) {
				continue;
			}
			
			// Check for structural constraints 
			if(targetTree.getDescendantCount()<descendantCount
					|| targetTree.getHeight()<height) {
				continue;
			}
			
			// Check for required number of children
			if(targetTree.getEdgeCount()<node.getChildCount()) {
				continue;
			}
			
			// Check if the current node is a potential match
			if(!matchesConstraints()) {
				continue;
			}
			
			// Lock allocation
			allocate();
			
			// Search for child matchers that serve as exclusions
			excluded = false;
			if(exclusions!=null) {
				for(Matcher matcher : exclusions) {
					if(matcher.matches()) {
						excluded = true; 
						break;
					}
				}
			}
			
			if(!excluded) {
				// Delegate further search to the next matcher
				// or otherwise commit current match
				if(next!=null) {
					matched |= next.matches();
				} else if(!exclusionMember) {
					cacheHits();
					if(searchMode==SearchMode.INDEPENDENT_HITS) {
						commit();
					}
					matched = true;
				}
			}

			// Release lock
			deallocate();
			
			// Stop search if only one successful hit is required
			// This is the case when either a non-exhaustive search
			// takes place or the matcher is a part of a sub-tree
			// serving as exclusion
			if(matched && (exclusionMember || !exhaustive)) {
				break;
			}
		}
		
		// If unsuccessful and part of a disjunction let the 
		// alternate matcher have a try.
		if(!matched) {
			if(exclusionMember) {
				commit();
			} else if(alternate!=null) {
				matched = alternate.matches();
			}
		} else if(searchMode!=SearchMode.INDEPENDENT_HITS) {
			commit();
		}
		
		return matched;
	}

}