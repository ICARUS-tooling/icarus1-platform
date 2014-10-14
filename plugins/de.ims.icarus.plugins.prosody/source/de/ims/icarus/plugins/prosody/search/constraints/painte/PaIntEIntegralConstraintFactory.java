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
package de.ims.icarus.plugins.prosody.search.constraints.painte;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEIntegralConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "painteIntegral"; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.prosody.search.painteIntegral"; //$NON-NLS-1$

	public PaIntEIntegralConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteIntegral.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteIntegral.description"); //$NON-NLS-1$
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return Double.class;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return DefaultSearchOperator.numerical();
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return LanguageConstants.DATA_UNDEFINED_DOUBLE_VALUE;
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return LanguageUtils.parseDoubleLabel((String)label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return LanguageUtils.getLabel((double)value);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new PaIntEIntegralConstraint(value, operator, specifier);
	}

	private static class PaIntEIntegralConstraint extends BoundedSyllableConstraint {

		private static final long serialVersionUID = -284378544802853009L;

		public PaIntEIntegralConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		@Override
		protected String getConfigPath() {
			return CONFIG_PATH;
		}

		@Override
		public SearchConstraint clone() {
			return new PaIntEIntegralConstraint(getValue(), getOperator(), getSpecifier());
		}

		private final PaIntEParams params = new PaIntEParams();

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint#getInstance(de.ims.icarus.plugins.prosody.search.ProsodyTargetTree, int)
		 */
		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {

			params.setParams(tree.getSource(), tree.getNodeIndex(), syllable);

			return PaIntEUtils.calcIntegral(leftBorder, rightBorder, params);
		}
	}
}
