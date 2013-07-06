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

import net.ikarus_systems.icarus.search_tools.standard.DefaultGraphNode;

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
