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
