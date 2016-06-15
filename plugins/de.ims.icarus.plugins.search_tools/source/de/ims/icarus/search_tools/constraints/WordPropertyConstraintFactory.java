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
package de.ims.icarus.search_tools.constraints;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultCaseInsensitiveConstraint;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.tree.TargetTree;
import de.ims.icarus.search_tools.util.SharedPropertyRegistry;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class WordPropertyConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "wordProperty"; //$NON-NLS-1$

	public WordPropertyConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.languageTools.constraints.wordProperty.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.wordProperty.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractConstraintFactory#getSupportedSpecifiers()
	 */
	@Override
	public Object[] getSupportedSpecifiers() {
		return SharedPropertyRegistry.getSpecifiers(SharedPropertyRegistry.WORD_LEVEL,
				SharedPropertyRegistry.INCLUDE_COMPATIBLE_TYPES | SharedPropertyRegistry.INCLUDE_GENERAL_LEVEL,
				LanguageUtils.getSentenceDataContentType());
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new WordPropertyConstraint(value, operator, specifier);
		else
			return new WordPropertyIConstraint(value, operator, specifier);
	}

	private static class WordPropertyConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -2520716674648713610L;

		public WordPropertyConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getProperty(getKey());
		}

		@Override
		public SearchConstraint clone() {
			return new WordPropertyConstraint(getValue(), getOperator(), getSpecifier());
		}
	}

	private static class WordPropertyIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = -9209332012676323076L;

		public WordPropertyIConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			Object p = ((TargetTree)value).getProperty(getKey());
			return p==null ? null : p.toString().toLowerCase();
		}

		@Override
		public WordPropertyIConstraint clone() {
			return new WordPropertyIConstraint(getValue(), getOperator(), getSpecifier());
		}
	}
}
