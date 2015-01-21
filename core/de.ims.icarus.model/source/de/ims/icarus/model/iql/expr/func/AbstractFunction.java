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
package de.ims.icarus.model.iql.expr.func;

import de.ims.icarus.model.iql.expr.AbstractExpression;
import de.ims.icarus.model.iql.expr.Expression;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractFunction extends AbstractExpression implements Function {

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, int)
	 */
	@Override
	public Function setParam(int index, int value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, long)
	 */
	@Override
	public Function setParam(int index, long value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, short)
	 */
	@Override
	public Function setParam(int index, short value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, float)
	 */
	@Override
	public Function setParam(int index, float value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, double)
	 */
	@Override
	public Function setParam(int index, double value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, boolean)
	 */
	@Override
	public Function setParam(int index, boolean value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, de.ims.icarus.model.iql.expr.Expression)
	 */
	@Override
	public Function setParam(int index, Expression value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, java.lang.Object)
	 */
	@Override
	public Function setParam(int index, Object value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.func.Function#setParam(int, java.lang.String)
	 */
	@Override
	public Function setParam(int index, String value) {
		throw new UnsupportedOperationException();
	}
}
