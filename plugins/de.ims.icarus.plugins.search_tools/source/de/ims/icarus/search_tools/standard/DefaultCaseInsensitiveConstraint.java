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
package de.ims.icarus.search_tools.standard;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

import de.ims.icarus.search_tools.SearchOperator;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultCaseInsensitiveConstraint extends DefaultConstraint {

	private static final long serialVersionUID = -7648734660494017554L;

	@XmlTransient
	protected Object lowercaseValue;

	public DefaultCaseInsensitiveConstraint(String token, Object value,
			SearchOperator operator) {
		super(token, value, operator);
	}

	public DefaultCaseInsensitiveConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}

	@Override
	protected Object getConstraint() {
		return lowercaseValue;
	}

	@XmlElements({ @XmlElement(name = "string", type = String.class),
			@XmlElement(name = "integer", type = Integer.class),
			@XmlElement(name = "float", type = Float.class),
			@XmlElement(name = "double", type = Double.class),
			@XmlElement(name = "long", type = Long.class),
			@XmlElement(name = "boolean", type = Boolean.class) })
	@Override
	public void setValue(Object value) {
		super.setValue(value);
		lowercaseValue = String.valueOf(value).toLowerCase();
	}

	public Object getLowercaseValue() {
		return lowercaseValue;
	}

	/**
	 * @see de.ims.icarus.search_tools.standard.DefaultConstraint#clone()
	 */
	@Override
	public DefaultCaseInsensitiveConstraint clone() {
		return (DefaultCaseInsensitiveConstraint) super.clone();
	}
}