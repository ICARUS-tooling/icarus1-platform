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
package de.ims.icarus.language.model.standard.meta;

import java.util.Set;

import de.ims.icarus.language.model.meta.ValueSet;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.standard.ProxyIdentity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FixedValueSet extends ProxyIdentity implements ValueSet {

	/**
	 * @see de.ims.icarus.language.model.meta.ValueSet#getValues()
	 */
	@Override
	public Set<String> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.meta.ValueSet#getName(java.lang.String)
	 */
	@Override
	public String getName(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.meta.ValueSet#getDescription(java.lang.String)
	 */
	@Override
	public String getDescription(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.meta.ValueSet#getValueType()
	 */
	@Override
	public ValueType getValueType() {
		// TODO Auto-generated method stub
		return null;
	}

}
