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
package de.ims.icarus.eval;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExpressionFactory {

	private Map<String, Class<?>> variables = new LinkedHashMap<>();

	private String code;
	private Environment environment;

	private Expression expression;

	public void addVariable(String id, Class<?> namespace) {
		if (id == null)
			throw new NullPointerException("Invalid id");
		if (namespace == null)
			throw new NullPointerException("Invalid namespace");

		//TODO
	}

	public Expression build() {
		if(expression==null) {
			//TODO
		}

		return expression;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the environment
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		if (code == null)
			throw new NullPointerException("Invalid code");

		this.code = code;
	}

	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(Environment environment) {
		if (environment == null)
			throw new NullPointerException("Invalid environment");

		this.environment = environment;
	}
}
