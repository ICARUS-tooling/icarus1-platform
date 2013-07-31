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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools.view.results;

import java.awt.Color;

import de.ims.icarus.util.Exceptions;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ResultCountUtils {
	
	private static double minPercentage = 0.01;
	private static double highlightPercentage = 10.0;
	private static Color highlightColor = Color.blue;

	private ResultCountUtils() {
		// no-op
	}

	/**
	 * @return the minPercentage
	 */
	public static double getMinPercentage() {
		return minPercentage;
	}

	/**
	 * @param minPercentage the minPercentage to set
	 */
	public static void setMinPercentage(double value) {
		minPercentage = value;
	}

	/**
	 * @return the highlightPercentage
	 */
	public static double getHighlightPercentage() {
		return highlightPercentage;
	}

	/**
	 * @param highlightPercentage the highlightPercentage to set
	 */
	public static void setHighlightPercentage(double value) {
		highlightPercentage = value;
	}

	/**
	 * @return the highlightColor
	 */
	public static Color getHighlightColor() {
		return highlightColor;
	}

	/**
	 * @param highlightColor the highlightColor to set
	 */
	public static void setHighlightColor(Color value) {
		Exceptions.testNullArgument(value, "value"); //$NON-NLS-1$
		
		highlightColor = value;
	}
}
