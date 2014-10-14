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
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
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
public class PaIntEChannelConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "painteChannel"; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.prosody.search.painteChannel"; //$NON-NLS-1$

	public PaIntEChannelConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteChannel.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteChannel.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return new Object[0];
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return null;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[]{
				DefaultSearchOperator.EQUALS,
				DefaultSearchOperator.EQUALS_NOT,
				DefaultSearchOperator.GROUPING,
		};
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return ProsodyUtils.parsePaIntEChannelLabel((String)label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return ProsodyUtils.getPaIntEChannelLabel((int)value);
	}

	@Override
	public Object[] getLabelSet(Object specifier) {
		return new Object[]{
				LanguageConstants.DATA_UNDEFINED_LABEL,
				ProsodyConstants.PAINTE_CHANNEL_INSIDE_LABEL,
				ProsodyConstants.PAINTE_CHANNEL_BELOW_LABEL,
				ProsodyConstants.PAINTE_CHANNEL_ABOVE_LABEL,
				ProsodyConstants.PAINTE_CHANNEL_CROSSING_LABEL,
		};
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new PaIntEChannelConstraint(value, operator, specifier);
	}

	private static class PaIntEChannelConstraint extends BoundedSyllableConstraint implements PaIntEConstraint {

		private static final long serialVersionUID = 6887748634037055630L;

		private static final String LOWER_CURVE = "Lower PaIntE-constraint"; //$NON-NLS-1$

		private static final String UPPER_CURVE = "Upper PaIntE-constraint"; //$NON-NLS-1$

		protected final PaIntEConstraintParams valueParams = new PaIntEConstraintParams();
		protected PaIntEConstraintParams lowerParams;
		protected PaIntEConstraintParams upperParams;

		public PaIntEChannelConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		@Override
		public PaIntEConstraintParams[] getPaIntEConstraints() {
			return new PaIntEConstraintParams[]{lowerParams, upperParams};
		}

		@Override
		protected String getConfigPath() {
			return CONFIG_PATH;
		}

		@Override
		public void setSpecifier(Object specifier) {
			super.setSpecifier(specifier);

			String s = (String)specifier;
			if(s!=null && !LanguageConstants.DATA_UNDEFINED_LABEL.equals(s)) {
				if(lowerParams==null) {
					lowerParams = new PaIntEConstraintParams();
				}
				if(upperParams==null) {
					upperParams = new PaIntEConstraintParams();
				}

				parseConstraint(s, lowerParams, upperParams);

				lowerParams.checkParams(LOWER_CURVE);
				upperParams.checkParams(UPPER_CURVE);
			}
		}

		@Override
		public SearchConstraint clone() {
			return new PaIntEChannelConstraint(getValue(), getOperator(), getSpecifier());
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint#getInstance(de.ims.icarus.plugins.prosody.search.ProsodyTargetTree, int)
		 */
		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {

			valueParams.setParams(tree.getSource(), tree.getNodeIndex(), syllable);

			double stepSize = (rightBorder-leftBorder)/resolution;

			double x = leftBorder;

			CurveState state = CurveState.BLANK;
			CurveState next = null;

			while(x<=rightBorder) {
				double yTarget = PaIntEUtils.calcY(x, valueParams);
				double yUpper = PaIntEUtils.calcY(x, upperParams);
				double yLower = PaIntEUtils.calcY(x, lowerParams);

				next = state.compute(yUpper, yLower, yTarget);

//				System.out.printf("state=%s next=%s\n", state, next);

				if(next==null) {
					break;
				}

				state = next;
				x += stepSize;
			}

			return state.getResult();
		}
	}

	private enum CurveState {
		BLANK(LanguageConstants.DATA_UNDEFINED_VALUE) {
			@Override
			public CurveState compute(double upper, double lower, double value) {
				if(value>upper) {
					return ABOVE;
				} else if(value<lower) {
					return BELOW;
				} else {
					return INSIDE;
				}
			}
		},
		BELOW(ProsodyConstants.PAINTE_CHANNEL_BELOW_VALUE) {
			@Override
			public CurveState compute(double upper, double lower, double value) {
				if(value>=lower) {
					return CROSSING;
				} else {
					return this;
				}
			}
		},
		ABOVE(ProsodyConstants.PAINTE_CHANNEL_ABOVE_VALUE) {
			@Override
			public CurveState compute(double upper, double lower, double value) {
				if(value<=upper) {
					return CROSSING;
				} else {
					return this;
				}
			}
		},
		INSIDE(ProsodyConstants.PAINTE_CHANNEL_INSIDE_VALUE) {
			@Override
			public CurveState compute(double upper, double lower, double value) {
				if(value<lower || value>upper) {
					return CROSSING;
				} else {
					return this;
				}
			}
		},
		CROSSING(ProsodyConstants.PAINTE_CHANNEL_CROSSING_VALUE) {
			@Override
			public CurveState compute(double upper, double lower, double value) {
				// Final state, there is no further changing
				return null;
			}
		},

		;

		private final int result;

		private CurveState(int result) {
			this.result = result;
		}

		public int getResult() {
			return result;
		}

		public abstract CurveState compute(double upper, double lower, double value);
	}
}
