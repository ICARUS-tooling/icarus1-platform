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

import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.GroupCache;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyGroupConstraint implements SearchConstraint {
	
	private static final long serialVersionUID = -1351042489767331758L;
	
	private final SearchConstraint source;
	private final GroupCache cache;

	public DummyGroupConstraint(SearchConstraint source, GroupCache cache) {
		this.source = source;
		this.cache = cache;
	}

	/**
	 * Instead of doing a match check this implementation
	 * directly delegates to the underlying {@code GroupCache}'s
	 * {@link GroupCache#cacheGroupInstance(int, Object)} method.
	 * 
	 * @see de.ims.icarus.search_tools.standard.DefaultConstraint#matches(java.lang.Object)
	 * @see GroupCache#cacheGroupInstance(int, Object)
	 */
	@Override
	public boolean matches(Object value) {
		cache.cacheGroupInstance(getGroupId(), getInstance(value));
		
		return true;
	}
	
	@Override
	public DummyGroupConstraint clone() {
		return new DummyGroupConstraint(source, cache); 
	}

	public int getGroupId() {
		return (int) getValue();
	}

	public SearchConstraint getSource() {
		return source;
	}

	public GroupCache getCache() {
		return cache;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getToken()
	 */
	@Override
	public String getToken() {
		return source.getToken();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getValue()
	 */
	@Override
	public Object getValue() {
		return source.getValue();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getOperator()
	 */
	@Override
	public SearchOperator getOperator() {
		return source.getOperator();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getInstance(java.lang.Object)
	 */
	@Override
	public Object getInstance(Object value) {
		return source.getInstance(value);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isUndefined()
	 */
	@Override
	public boolean isUndefined() {
		return false;
	}
}