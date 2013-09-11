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
package de.ims.icarus.plugins.errormining.ngram_search;

import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.util.Options;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramTagConstraintFactory extends AbstractConstraintFactory {

	
	public static final String TOKEN = "tag"; //$NON-NLS-1$
	
	/**
	 * @param token
	 * @param type
	 * @param nameKey
	 * @param descriptionKey
	 */
	public NGramTagConstraintFactory() {
		//super(token, type, nameKey, descriptionKey)
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.errormining.constraints.tag.name",  //$NON-NLS-1$
				"plugins.errormining.constraints.tag.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new NGramTagConstraint(value, operator);
	}
	
	private static class NGramTagConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 284330345465175039L;

		public NGramTagConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			//return ((DependencyTargetTree)value).getForm();
			return null;
		}

		@Override
		public SearchConstraint clone() {
			return new NGramTagConstraint(getValue(), getOperator());
		}
	}

}
