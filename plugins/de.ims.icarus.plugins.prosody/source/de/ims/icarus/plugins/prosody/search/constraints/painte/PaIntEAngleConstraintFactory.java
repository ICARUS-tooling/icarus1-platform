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
public class PaIntEAngleConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "painteAngle"; //$NON-NLS-1$

	public PaIntEAngleConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteAngle.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteAngle.description"); //$NON-NLS-1$
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
		double val = LanguageUtils.parseDoubleLabel((String)label);
		if(val>=180.0) {
			val %= 180.0;
		}
		return val;
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
		return new PaIntEAngleConstraint(value, operator, specifier);
	}

	private static class PaIntEAngleConstraint extends AbstractParameterizedPaIntEConstraint {

		private static final long serialVersionUID = 5588616829782787261L;

		public PaIntEAngleConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		@Override
		public SearchConstraint clone() {
			return new PaIntEAngleConstraint(getValue(), getOperator(), getSpecifier());
		}

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
			 * cos(y) = (a*b) / (|a|*|b|)
			 */

			double scal = 0;
			double quantA = 0;
			double quantB = 0;

			// A1
			if(specifierParams.isA1Active()) {
				scal += (specifierParams.getA1()*valueParams.getA1());
				quantA += specifierParams.getA1()*specifierParams.getA1();
				quantB += valueParams.getA1()*valueParams.getA1();
			}

			// A2
			if(specifierParams.isA2Active()) {
				scal += (specifierParams.getA2()*valueParams.getA2());
				quantA += specifierParams.getA2()*specifierParams.getA2();
				quantB += valueParams.getA2()*valueParams.getA2();
			}

			// B
			if(specifierParams.isBActive()) {
				scal += (specifierParams.getB()*valueParams.getB());
				quantA += specifierParams.getB()*specifierParams.getB();
				quantB += valueParams.getB()*valueParams.getB();
			}

			// C1
			if(specifierParams.isC1Active()) {
				scal += (specifierParams.getC1()*valueParams.getC1());
				quantA += specifierParams.getC1()*specifierParams.getC1();
				quantB += valueParams.getC1()*valueParams.getC1();
			}

			// C2
			if(specifierParams.isC2Active()) {
				scal += (specifierParams.getC2()*valueParams.getC2());
				quantA += specifierParams.getC2()*specifierParams.getC2();
				quantB += valueParams.getC2()*valueParams.getC2();
			}

			// D
			if(specifierParams.isDActive()) {
				scal += (specifierParams.getD()*valueParams.getD());
				quantA += specifierParams.getD()*specifierParams.getD();
				quantB += valueParams.getD()*valueParams.getD();
			}

			// Alignment
			if(specifierParams.isAlignmentActive()) {
				scal += (specifierParams.getAlignment()*valueParams.getAlignment());
				quantA += specifierParams.getAlignment()*specifierParams.getAlignment();
				quantB += valueParams.getAlignment()*valueParams.getAlignment();
			}

			quantA = Math.sqrt(quantA);
			quantB = Math.sqrt(quantB);

			return Math.acos(scal/(quantA*quantB))/Math.PI*180;
		}
	}

}
