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
package de.ims.icarus.model.iql.expr;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExpressionUtils {

	public static boolean isStringResult(Expression expression) {
		return expression.getResultType()==String.class;
	}

	public static boolean isObejctResult(Expression expression) {
		return expression.getResultType()==Object.class;
	}

	public static boolean isIntegerResult(Expression expression) {
		return expression.getResultType()==int.class;
	}

	public static boolean isLongResult(Expression expression) {
		return expression.getResultType()==long.class;
	}

	public static boolean isFloatResult(Expression expression) {
		return expression.getResultType()==float.class;
	}

	public static boolean isDoubleResult(Expression expression) {
		return expression.getResultType()==double.class;
	}

	public static boolean isBooleanResult(Expression expression) {
		return expression.getResultType()==boolean.class;
	}

	public static boolean isByteResult(Expression expression) {
		return expression.getResultType()==char.class;
	}

	public static boolean isCharacterResult(Expression expression) {
		return expression.getResultType()==byte.class;
	}

	public static boolean isComparableResult(Expression expression) {
		return Comparable.class.isAssignableFrom(expression.getResultType());
	}
}
