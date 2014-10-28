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

import java.util.Arrays;
import java.util.Locale;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.ui.details.ProsodySentenceDetailPresenter;
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

		// TEST CONSTRAINTS: [painteChannel$"0.2|50|50|100;0.3|60|60|210"=Crossing]

		private static final long serialVersionUID = 6887748634037055630L;

		private static final String LOWER_CURVE = "Lower PaIntE-constraint"; //$NON-NLS-1$

		private static final String UPPER_CURVE = "Upper PaIntE-constraint"; //$NON-NLS-1$

		protected final PaIntEConstraintParams valueParams = new PaIntEConstraintParams();
		protected transient PaIntEConstraintParams lowerParams;
		protected transient PaIntEConstraintParams upperParams;

		protected transient boolean compact = false;
		protected transient CompactCurveBuffer curveBuffer;

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

		/**
		 * The new implementation of the {@link ProsodySentenceDetailPresenter} allows us to always
		 * consider boundaries when visualizing constraints.
		 *
		 * <b>Deprecated Info:</b> Note that the channel bounds are only provided to external facilities if the
		 * constraints used are <b>not</b> in compact mode! This is to not screw up
		 * the visualization of "real" curves by artificially limiting their range on
		 * the horizontal axis.
		 *
		 * @see de.ims.icarus.plugins.prosody.search.constraints.painte.PaIntEConstraint#hasBounds()
		 */
		@Override
		public boolean hasBounds() {
			return true;
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

				PaIntEConstraintParams.checkParams(lowerParams, LOWER_CURVE);
				PaIntEConstraintParams.checkParams(lowerParams, UPPER_CURVE);
			}
		}

		@Override
		public SearchConstraint clone() {
			return new PaIntEChannelConstraint(getValue(), getOperator(), getSpecifier());
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.DefaultConstraint#prepare()
		 */
		@Override
		public void prepare() {
			compact = lowerParams.isCompact() || upperParams.isCompact();

			if(compact && curveBuffer==null) {
				curveBuffer = new CompactCurveBuffer();
			}
		}

		private boolean isInside(double v) {
			return v>=leftBorder && v<=rightBorder;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint#getInstance(de.ims.icarus.plugins.prosody.search.ProsodyTargetTree, int)
		 */
		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {

			valueParams.setParams(tree.getSource(), tree.getNodeIndex(), syllable);

			if(compact) {

				if(!isInside(lowerParams.getB())
						|| !isInside(upperParams.getB())
						|| !isInside(valueParams.getB())) {
					return LanguageConstants.DATA_UNDEFINED_VALUE;
				}

				curveBuffer.fill(this);

				CurveState state = CurveState.BLANK;
				CurveState next = null;

				for(int i=0; i<curveBuffer.pointCount(); i++) {
					double yTarget = curveBuffer.pointYTarget(i);
					double yUpper = curveBuffer.pointYUpper(i);
					double yLower = curveBuffer.pointYLower(i);

					next = state.compute(yUpper, yLower, yTarget);

					if(next==null) {
						break;
					}

					state = next;
				}

				return state.getResult();
			} else {

				double stepSize = (rightBorder-leftBorder)/resolution;

				double x = leftBorder;

				CurveState state = CurveState.BLANK;
				CurveState next = null;

				while(x<=rightBorder) {
					double yTarget = PaIntEUtils.calcY(x, valueParams);
					double yUpper = PaIntEUtils.calcY(x, upperParams);
					double yLower = PaIntEUtils.calcY(x, lowerParams);

	//				if(verbose)
	//				System.out.printf("value=%.02f lower=%.02f upper=%.02f\n", yTarget, yLower, yUpper);

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

		@Override
		public Object getLabel(Object value) {
			return ProsodyUtils.getPaIntEChannelLabel((int)value);
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

	private static class CompactCurveBuffer {

		// 2 endpoints and 1 peek for each curve involved
		static final int pointCount = 5;

		final double[] pointsX = new double[pointCount];
		final double[] pointsYLower = new double[pointCount];
		final double[] pointsYUpper = new double[pointCount];
		final double[] pointsYTarget = new double[pointCount];

		final LineBuffer lowerLine = new LineBuffer();
		final LineBuffer upperLine = new LineBuffer();
		final LineBuffer targetLine = new LineBuffer();

		public void fill(PaIntEChannelConstraint constraint) {
			final double l = constraint.leftBorder;
			final double r = constraint.rightBorder;

			pointsX[0] = l;
			pointsX[1] = constraint.lowerParams.getB();
			pointsX[2] = constraint.upperParams.getB();
			pointsX[3] = constraint.valueParams.getB();
			pointsX[4] = r;

			Arrays.sort(pointsX);

			lowerLine.reset(l, r, constraint.lowerParams);
			upperLine.reset(l, r, constraint.upperParams);
			targetLine.reset(l, r, constraint.valueParams);

			for(int i=0; i<pointCount; i++) {
				double x = pointsX[i];

				pointsYLower[i] = lowerLine.getY(x);
				pointsYUpper[i] = upperLine.getY(x);
				pointsYTarget[i] = targetLine.getY(x);
			}
		}

		public int pointCount() {
			return pointCount;
		}

		public double pointX(int index) {
			return pointsX[index];
		}

		public double pointYLower(int index) {
			return pointsYLower[index];
		}

		public double pointYUpper(int index) {
			return pointsYUpper[index];
		}

		public double pointYTarget(int index) {
			return pointsYTarget[index];
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("Lower Line:\n").append(lowerLine).append('\n');
			sb.append("Target Line:\n").append(targetLine).append('\n');
			sb.append("Upper Line:\n").append(upperLine).append('\n');

			for(int i=0; i<pointCount; i++) {
				sb.append(String.format(Locale.ENGLISH,
						"x=%.02f lower=%.02f target=%,02f upper=%.02f",
						pointX(i), pointYLower(i), pointYTarget(i), pointYUpper(i))).append('\n');
			}

			return sb.toString();
		}
	}

	/**
	 * Models the 2 lines that describe a compact PaIntE set.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private static class LineBuffer {
		private double m1, m2; // slopes for left and right line
		private double c1, c2; // Points of the lines on the vertical axis
		private double d;

		private double b;

		public double getY(double x) {
			return x<b ? m1*x + c1 : (x>b ? m2*x+c2 : d);
		}

		public void reset(double l, double r, PaIntEConstraintParams params) {
			b = params.getB();
			d = params.getD();
			final double y1 = params.getD()-params.getC1();
			final double y2 = params.getD()-params.getC2();

			// Ascending line
			m1 = (d-y1) / (b-l); // always positive
			c1 = y1 - l*m1;

			// Descending line
			m2 = (y2-d) / (r-b); // always negative
			c2 = y2 - r*m2;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("b=").append(b).append(" d=").append(d).append('\n');
			sb.append("line_asc=").append(m1).append("x+").append(c1).append('\n');
			sb.append("line_desc=").append(m2).append("x+").append(c2);

			return sb.toString();
		}
	}
}
