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

 * $Revision: 389 $
 * $Date: 2015-04-23 12:19:15 +0200 (Do, 23 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.prosody/source/de/ims/icarus/plugins/prosody/search/constraints/WordPropertyConstraintFactory.java $
 *
 * $LastChangedDate: 2015-04-23 12:19:15 +0200 (Do, 23 Apr 2015) $
 * $LastChangedRevision: 389 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.search_tools.constraints;

import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultCaseInsensitiveConstraint;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.tree.AbstractSentenceTargetTree;
import de.ims.icarus.search_tools.util.SharedPropertyRegistry;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: WordPropertyConstraintFactory.java 389 2015-04-23 10:19:15Z mcgaerty $
 *
 */
public class SentencePropertyConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "sentenceProperty"; //$NON-NLS-1$

	public SentencePropertyConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.languageTools.constraints.sentenceProperty.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.sentenceProperty.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractConstraintFactory#getSupportedSpecifiers()
	 */
	@Override
	public Object[] getSupportedSpecifiers() {
		return SharedPropertyRegistry.getSpecifiers(SharedPropertyRegistry.SENTENCE_LEVEL);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new SentencePropertyConstraint(value, operator, specifier);
		else
			return new SentencePropertyIConstraint(value, operator, specifier);
	}

	private static class SentencePropertyConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 426884206361332385L;

		public SentencePropertyConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			return ((AbstractSentenceTargetTree<?>)value).getSource().getProperty(getKey());
		}

		@Override
		public SearchConstraint clone() {
			return new SentencePropertyConstraint(getValue(), getOperator(), getSpecifier());
		}
	}

	private static class SentencePropertyIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = -6552729430469568381L;

		public SentencePropertyIConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			Object p = ((AbstractSentenceTargetTree<?>)value).getSource().getProperty(getKey());
			return p==null ? null : p.toString().toLowerCase();
		}

		@Override
		public SentencePropertyIConstraint clone() {
			return new SentencePropertyIConstraint(getValue(), getOperator(), getSpecifier());
		}
	}
}
