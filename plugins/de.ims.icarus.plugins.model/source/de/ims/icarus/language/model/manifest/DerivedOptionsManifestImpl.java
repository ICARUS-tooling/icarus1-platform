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
package de.ims.icarus.language.model.manifest;

import java.util.Set;

import de.ims.icarus.language.model.meta.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DerivedOptionsManifestImpl implements OptionsManifest {

	private OptionsManifest template;
	private OptionsManifest override;

	private Set<String> mergedNames;

	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getOptionNames()
	 */
	@Override
	public Set<String> getOptionNames() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getDefaultValue(java.lang.String)
	 */
	@Override
	public Object getDefaultValue(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getValueType(java.lang.String)
	 */
	@Override
	public ValueType getValueType(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getName(java.lang.String)
	 */
	@Override
	public String getName(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getDescription(java.lang.String)
	 */
	@Override
	public String getDescription(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getSupportedValues(java.lang.String)
	 */
	@Override
	public ValueIterator getSupportedValues(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getSupportedRange(java.lang.String)
	 */
	@Override
	public ValueRange getSupportedRange(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}
