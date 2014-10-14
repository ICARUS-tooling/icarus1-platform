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
public class PaIntEDistanceConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "painteDistance"; //$NON-NLS-1$

	public PaIntEDistanceConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteDistance.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteDistance.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return new Object[0];
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
		return new PaIntEDistanceConstraint(value, operator, specifier);
	}

	private static class PaIntEDistanceConstraint extends AbstractParameterizedPaIntEConstraint {

		private static final long serialVersionUID = 6887748634037055630L;

		public PaIntEDistanceConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		@Override
		public SearchConstraint clone() {
			return new PaIntEDistanceConstraint(getValue(), getOperator(), getSpecifier());
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.painte.BoundedSyllableConstraint#getConfigPath()
		 */
		@Override
		protected String getConfigPath() {
			return null;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint#getInstance(de.ims.icarus.plugins.prosody.search.ProsodyTargetTree, int)
		 */
		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {

			valueParams.setParams(tree.getSource(), tree.getNodeIndex(), syllable);

			/*
			 * dist = ||a-b||2 = sqrt(sum((a1-b1)²...(an-bn)²))
			 */

			double sum = 0;

			// A1
			if(specifierParams.isA1Active()) {
				sum += Math.pow((specifierParams.getA1()-valueParams.getA1()), 2.0);
			}

			// A2
			if(specifierParams.isA2Active()) {
				sum += Math.pow((specifierParams.getA2()-valueParams.getA2()), 2.0);
			}

			// B
			if(specifierParams.isBActive()) {
				sum += Math.pow((specifierParams.getB()-valueParams.getB()), 2.0);
			}

			// C1
			if(specifierParams.isC1Active()) {
				sum += Math.pow((specifierParams.getC1()-valueParams.getC1()), 2.0);
			}

			// C2
			if(specifierParams.isC2Active()) {
				sum += Math.pow((specifierParams.getC2()-valueParams.getC2()), 2.0);
			}

			// D
			if(specifierParams.isDActive()) {
				sum += Math.pow((specifierParams.getD()-valueParams.getD()), 2.0);
			}

			// Alignment
			if(specifierParams.isAlignmentActive()) {
				sum += Math.pow((specifierParams.getAlignment()-valueParams.getAlignment()), 2.0);
			}

			return Math.sqrt(sum);
		}
	}

}
