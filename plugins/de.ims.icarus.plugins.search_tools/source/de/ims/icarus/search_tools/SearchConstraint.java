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
package de.ims.icarus.search_tools;

import java.io.Serializable;

import de.ims.icarus.search_tools.standard.GroupCache;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchConstraint extends Serializable {

	/**
	 * Returns the token used to identify this constraint's
	 * factory within a certain {@link ConstraintContext}.
	 */
	String getToken();

	/**
	 * Returns the value this constraint is meant to match
	 * against.
	 */
	Object getValue();

	/**
	 * Returns the optional specifier used to further restrict
	 * the matching process.
	 */
	Object getSpecifier();

	SearchOperator getOperator();

	SearchConstraint clone();

	boolean matches(Object value);

	Object getInstance(Object value);

	Object getLabel(Object value);

	boolean isUndefined();

	void setActive(boolean active);

	boolean isActive();

	boolean isMultiplexing();

	void group(GroupCache cache, int groupId, Object value);
}
