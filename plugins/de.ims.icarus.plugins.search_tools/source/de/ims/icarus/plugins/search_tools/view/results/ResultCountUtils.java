/*
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
