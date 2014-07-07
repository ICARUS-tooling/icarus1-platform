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
package de.ims.icarus.search_tools.io;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ParameterType {

	INTEGER(Integer.class) {
		@Override
		public Object parse(String s) {
			return Integer.parseInt(s);
		}
	},
	LONG(Long.class) {
		@Override
		public Object parse(String s) {
			return Long.parseLong(s);
		}
	},
	STRING(String.class) {
		@Override
		public Object parse(String s) {
			return s;
		}
	},
	FLOAT(Float.class) {
		@Override
		public Object parse(String s) {
			return Float.parseFloat(s);
		}
	},
	DOUBLE(Double.class) {
		@Override
		public Object parse(String s) {
			return Double.parseDouble(s);
		}
	},
	BOOLEAN(Boolean.class) {
		@Override
		public Object parse(String s) {
			return Boolean.parseBoolean(s);
		}
	},
	ENUM(Enum.class) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object parse(String s) {
			String[] parts = s.split("@"); //$NON-NLS-1$
			try {
				Class<?> clazz = Class.forName(parts[0]);

				return Enum.valueOf((Class<Enum>)clazz, parts[1]);
			} catch (ClassNotFoundException e) {
//				LoggerFactory.error(this, "Failed to load enum class for parameter: "+s, e); //$NON-NLS-1$
				throw new CorruptedStateException("Unable to parse parameter: "+s, e); //$NON-NLS-1$
			}

		}

		/**
		 * @see de.ims.icarus.search_tools.io.ParameterType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object param) {
			Enum<?> enumType = (Enum<?>) param;

			return enumType.getDeclaringClass().getName()+"@"+enumType.name(); //$NON-NLS-1$
		}
	};

	private final Class<?> clazz;

	private ParameterType(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public abstract Object parse(String s);
	public String toString(Object param) {
		return String.valueOf(param);
	}

	private static Map<Class<?>, ParameterType> lut;

	public static ParameterType getType(Object parameter) {

		if(parameter instanceof Enum) {
			return ENUM;
		}


		if(lut==null) {
			Map<Class<?>, ParameterType> map = new HashMap<>();

			for(ParameterType type : values()) {
				map.put(type.clazz, type);
			}

			lut = map;
		}

		ParameterType type = lut.get(parameter.getClass());

		if(type==null)
			throw new IllegalArgumentException("No type registered for class: "+parameter.getClass()); //$NON-NLS-1$

		return type;
	}

	public static ParameterType parseParameterType(String s) {
		return valueOf(s.toUpperCase());
	}
}
