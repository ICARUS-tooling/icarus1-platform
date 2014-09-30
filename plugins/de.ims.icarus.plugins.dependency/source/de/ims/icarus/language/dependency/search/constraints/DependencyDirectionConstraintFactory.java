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
package de.ims.icarus.language.dependency.search.constraints;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.dependency.search.DependencyTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyDirectionConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "direction"; //$NON-NLS-1$

	public DependencyDirectionConstraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.direction.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.direction.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new DependencyDirectionConstraint(value, operator);
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return null;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[]{
				DefaultSearchOperator.EQUALS,
				DefaultSearchOperator.GROUPING,
		};
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return LanguageUtils.parseDirectionLabel((String)label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return LanguageUtils.getDirectionLabel((int)value);
	}

	@Override
	public Object[] getLabelSet(Object specifier) {
		return new Object[]{
				LanguageConstants.DATA_UNDEFINED_LABEL,
				LanguageConstants.DATA_LEFT_LABEL,
				LanguageConstants.DATA_RIGHT_LABEL,
		};
	}

	private static class DependencyDirectionConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 8874429868140453623L;

		public DependencyDirectionConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getLabel(Object value) {
			return LanguageUtils.getDirectionLabel((int)value);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getDirection();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyDirectionConstraint(getValue(), getOperator());
		}
	}
}
