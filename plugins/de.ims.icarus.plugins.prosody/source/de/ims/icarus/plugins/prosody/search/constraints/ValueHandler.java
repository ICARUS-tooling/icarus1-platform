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
package de.ims.icarus.plugins.prosody.search.constraints;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ValueHandler {

	public String valueToLabel(Object value) {
		return String.valueOf(value);
	}

	public Object labelToValue(Object label) {
		return label;
	}

	public Object[] getLabelSet() {
		return null;
	}

	public abstract Class<?> getValueClass();

	public abstract Object getDefaultValue();

	// Calc o1-o2
	public abstract Object substract(Object o1, Object o2);


	public static final ValueHandler stringHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return String.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		}

		@Override
		public Object substract(Object o1, Object o2) {
			throw new UnsupportedOperationException("Cannot substract strings!");
		}
	};

	public static final ValueHandler integerHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Integer.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseIntegerLabel((String)label);
		}

		@Override
		public String valueToLabel(Object value) {
			return LanguageUtils.getLabel((int)value);
		}

		@Override
		public Object substract(Object o1, Object o2) {
			return (int)o1-(int)o2;
		}
	};

	public static final ValueHandler longHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Long.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseIntegerLabel((String)label);
		}

		@Override
		public String valueToLabel(Object value) {
			return LanguageUtils.getLabel((int)value);
		}

		@Override
		public Object substract(Object o1, Object o2) {
			return (long)o1-(long)o2;
		}
	};

	public static final ValueHandler floatHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Float.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_FLOAT_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseFloatLabel((String)label);
		}

		@Override
		public String valueToLabel(Object value) {
			return LanguageUtils.getLabel((float)value);
		}

		@Override
		public Object substract(Object o1, Object o2) {
			return (float)o1-(float)o2;
		}
	};

	public static final ValueHandler doubleHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Double.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_DOUBLE_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseDoubleLabel((String)label);
		}

		@Override
		public String valueToLabel(Object value) {
			return LanguageUtils.getLabel((double)value);
		}

		@Override
		public Object substract(Object o1, Object o2) {
			return (double)o1-(double)o2;
		}
	};

	public static final ValueHandler booleanHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Double.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseBooleanLabel((String)label);
		}

		@Override
		public Object[] getLabelSet() {
			return new Object[]{
					LanguageConstants.DATA_UNDEFINED_LABEL,
					LanguageUtils.getBooleanLabel(LanguageConstants.DATA_YES_VALUE),
					LanguageUtils.getBooleanLabel(LanguageConstants.DATA_NO_VALUE),
			};
		}

		@Override
		public Object substract(Object o1, Object o2) {
			throw new UnsupportedOperationException("Cannot substarct boolean values!");
		}
	};
}
