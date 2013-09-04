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

import de.ims.icarus.language.dependency.search.DependencyTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultCaseInsensitiveConstraint;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyFeaturesConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "features"; //$NON-NLS-1$

	public DependencyFeaturesConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.features.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.features.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new DependencyFeaturesConstraint(value, operator);
		else
			return new DependencyFeaturesCIConstraint(value, operator);
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[] {
			DefaultSearchOperator.EQUALS,
			DefaultSearchOperator.EQUALS_NOT,
			DefaultSearchOperator.CONTAINS,
			DefaultSearchOperator.CONTAINS_NOT,
			DefaultSearchOperator.MATCHES,
			DefaultSearchOperator.MATCHES_NOT,
			DefaultSearchOperator.GROUPING,
		};
	}

	private static class DependencyFeaturesConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -3346450454270312183L;

		public DependencyFeaturesConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getFeatures();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyFeaturesConstraint(getValue(), getOperator());
		}
	}

	private static class DependencyFeaturesCIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = -3346450454270312183L;

		public DependencyFeaturesCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getFeatures().toLowerCase();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyFeaturesCIConstraint(getValue(), getOperator());
		}
	}
}
