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
import de.ims.icarus.plugins.prosody.ui.geom.Axis.Integer;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.search_tools.tree.TargetTree;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyRelativeWordPositionConstraintFactory extends AbstractConstraintFactory implements LanguageConstants {

	public static final String TOKEN = "section"; //$NON-NLS-1$

	public DependencyRelativeWordPositionConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.languageTools.constraints.section.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.section.description"); //$NON-NLS-1$
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return Integer.class;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return DefaultSearchOperator.numerical();
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return LanguageUtils.parseIntegerLabel((String) label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return LanguageUtils.getLabel((int)value);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new DependencyRelativeWordPositionConstraint(value, operator);
	}

	private static class DependencyRelativeWordPositionConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -4951579070208503138L;

		public DependencyRelativeWordPositionConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			TargetTree tree = (TargetTree)value;
//			System.out.println((int) ((tree.getNodeIndex()+1)/(double)tree.size() * 100.0));
			return (int) ((1+tree.getNodeIndex())/(double)tree.size() * 100.0);
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyRelativeWordPositionConstraint(getValue(), getOperator());
		}
	}
}
