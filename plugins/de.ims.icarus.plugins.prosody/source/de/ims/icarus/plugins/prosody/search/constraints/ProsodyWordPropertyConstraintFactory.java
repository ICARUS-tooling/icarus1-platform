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

 * $Revision: 270 $
 * $Date: 2014-07-08 13:44:07 +0200 (Di, 08 Jul 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.coref/source/de/ims/icarus/plugins/coref/search/constraints/CoreferenceSentencePropertyConstraintFactory.java $
 *
 * $LastChangedDate: 2014-07-08 13:44:07 +0200 (Di, 08 Jul 2014) $
 * $LastChangedRevision: 270 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.prosody.search.constraints;

import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultCaseInsensitiveConstraint;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: CoreferenceSentencePropertyConstraintFactory.java 270 2014-07-08 11:44:07Z mcgaerty $
 *
 */
public class ProsodyWordPropertyConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "wordProperty"; //$NON-NLS-1$

	public ProsodyWordPropertyConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.wordProperty.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.wordProperty.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return ProsodyUtils.getDefaultWordPropertyKeys();
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new ProsodyWordPropertyConstraint(value, operator, specifier);
		else
			return new ProsodyWordPropertyIConstraint(value, operator, specifier);
	}

	private static class ProsodyWordPropertyConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -2520716674648713610L;

		public ProsodyWordPropertyConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			return ((ProsodyTargetTree)value).getProperty(getKey());
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodyWordPropertyConstraint(getValue(), getOperator(), getSpecifier());
		}
	}

	private static class ProsodyWordPropertyIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = -9209332012676323076L;

		public ProsodyWordPropertyIConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			Object p = ((ProsodyTargetTree)value).getProperty(getKey());
			return p==null ? null : p.toString().toLowerCase();
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodyWordPropertyIConstraint(getValue(), getOperator(), getSpecifier());
		}
	}
}
