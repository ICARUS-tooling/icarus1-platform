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
public class Constants {

	public static Expression constantInteger(Namespace namespace, int value) {
		return new NumberConstant(namespace, int.class, value);
	}

	public static Expression constantLong(Namespace namespace, long value) {
		return new NumberConstant(namespace, long.class, value);
	}

	public static Expression constantFloat(Namespace namespace, float value) {
		return new NumberConstant(namespace, float.class, value);
	}

	public static Expression constantDouble(Namespace namespace, double value) {
		return new NumberConstant(namespace, double.class, value);
	}

	public static Expression constantBoolean(Namespace namespace, boolean value) {
		return new BooleanConstant(namespace, value);
	}

	public static Expression constantString(Namespace namespace, CharSequence value) {
		return new StringConstant(namespace, value);
	}

	public static Expression constantObject(Namespace namespace, Object value) {
		return constantObject(namespace, Object.class, value);
	}

	public static Expression constantObject(Namespace namespace, Class<?> type, Object value) {
		if(type.isPrimitive())
			throw new IllegalArgumentException("Cannot use primitive class for object constant"); //$NON-NLS-1$

		return new ObjectConstant(namespace, type, value);
	}

	abstract static class AbstractConstant implements Expression, Cloneable {

		private final Namespace namespace;

		AbstractConstant(Namespace namespace) {
			if (namespace == null)
				throw new NullPointerException("Invalid namespace"); //$NON-NLS-1$

			this.namespace = namespace;
		}

		@Override
		public Expression clone() {

			Expression result = null;

			try {
				result = (Expression) super.clone();
			} catch (CloneNotSupportedException e) {
				// Not going to happen
			}

			return result;
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.NamespaceMember#getNamespace()
		 */
		@Override
		public Namespace getNamespace() {
			return namespace;
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Expression#isConstant()
		 */
		@Override
		public boolean isConstant() {
			return true;
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
	}

	static class NumberConstant extends AbstractConstant {
		private final Class<?> type;
		private final Number value;

		NumberConstant(Namespace namespace, Class<?> type, Number value) {
			super(namespace);

			if (type == null)
				throw new NullPointerException("Invalid type"); //$NON-NLS-1$
			if (value == null)
				throw new NullPointerException("Invalid value"); //$NON-NLS-1$

			this.type = type;
			this.value = value;
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Value#intValue()
		 */
		@Override
		public int intValue() {
			return value.intValue();
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Value#longValue()
		 */
		@Override
		public long longValue() {
			return value.longValue();
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Value#floatValue()
		 */
		@Override
		public float floatValue() {
			return value.floatValue();
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Value#doubleValue()
		 */
		@Override
		public double doubleValue() {
			return value.doubleValue();
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Expression#getResultType()
		 */
		@Override
		public Class<?> getResultType() {
			return type;
		}
	}

	static class BooleanConstant extends AbstractConstant {

		private final boolean value;

		BooleanConstant(Namespace namespace, boolean value) {
			super(namespace);

			this.value = value;
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Constants.AbstractConstant#booleanValue()
		 */
		@Override
		public boolean booleanValue() {
			return value;
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Expression#getResultType()
		 */
		@Override
		public Class<?> getResultType() {
			return boolean.class;
		}
	}

	static class StringConstant extends AbstractConstant {

		private final String value;

		StringConstant(Namespace namespace, CharSequence value) {
			super(namespace);

			if (value == null)
				throw new NullPointerException("Invalid value"); //$NON-NLS-1$

			this.value = value.toString();
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Expression#getResultType()
		 */
		@Override
		public Class<?> getResultType() {
			return String.class;
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Constants.AbstractConstant#value()
		 */
		@Override
		public Object value() {
			return value;
		}
	}

	static class ObjectConstant extends AbstractConstant {

		private final Object value;
		private final Class<?> type;

		ObjectConstant(Namespace namespace, Class<?> type, Object value) {
			super(namespace);

			if (type == null)
				throw new NullPointerException("Invalid type"); //$NON-NLS-1$
			if (value == null)
				throw new NullPointerException("Invalid value"); //$NON-NLS-1$

			this.type = type;
			this.value = value;
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Expression#getResultType()
		 */
		@Override
		public Class<?> getResultType() {
			return type;
		}

		/**
		 * @see de.ims.icarus.model.iql.expr.Constants.AbstractConstant#value()
		 */
		@Override
		public Object value() {
			return value;
		}
	}
}
