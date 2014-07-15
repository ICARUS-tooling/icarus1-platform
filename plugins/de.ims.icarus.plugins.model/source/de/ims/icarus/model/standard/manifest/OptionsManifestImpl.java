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
package de.ims.icarus.model.standard.manifest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class OptionsManifestImpl extends AbstractDerivable<OptionsManifest> implements OptionsManifest {

	private String id;

	private Set<String> baseNames = new HashSet<>();
	private Set<Identity> groupIdentifiers = new HashSet<>();
	private Map<String, Option> options = new HashMap<>();

	@Override
	protected void copyFrom(OptionsManifest template) {

		for(String key : template.getOptionNames()) {
			Option option = new Option();
			option.name = template.getName(key);
			option.description = template.getDescription(key);
			option.valueType = template.getValueType(key);
			option.defaultValue = template.getDefaultValue(key);
			option.range = template.getSupportedRange(key);
			option.values = template.getSupportedValues(key);
			option.group = template.getOptionGroup(key);
			option.published = template.isPublished(key);
			option.multivalue = template.isMultiValue(key);

			options.put(key, option);
			baseNames.add(key);
		}
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		this.id = id;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getOptionNames()
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
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getDefaultValue(java.lang.String)
	 */
	@Override
	public Object getDefaultValue(String name) {
		return getOption(name).defaultValue;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getValueType(java.lang.String)
	 */
	@Override
	public ValueType getValueType(String name) {
		return getOption(name).valueType;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getName(java.lang.String)
	 */
	@Override
	public String getName(String name) {
		Option option = getOption(name);
		return getOption(name).name==null ? name : option.name;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getDescription(java.lang.String)
	 */
	@Override
	public String getDescription(String name) {
		return getOption(name).description;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getSupportedValues(java.lang.String)
	 */
	@Override
	public ValueSet getSupportedValues(String name) {
		return getOption(name).values;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#isPublished(java.lang.String)
	 */
	@Override
	public boolean isPublished(String name) {
		return getOption(name).published;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getOptionGroup(java.lang.String)
	 */
	@Override
	public String getOptionGroup(String name) {
		return getOption(name).group;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getGroupIdentifiers()
	 */
	@Override
	public Set<Identity> getGroupIdentifiers() {
		return CollectionUtils.getSetProxy(groupIdentifiers);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#isMultiValue(java.lang.String)
	 */
	@Override
	public boolean isMultiValue(String name) {
		return getOption(name).multivalue;
	}

	@Override
	public void addOption(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		Option option = options.get(name);
		if(option!=null)
			throw new IllegalArgumentException("Duplicate option name: "+name); //$NON-NLS-1$

		option = new Option();

		options.put(name, option);
		baseNames.add(name);
	}

	@Override
	public void removeOption(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		if(options.remove(name)==null)
			throw new IllegalArgumentException("Unknown option name: "+name); //$NON-NLS-1$

		baseNames.remove(name);
	}

	@Override
	public void setName(String key, String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		getOption(key).name = name;
	}

	@Override
	public void setDescription(String key, String description) {
		if (description == null)
			throw new NullPointerException("Invalid description"); //$NON-NLS-1$

		getOption(key).description = description;
	}

	@Override
	public void setDefaultValue(String key, Object defaultValue) {
		getOption(key).defaultValue = defaultValue;
	}

	@Override
	public void setValueType(String key, ValueType valueType) {
		if (valueType == null)
			throw new NullPointerException("Invalid valueType"); //$NON-NLS-1$

		getOption(key).valueType = valueType;
	}

	@Override
	public void setSupportedValues(String key, ValueSet values) {
		getOption(key).values = values;
	}

	public void setSupportedRange(String key, ValueRange range) {
		getOption(key).range = range;
	}

	@Override
	public void setPublished(String name, boolean published) {
		getOption(name).published = published;
	}

	@Override
	public void setOptionGroup(String key, String group) {
		getOption(key).group = group;
	}

	@Override
	public void setMultiValue(String name, boolean multivalue) {
		getOption(name).multivalue = multivalue;
	}

	@Override
	public void addGroupIdentifier(Identity identity) {
		if (identity == null)
			throw new NullPointerException("Invalid identity"); //$NON-NLS-1$

		groupIdentifiers.add(identity);
	}

	@Override
	public void removeGroupIdentifier(Identity identity) {
		if (identity == null)
			throw new NullPointerException("Invalid identity"); //$NON-NLS-1$

		groupIdentifiers.remove(identity);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getSupportedRange(java.lang.String)
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
		public String group;
		public ValueSet values;
		public ValueRange range;
		public boolean published = true;
		public boolean multivalue = false;
	}
}
