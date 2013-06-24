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

import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;
import net.ikarus_systems.icarus.search_tools.standard.GroupCache;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyGroupConstraint extends DefaultConstraint {
	
	private static final long serialVersionUID = -1351042489767331758L;
	
	private final SearchConstraint source;
	private final GroupCache cache;

	public DummyGroupConstraint(SearchConstraint source, GroupCache cache) {
		super(source.getToken(), source.getValue(), source.getOperator());
		
		this.source = source;
		this.cache = cache;
	}

	/**
	 * Instead of doing a match check this implementation
	 * directly delegates to the underlying {@code GroupCache}'s
	 * {@link GroupCache#cacheGroupInstance(int, Object)} method.
	 * 
	 * @see net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object value) {
		cache.cacheGroupInstance(getGroupId(), value);
		
		return true;
	}
	
	public int getGroupId() {
		return (int) getConstraint();
	}

	public SearchConstraint getSource() {
		return source;
	}

	public GroupCache getCache() {
		return cache;
	}
}