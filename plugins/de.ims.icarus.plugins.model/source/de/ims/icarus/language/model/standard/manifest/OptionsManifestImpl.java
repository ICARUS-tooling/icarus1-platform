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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.model.manifest.OptionsManifest;
import de.ims.icarus.language.model.manifest.ValueIterator;
import de.ims.icarus.language.model.manifest.ValueRange;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.standard.manifest.Values.ValueIteratorFactory;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class OptionsManifestImpl implements OptionsManifest {

	private Set<String> baseNames = new HashSet<>();
	private Map<String, Option> options = new HashMap<>();

	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getOptionNames()
	 */
	@Override
	public Set<String> getOptionNames() {
		return CollectionUtils.getSetProxy(baseNames);
	}

	private Option getOption(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		Option option = options.get(name);
		if(option==null)
			throw new IllegalArgumentException("No such option: "+name); //$NON-NLS-1$

		return option;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getDefaultValue(java.lang.String)
	 */
	@Override
	public Object getDefaultValue(String name) {
		return getOption(name).defaultValue;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getValueType(java.lang.String)
	 */
	@Override
	public ValueType getValueType(String name) {
		return getOption(name).valueType;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getName(java.lang.String)
	 */
	@Override
	public String getName(String name) {
		Option option = getOption(name);
		return getOption(name).name==null ? name : option.name;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getDescription(java.lang.String)
	 */
	@Override
	public String getDescription(String name) {
		return getOption(name).description;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getSupportedValues(java.lang.String)
	 */
	@Override
	public ValueIterator getSupportedValues(String name) {
		return getOption(name).iteratorFactory.newIterator();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.OptionsManifest#getSupportedRange(java.lang.String)
	 */
	@Override
	public ValueRange getSupportedRange(String name) {
		return getOption(name).range;
	}

	private static class Option {
		public Object defaultValue;
		public ValueType valueType;
		public String name;
		public String description;
		public ValueIteratorFactory iteratorFactory;
		public ValueRange range;
	}
}
