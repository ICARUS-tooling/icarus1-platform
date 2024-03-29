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

import de.ims.icarus.util.Options;



/**
 * Describes and
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ConstraintFactory {

	public static final int EDGE_CONSTRAINT_TYPE = 1;
	public static final int NODE_CONSTRAINT_TYPE = 2;

	SearchConstraint createConstraint(Object value, SearchOperator operator, Object specifier, Options options);

	SearchOperator[] getSupportedOperators();

	String getName();

	String getDescription();

	String getToken();

	/**
	 * Returns the class of supported values. This is a hint for editors
	 * or other user interface elements on what kind of component should
	 * be used to present the constraint. If the return value is {@code null}
	 * than only the values returned by {@link #getValueSet()} are considered
	 * legal!
	 */
	Class<?> getValueClass(Object specifier);

	/**
	 * Returns the value to be used as constraint in the case that
	 * no user input was made.
	 */
	Object getDefaultValue(Object specifier);

	/**
	 * Returns a collection of possible values that should be displayed to the
	 * user when editing the constraint. If {@link #getValueClass()} returns
	 * {@code null} these values are considered to be the only legal collection
	 * of possible values!
	 */
	Object[] getLabelSet(Object specifier);

	/**
	 * Transforms or parses the given {@code label} into a value
	 * suitable for {@code SearchConstraint} objects created by this factory.
	 */
	Object labelToValue(Object label, Object specifier);

	/**
	 * Transforms the given {@code value} into a {@code label} object
	 * that can be used for interface elements presented to the user.
	 */
	Object valueToLabel(Object value, Object specifier);

	int getConstraintType();

	// TODO add mechanics to create multiple instances of constraint and to
	// obtain min and max allowed instance count

	/**
	 * Returns the minimum required count of constraint instances
	 * created by this factory. A value of {@code -1} allows the
	 * user interface to make that decision.
	 */
	int getMinInstanceCount();

	/**
	 * Returns the maximum allowed count of constraint instances
	 * created by this factory. A value of {@code -1} deactivates
	 * the upper limit and allows the user interface to handle the
	 * decision. Note that aside from that reserved return value all
	 * values that are less than the current minimum as obtained from
	 * {@link #getMinInstanceCount()} will cause exceptions.
	 */
	int getMaxInstanceCount();

	Object[] getSupportedSpecifiers();
}
