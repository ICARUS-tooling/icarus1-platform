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
package de.ims.icarus.plugins.prosody.painte;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import de.ims.icarus.plugins.prosody.painte.PaIntEOperator.NumberOperator;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEUtils {

	/*
	 * Painte formula:
	 * d - (c_1/(1+E^(-a_1*(b-x)+g))) - (c_2/(1+E^(-a_2*(x-b)+g)))
	 * d - (c_1/(1+exp(-a_1*(b-x)+g))) - (c_2/(1+exp(-a_2*(x-b)+g)))
	 *
	 * Equation:
	 * d_1 - (c_11/(1+exp(-a_11*(b_1-x)+g_1))) - (c_12/(1+exp(-a_12*(x-b_1)+g_1))) = d_2 - (c_21/(1+exp(-a_21*(b_2-x)+g_2))) - (c_22/(1+exp(-a_22*(x-b_2)+g_2)))
	 *
	 * For mathematica:
	 * d_1 = d
	 * c_11 = c
	 * c_12 = e
	 * a_11 = a
	 * a_12 = f
	 * b_1 = b
	 * g_1 = g
	 *
	 * d_2 = h
	 * c_21 = i
	 * c_22 = j
	 * a_21 = k
	 * a_22 = l
	 * b_2 = m
	 * g_2 = n
	 *
	 * d - (c/(1+E^(-a*(b-x)+g))) - (e/(1+E^(-f*(x-b)+g)))
	 * h - (i/(1+E^(-k*(m-x)+n))) - (j/(1+E^(-l*(x-m)+n)))
	 *
	 * Painte integral formula:
	 * ((c1*log(E^(-a1*b+a1*x)+1))/a1) - ((c2*log(-E^(a2*b+g)-E^(a2*x)))/a2) + (x*d-c1)
	 *
	 * Integrate[d - c1/(1 + E^(-(a1*(b - x)) + g)) - c2/(1 + E^(-(a2*(x - b)) + g)), x]
	 * 			==
	 * 			(-c1 + d)*x - (c2*Log[-E^(a2*b + g) - E^(a2*x)])/a2 + (c1*Log[1 + E^(-(a1*b) + g + a1*x)])/a1
	 *
	 * Integtral via Maxima:
	 *
	 * 			-c1*(-log(exp(a1*x+g-a1*b)+1)/a1 + x + g/a1 - b) - c2*log(exp(a2*x)+exp(g+a2*b))/a2 + d*x
	 *
	 */

	private static double exp(double x) {
		return Math.exp(x);
	}

	private static double log(double x) {
		return x<0 ? Math.log(-x) : Math.log(x);
	}

	public static double calcY(double x, PaIntEParams params) {
		return calcY(x, params.a1, params.a2, params.b, params.c1, params.c2, params.d, params.alignment);
	}

	public static double calcY(double x, PaIntEConstraintParams constraints) {
		return calcY(x, constraints.a1, constraints.a2, constraints.b,
				constraints.c1, constraints.c2, constraints.d, PaIntEParams.DEFAULT_ALIGNMENT);
	}

	public static double calcY(double x, double a1, double a2, double b, double c1, double c2, double d, double alignment) {
		return d - (c1/(1+exp(-a1*(b-x)+alignment))) - (c2/(1+exp(-a2*(x-b)+alignment)));
	}

	public static double calcIntegral(double x, double a1, double a2, double b, double c1, double c2, double d, double alignment) {
//		String s ="(-c1 + d)*x - (c2*Log[-E^(a2*b + m) - E^(a2*x)])/a2 + (c1*Log[1 + E^(-(a1*b) + m + a1*x)])/a1";
//		String s ="-c1*(-log(exp(a1*x+m-a1*b)+1)/a1 + x + m/a1 - b) - c2*log(exp(a2*x)+exp(m+a2*b))/a2 + d*x";
////		s = s.replace("x", String.valueOf(x));
//		s = s.replace("a1", String.format(Locale.ENGLISH, "%.02f", a1));
//		s = s.replace("a2", String.format(Locale.ENGLISH, "%.02f", a2));
//		s = s.replace("b", String.format(Locale.ENGLISH, "%.02f", b));
//		s = s.replace("c1", String.format(Locale.ENGLISH, "%.02f", c1));
//		s = s.replace("c2", String.format(Locale.ENGLISH, "%.02f", c2));
//		s = s.replace("d", String.format(Locale.ENGLISH, "%.02f", d));
//		s = s.replace("m", String.format(Locale.ENGLISH, "%.02f", alignment));
//		System.out.println(s);

//		return ((c1*Math.log(Math.exp(-a1*b+a1*x+alignment)))/a1)
//				- ((c2*Math.log(-Math.exp(a2*b+alignment)-Math.exp(a2*x)))/a2)
//				+ (x*d-c1);
//		return -c1*(-log(exp(a1*x+g-a1*b)+1)/a1 + x + g/a1 - b) - c2*log(exp(a2*x)+exp(g+a2*b))/a2 + d*x;

		double p1 = -c1*(-log(exp(a1*x+alignment-a1*b)+1)/a1 + x + alignment/a1 - b);
		double p2 = - c2*log(exp(a2*x)+exp(alignment+a2*b))/a2;
		double p3 = d*x;

		return p1 - p2 + p3;
	}

	public static double calcIntegral(double x, PaIntEParams params) {
		return calcIntegral(x, params.a1, params.a2, params.b, params.c1, params.c2, params.d, params.alignment);
	}

	public static double calcIntegral(double x, PaIntEConstraintParams constraints) {
		return calcY(x, constraints.a1, constraints.a2, constraints.b,
				constraints.c1, constraints.c2, constraints.d, PaIntEParams.DEFAULT_ALIGNMENT);
	}

	public static double calcIntegral(double x1, double x2, double a1, double a2, double b, double c1, double c2, double d, double alignment) {
		double y1 = calcIntegral(x2, a1, a2, b, c1, c2, d, alignment);
		double y2 = calcIntegral(x2, a1, a2, b, c1, c2, d, alignment);

		return y2-y1;
	}

	public static double calcIntegral(double x1, double x2, PaIntEParams params) {
		double y1 = calcIntegral(x1, params);
		double y2 = calcIntegral(x2, params);

		return y2-y1;
	}

	public static double calcIntegral(double x1, double x2, PaIntEConstraintParams constraints) {
		double y1 = calcIntegral(x1, constraints);
		double y2 = calcIntegral(x2, constraints);

		return y2-y1;
	}

	public static NumberOperator getNumberOperator(SearchOperator operator) {
		if(operator==DefaultSearchOperator.GREATER_THAN) {
			return greaterThan;
		} else if(operator==DefaultSearchOperator.GREATER_OR_EQUAL) {
			return greaterOrEqual;
		} else if(operator==DefaultSearchOperator.LESS_THAN) {
			return lessThan;
		} else if(operator==DefaultSearchOperator.LESS_OR_EQUAL) {
			return lessOrEqual;
		} else
			throw new IllegalArgumentException("Cannot translate search operator into numerical equivalent: "+operator.getName()); //$NON-NLS-1$
	}

	private static void copy(String data) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard==null) {
			return;
		}

		StringSelection ss = new StringSelection(data);

		clipboard.setContents(ss, ss);
	}

	public static void copyParams(PaIntEParams params) {
		copy(params.toString());
	}

	public static void copyConstraints(PaIntEConstraintParams constraints) {
		copyParams(constraints);
	}

	public static void copyWrapper(PaIntEParamsWrapper wrapper) {
		copyParams(wrapper.getParams());
	}

	public static void copyWrapperId(PaIntEParamsWrapper wrapper) {
		copy("$"+wrapper.getName()); //$NON-NLS-1$
	}

	public static final NumberOperator lessThan = new NumberOperator() {

		@Override
		public boolean apply(double d1, double d2) {
			return d1<d2;
		}
	};

	public static final NumberOperator lessOrEqual = new NumberOperator() {

		@Override
		public boolean apply(double d1, double d2) {
			return d1<=d2;
		}
	};

	public static final NumberOperator greaterThan = new NumberOperator() {

		@Override
		public boolean apply(double d1, double d2) {
			return d1>d2;
		}
	};

	public static final NumberOperator greaterOrEqual = new NumberOperator() {

		@Override
		public boolean apply(double d1, double d2) {
			return d1>=d2;
		}
	};

	public static class GreaterThan implements PaIntEOperator {

		@Override
		public boolean apply(PaIntEConstraintParams target, PaIntEConstraintParams constraints) {
			if(constraints.isDActive() && target.getD()<=constraints.getD()) {
				return false;
			}
			if(constraints.isC1Active() && target.getC1()>=constraints.getC1()) {
				return false;
			}
			if(constraints.isC2Active() && target.getC1()>=constraints.getC2()) {
				return false;
			}

			return true;
		}

	};

	public static class GreaterOrEqual implements PaIntEOperator {

		@Override
		public boolean apply(PaIntEConstraintParams target, PaIntEConstraintParams constraints) {
			if(constraints.isDActive() && target.getD()<constraints.getD()) {
				return false;
			}
			if(constraints.isC1Active() && target.getC1()>constraints.getC1()) {
				return false;
			}
			if(constraints.isC2Active() && target.getC1()>constraints.getC2()) {
				return false;
			}

			return true;
		}

	};

	public static class LessThan implements PaIntEOperator {

		@Override
		public boolean apply(PaIntEConstraintParams target, PaIntEConstraintParams constraints) {
			if(constraints.isDActive() && target.getD()>=constraints.getD()) {
				return false;
			}
			if(constraints.isC1Active() && target.getC1()<=constraints.getC1()) {
				return false;
			}
			if(constraints.isC2Active() && target.getC1()<=constraints.getC2()) {
				return false;
			}

			return true;
		}

	};

	public static class LessOrEqual implements PaIntEOperator {

		@Override
		public boolean apply(PaIntEConstraintParams target, PaIntEConstraintParams constraints) {
			if(constraints.isDActive() && target.getD()>constraints.getD()) {
				return false;
			}
			if(constraints.isC1Active() && target.getC1()<constraints.getC1()) {
				return false;
			}
			if(constraints.isC2Active() && target.getC1()<constraints.getC2()) {
				return false;
			}

			return true;
		}

	};

	public static class InsideRange implements PaIntEOperator {

		private final PaIntEConstraintParams delta;

		public InsideRange(PaIntEConstraintParams delta) {
			this.delta = delta;
		}

		@Override
		public boolean apply(PaIntEConstraintParams target, PaIntEConstraintParams constraints) {
			if(constraints.isA1Active() && Math.abs(target.getA1()-constraints.getA1())>delta.getA1()) {
				return false;
			}
			if(constraints.isA2Active() && Math.abs(target.getA2()-constraints.getA2())>delta.getA2()) {
				return false;
			}
			if(constraints.isBActive() && Math.abs(target.getB()-constraints.getB())>delta.getB()) {
				return false;
			}
			if(constraints.isC1Active() && Math.abs(target.getC1()-constraints.getC1())>delta.getC1()) {
				return false;
			}
			if(constraints.isC2Active() && Math.abs(target.getC2()-constraints.getC2())>delta.getC2()) {
				return false;
			}
			if(constraints.isDActive() && Math.abs(target.getD()-constraints.getD())>delta.getD()) {
				return false;
			}

			return true;
		}

	};

	public static class OutsideRange implements PaIntEOperator {

		private final PaIntEConstraintParams delta;

		public OutsideRange(PaIntEConstraintParams delta) {
			this.delta = delta;
		}

		@Override
		public boolean apply(PaIntEConstraintParams target, PaIntEConstraintParams constraints) {
			if(constraints.isA1Active() && Math.abs(target.getA1()-constraints.getA1())<=delta.getA1()) {
				return false;
			}
			if(constraints.isA2Active() && Math.abs(target.getA2()-constraints.getA2())<=delta.getA2()) {
				return false;
			}
			if(constraints.isBActive() && Math.abs(target.getB()-constraints.getB())<=delta.getB()) {
				return false;
			}
			if(constraints.isC1Active() && Math.abs(target.getC1()-constraints.getC1())<=delta.getC1()) {
				return false;
			}
			if(constraints.isC2Active() && Math.abs(target.getC2()-constraints.getC2())<=delta.getC2()) {
				return false;
			}
			if(constraints.isDActive() && Math.abs(target.getD()-constraints.getD())<=delta.getD()) {
				return false;
			}

			return true;
		}

	};
}
