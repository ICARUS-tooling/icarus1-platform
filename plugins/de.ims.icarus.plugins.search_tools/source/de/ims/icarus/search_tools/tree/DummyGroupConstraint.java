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
		if(source.isMultiplexing()) {
			source.group(cache,  getGroupId(), value);
		} else {
			cache.cacheGroupInstance(getGroupId(), getLabel(getInstance(value)), true);
		}

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

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#setActive(boolean)
	 */
	@Override
	public void setActive(boolean active) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isActive()
	 */
	@Override
	public boolean isActive() {
		return true;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getSpecifier()
	 */
	@Override
	public Object getSpecifier() {
		return source.getSpecifier();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getLabel(java.lang.Object)
	 */
	@Override
	public Object getLabel(Object value) {
		return source.getLabel(value);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isMultiplexing()
	 */
	@Override
	public boolean isMultiplexing() {
		return false;
	}

	@Override
	public void group(GroupCache cache, int groupId, Object value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#prepare()
	 */
	@Override
	public void prepare() {
		source.prepare();
	}
}