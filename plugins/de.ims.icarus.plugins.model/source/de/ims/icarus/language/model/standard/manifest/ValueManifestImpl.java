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
package de.ims.icarus.language.model.standard.manifest;

import de.ims.icarus.language.model.api.manifest.ValueManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueManifestImpl implements ValueManifest {

	private Object value;
	private String name;
	private String description;

	/**
	 * @see de.ims.icarus.language.model.api.manifest.ValueManifest#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.ValueManifest#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.ValueManifest#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		if (value == null)
			throw new NullPointerException("Invalid value");  //$NON-NLS-1$

		this.value = value;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name");  //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		if (description == null)
			throw new NullPointerException("Invalid description");  //$NON-NLS-1$

		this.description = description;
	}

}
