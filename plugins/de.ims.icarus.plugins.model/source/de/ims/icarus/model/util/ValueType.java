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
package de.ims.icarus.model.util;

import org.java.plugin.registry.Extension;

import de.ims.icarus.model.xml.XmlResource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ValueType implements XmlResource {

	UNKNOWN(Object.class),
	CUSTOM(Object.class),
	STRING(String.class),
	BOOLEAN(Boolean.class),
	INTEGER(Integer.class),
	LONG(Long.class),
	DOUBLE(Double.class),
	EXTENSION(Extension.class);

	private final Class<?> baseClass;

	private ValueType(Class<?> baseClass) {
		this.baseClass = baseClass;
	}

	/**
	 * @see de.ims.icarus.model.api.xml.XmlResource#getValue()
	 */
	@Override
	public String getValue() {
		return name().toLowerCase();
	}

	public static ValueType parseValueType(String s) {
		return valueOf(s.toUpperCase());
	}

	public boolean isValidValue(Object value) {
		return value!=null && baseClass.isAssignableFrom(value.getClass());
	}
}
