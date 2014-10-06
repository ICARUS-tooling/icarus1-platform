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


public class PaIntEIntervalOperator implements PaIntEOperator {

	private final double leftBorder, rightBorder, stepSize;
	private final NumberOperator operator;

	public PaIntEIntervalOperator(double leftBorder, double rightBorder, int resolution, NumberOperator operator) {
		this.leftBorder = leftBorder;
		this.rightBorder = rightBorder;
		this.operator = operator;

		stepSize = (rightBorder-leftBorder)/resolution;
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.painte.PaIntEOperator#apply(de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams)
	 */
	@Override
	public boolean apply(PaIntEConstraintParams target, PaIntEConstraintParams constraints) {
		double x = leftBorder;

		while(x<=rightBorder) {
			double yTarget = PaIntEUtils.calcY(x, target);
			double yConstraint = PaIntEUtils.calcY(x, constraints);

			if(!operator.apply(yTarget, yConstraint)) {
				return false;
			}

			x += stepSize;
		}

		return true;
	}


}