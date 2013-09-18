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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.search.constraints;

import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.plugins.coref.search.DocumentTargetTree;
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
public class CoreferenceHeadPropertyConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "headProperty"; //$NON-NLS-1$

	public CoreferenceHeadPropertyConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, 
				"plugins.coref.constraints.headProperty.name",  //$NON-NLS-1$
				"plugins.coref.constraints.headProperty.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return CoreferenceUtils.getDefaultSentencePropertyKeys();
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new CoreferenceHeadPropertyConstraint(value, operator, specifier);
		else
			return new CoreferenceHeadPropertyIConstraint(value, operator, specifier);
	}

	private static class CoreferenceHeadPropertyConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -8085635133182844200L;

		public CoreferenceHeadPropertyConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}
		
		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			return ((DocumentTargetTree)value).getHeadProperty(getKey());
		}

		@Override
		public SearchConstraint clone() {
			return new CoreferenceHeadPropertyConstraint(getValue(), getOperator(), getSpecifier());
		}
	}

	private static class CoreferenceHeadPropertyIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = 8450749706605254456L;

		public CoreferenceHeadPropertyIConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}
		
		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			return ((DocumentTargetTree)value).getHeadProperty(getKey()).toString().toLowerCase();
		}

		@Override
		public SearchConstraint clone() {
			return new CoreferenceHeadPropertyIConstraint(getValue(), getOperator(), getSpecifier());
		}
	}
}
