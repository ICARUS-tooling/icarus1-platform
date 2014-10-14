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

import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEIntervalOperator;
import de.ims.icarus.plugins.prosody.painte.PaIntEOperator.NumberOperator;
import de.ims.icarus.plugins.prosody.painte.PaIntEUtils;
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
public class PaIntECurveConstraintFactory  extends AbstractConstraintFactory {

	public static final String TOKEN = "painteCurve"; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.prosody.search.painteCurve"; //$NON-NLS-1$

	public PaIntECurveConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteCurve.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteCurve.description"); //$NON-NLS-1$
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return DefaultSearchOperator.comparing();
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new PaIntECurveConstraint(value, operator, specifier);
	}

	private static class PaIntECurveConstraint extends AbstractPaIntEConstraint {

		private static final long serialVersionUID = 3777419413407098846L;

		private PaIntEIntervalOperator intervalOperator;

		public PaIntECurveConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		@Override
		public SearchConstraint clone() {
			return new PaIntECurveConstraint(getValue(), getOperator(), getSpecifier());
		}

		@Override
		protected boolean applyOperator(PaIntEConstraintParams target) {
			return intervalOperator.apply(target, constraintParams);
		}

		@Override
		public void setOperator(SearchOperator operator) {
			super.setOperator(operator);

			NumberOperator numberOperator = PaIntEUtils.getNumberOperator(operator);

			intervalOperator = new PaIntEIntervalOperator(leftBorder, rightBorder, resolution, numberOperator);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.painte.BoundedSyllableConstraint#getConfigPath()
		 */
		@Override
		protected String getConfigPath() {
			return CONFIG_PATH;
		}
	}
}
