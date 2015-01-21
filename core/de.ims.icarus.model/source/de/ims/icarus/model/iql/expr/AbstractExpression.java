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
public abstract class AbstractExpression implements Expression, Computable, Cloneable {

	private Namespace namespace;
	private boolean computed = false;

	/**
	 * @see de.ims.icarus.model.iql.expr.NamespaceMember#getNamespace()
	 */
	@Override
	public Namespace getNamespace() {
		return namespace;
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Value#value()
	 */
	@Override
	public Object value() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Value#intValue()
	 */
	@Override
	public int intValue() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Value#longValue()
	 */
	@Override
	public long longValue() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Value#floatValue()
	 */
	@Override
	public float floatValue() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Value#doubleValue()
	 */
	@Override
	public double doubleValue() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Value#booleanValue()
	 */
	@Override
	public boolean booleanValue() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Expression#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public Expression clone() {
		Object result = null;
		try {
			result = super.clone();
		} catch(CloneNotSupportedException e) {
			// never going to happen
		}

		return (Expression) result;
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Computable#isComputed()
	 */
	@Override
	public boolean isComputed() {
		return computed;
	}

	/**
	 * @see de.ims.icarus.model.iql.expr.Computable#compute()
	 */
	@Override
	public void compute() {
		computed = false;

		doCompute();

		computed = true;
	}

	protected abstract void doCompute() throws IllegalStateException;
}
