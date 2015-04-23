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
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyRelationConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "relation"; //$NON-NLS-1$

	public DependencyRelationConstraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.relation.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.relation.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new DependencyRelationConstraint(value, operator);
		else
			return new DependencyRelationCIConstraint(value, operator);
	}

	private static class DependencyRelationConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 1716609613318759367L;

		public DependencyRelationConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getRelation();
		}

		@Override
		public DependencyRelationConstraint clone() {
			return (DependencyRelationConstraint) super.clone();
		}
	}

	private static class DependencyRelationCIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = -3611860983057645172L;

		public DependencyRelationCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getRelation().toLowerCase();
		}

		@Override
		public DependencyRelationCIConstraint clone() {
			return (DependencyRelationCIConstraint) super.clone();
		}
	}
}
