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
 * $Revision: 462 $
 * $Date: 2016-06-15 22:11:19 +0200 (Mi, 15 Jun 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.search_tools/source/de/ims/icarus/search_tools/constraints/CoreferenceVirtualNodeConstraintFactory.java $
 *
 * $LastChangedDate: 2016-06-15 22:11:19 +0200 (Mi, 15 Jun 2016) $
 * $LastChangedRevision: 462 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.coref.search.constraints;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.plugins.coref.search.DocumentTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: CoreferenceVirtualNodeConstraintFactory.java 462 2016-06-15 20:11:19Z mcgaerty $
 *
 */
public class CoreferenceVirtualNodeConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "virtualNode"; //$NON-NLS-1$

	public CoreferenceVirtualNodeConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.coref.constraints.virtualNode.name",  //$NON-NLS-1$
				"plugins.coref.constraints.virtualNode.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new VirtualNodeConstraint(value, operator);
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
		return LanguageUtils.parseBooleanLabel((String)label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return LanguageUtils.getBooleanLabel((int)value);
	}

	@Override
	public Object[] getLabelSet(Object specifier) {
		return new Object[]{
				LanguageConstants.DATA_UNDEFINED_LABEL,
				LanguageUtils.getBooleanLabel(LanguageConstants.DATA_YES_VALUE),
				LanguageUtils.getBooleanLabel(LanguageConstants.DATA_NO_VALUE),
		};
	}

	private static class VirtualNodeConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -6584941551770965755L;

		public VirtualNodeConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DocumentTargetTree)value).isVirtual();
		}

		@Override
		protected Object getConstraint() {
			return getValue().equals(LanguageConstants.DATA_YES_VALUE);
		}

		@Override
		public SearchConstraint clone() {
			return new VirtualNodeConstraint(getValue(), getOperator());
		}
	}
}
