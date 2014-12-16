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
package de.ims.icarus.plugins.prosody.search.constraints;

import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodySyllablePropertyConstraintFactory extends AbstractConstraintFactory implements ProsodyConstants {

	public static final String TOKEN = "syllableProperty"; //$NON-NLS-1$

	public ProsodySyllablePropertyConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.syllableProperty.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.syllableProperty.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return ProsodyUtils.getDefaultSyllablePropertyKeys();
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new ProsodySyllablePropertyConstraint(value, operator, specifier);
		else
			return new ProsodySyllablePropertyCIConstraint(value, operator, specifier);
	}

	private static class ProsodySyllablePropertyConstraint extends AbstractProsodySyllableConstraint {

		private static final long serialVersionUID = -2250947975211835769L;

		public ProsodySyllablePropertyConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {
			return tree.getProperty(getKey(), syllable);
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodySyllablePropertyConstraint(getValue(), getOperator(), getSpecifier());
		}
	}

	private static class ProsodySyllablePropertyCIConstraint extends AbstractProsodySyllableCaseInsensitiveConstraint {

		private static final long serialVersionUID = 2216387895482510250L;

		public ProsodySyllablePropertyCIConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {
			return tree.getProperty(getKey(), syllable);
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodySyllablePropertyCIConstraint(getValue(), getOperator(), getSpecifier());
		}
	}
}
