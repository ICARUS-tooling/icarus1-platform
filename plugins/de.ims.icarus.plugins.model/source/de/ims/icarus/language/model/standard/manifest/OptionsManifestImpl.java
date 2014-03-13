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

import de.ims.icarus.language.model.api.manifest.OptionsManifest;
import de.ims.icarus.language.model.api.manifest.ValueRange;
import de.ims.icarus.language.model.api.manifest.ValueSet;
import de.ims.icarus.language.model.util.ValueType;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class OptionsManifestImpl extends AbstractDerivable<OptionsManifest> implements OptionsManifest {

	private String id;

	private Set<String> baseNames = new HashSet<>();
	private Map<String, Option> options = new HashMap<>();

	/**
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#readTemplate(de.ims.icarus.language.model.api.manifest.Derivable)
	 */
	@Override
	protected void readTemplate(OptionsManifest template) {
		super.readTemplate(template);

		Set<String> names = getOptionNames();

		for(String key : template.getOptionNames()) {
			if(!names.contains(key)) {
				addOption(key);
			}

			if(getName(key)==null) {
				setName(key, template.getName(key));
			}
			if(getDescription(key)==null) {
				setDescription(key, template.getDescription(key));
			}
			if(getValueType(key)==null) {
				setValueType(key, template.getValueType(key));
			}
			if(getDefaultValue(key)==null) {
				setDefaultValue(key, template.getDefaultValue(key));
			}
			if(getSupportedRange(key)==null) {
				setRange(key, template.getSupportedRange(key));
			}

			ValueSet valueSet = template.getSupportedValues(key);
			if(valueSet!=null) {
				ValueSet currentValues = getSupportedValues(key);
				if(currentValues==null) {
					currentValues = new ValueSetImpl();
					setSupportedValues(key, currentValues);
				}
				currentValues.setTemplate(valueSet);
			}
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
	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		this.id = id;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.OptionsManifest#getOptionNames()
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
	 * @see de.ims.icarus.language.model.api.manifest.OptionsManifest#getDefaultValue(java.lang.String)
	 */
	@Override
	public Object getDefaultValue(String name) {
		return getOption(name).defaultValue;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.OptionsManifest#getValueType(java.lang.String)
	 */
	@Override
	public ValueType getValueType(String name) {
		return getOption(name).valueType;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.OptionsManifest#getName(java.lang.String)
	 */
	@Override
	public String getName(String name) {
		Option option = getOption(name);
		return getOption(name).name==null ? name : option.name;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.OptionsManifest#getDescription(java.lang.String)
	 */
	@Override
	public String getDescription(String name) {
		return getOption(name).description;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.OptionsManifest#getSupportedValues(java.lang.String)
	 */
	@Override
	public ValueSet getSupportedValues(String name) {
		return getOption(name).values;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.OptionsManifest#isPublished(java.lang.String)
	 */
	@Override
	public boolean isPublished(String name) {
		return getOption(name).published;
	}

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

	public void setName(String key, String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		getOption(key).name = name;
	}

	public void setDescription(String key, String description) {
		if (description == null)
			throw new NullPointerException("Invalid description"); //$NON-NLS-1$

		getOption(key).description = description;
	}

	public void setDefaultValue(String key, Object defaultValue) {
		getOption(key).defaultValue = defaultValue;
	}

	public void setValueType(String key, ValueType valueType) {
		if (valueType == null)
			throw new NullPointerException("Invalid valueType"); //$NON-NLS-1$

		getOption(key).valueType = valueType;
	}

	public void setSupportedValues(String key, ValueSet values) {
		getOption(key).values = values;
	}

	public void setRange(String key, ValueRange range) {
		getOption(key).range = range;
	}

	public void setPublished(String name, boolean published) {
		getOption(name).published = published;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.OptionsManifest#getSupportedRange(java.lang.String)
	 */
	@Override
	public ValueRange getSupportedRange(String name) {
		return getOption(name).range;
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#writeTemplateXmlAttributes(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		serializer.writeAttribute("id", id); //$NON-NLS-1$
		serializer.writeAttribute("template-id", getTemplate().getId()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#writeFullXmlAttributes(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("id", id); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#writeTemplateXmlElements(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		OptionsManifest template = getTemplate();
		Set<String> derived = new HashSet<>(template.getOptionNames());

		// Only serialize options that differ from the template definition
		for(String option : baseNames) {
			if(derived.contains(option)
					&& ClassUtils.equals(getName(option), template.getName(option))
					&& ClassUtils.equals(getDescription(option), template.getDescription(option))
					&& ClassUtils.equals(getDefaultValue(option), template.getDefaultValue(option))
					&& ClassUtils.equals(getValueType(option), template.getValueType(option))
					&& ClassUtils.equals(getSupportedValues(option), template.getSupportedValues(option))
					&& ClassUtils.equals(getSupportedRange(option), template.getSupportedRange(option))) {
				continue;
			}

			XmlWriter.writeOptionElement(serializer, option, this);
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#writeFullXmlElements(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		for(String option : baseNames) {
			XmlWriter.writeOptionElement(serializer, option, this);
		}
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "options"; //$NON-NLS-1$
	}

	private static class Option {
		public Object defaultValue;
		public ValueType valueType;
		public String name;
		public String description;
		public ValueSet values;
		public ValueRange range;
		public boolean published = true;
	}
}
