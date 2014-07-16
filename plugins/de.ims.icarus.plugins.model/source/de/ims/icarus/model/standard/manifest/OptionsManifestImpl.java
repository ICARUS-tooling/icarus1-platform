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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class OptionsManifestImpl extends AbstractDerivable<OptionsManifest> implements OptionsManifest {

	private final Set<String> baseNames = new HashSet<>();
	private final Set<Identity> groupIdentifiers = new HashSet<>();
	private final Map<String, Option> options = new HashMap<>();

	/**
	 * @param manifestSource
	 * @param registry
	 */
	public OptionsManifestImpl(ManifestSource manifestSource,
			CorpusRegistry registry) {
		super(manifestSource, registry);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#writeElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write options in alphabetic order
		if(!options.isEmpty()) {
			List<String> names = CollectionUtils.asSortedList(options.keySet());

			for(String option : names) {
				ModelXmlUtils.writeOptionElement(serializer, options.get(option));
			}
		}

		// Write groups in alphabetic order
		if(!groupIdentifiers.isEmpty()) {
			List<Identity> idents = CollectionUtils.asSortedList(groupIdentifiers, Identity.COMPARATOR);

			for(Identity group : idents) {
				//FIXME empty element
				serializer.startElement(TAG_GROUP);

				// ATTRIBUTES
				ModelXmlUtils.writeIdentityAttributes(serializer, group);
				serializer.endElement(TAG_GROUP);
			}
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_OPTIONS;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getOptionNames()
	 */
	@Override
	public Set<String> getOptionNames() {
		Set<String> result = new HashSet<>();
		result.addAll(baseNames);

		if(hasTemplate()) {
			result.addAll(getTemplate().getOptionNames());
		}

		return result;
	}

	@Override
	public Option getOption(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		Option option = options.get(id);

		if(option==null && hasTemplate()) {
			option = getTemplate().getOption(id);
		}

		if(option==null)
			throw new IllegalArgumentException("No such option: "+id); //$NON-NLS-1$

		return option;
	}

	public void addOption(Option option) {
		if (option == null)
			throw new NullPointerException("Invalid option"); //$NON-NLS-1$

		String id = option.getId();

		if(id==null)
			throw new IllegalArgumentException("Option does not declare a valid id"); //$NON-NLS-1$

		if(options.containsKey(id))
			throw new IllegalArgumentException("Duplicate option id: "+id); //$NON-NLS-1$

		options.put(id, option);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.OptionsManifest#getGroupIdentifiers()
	 */
	@Override
	public Set<Identity> getGroupIdentifiers() {
		Set<Identity> result = new HashSet<>(groupIdentifiers);
		if(hasTemplate()) {
			result.addAll(getTemplate().getGroupIdentifiers());
		}

		return result;
	}

	public static class OptionImpl implements Option {
		private Object defaultValue;
		private ValueType valueType;
		private final String id;
		private String name;
		private String description;
		private String group;
		private ValueSet values;
		private ValueRange range;
		private boolean published = DEFAULT_PUBLISHED_VALUE;
		private boolean multivalue = DEFAULT_MULTIVALUE_VALUE;

		public OptionImpl(String id) {
			if (id == null)
				throw new NullPointerException("Invalid id"); //$NON-NLS-1$
			this.id = id;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}
		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#getId()
		 */
		@Override
		public String getId() {
			return id;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#getDefaultValue()
		 */
		@Override
		public Object getDefaultValue() {
			return defaultValue;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#getValueType()
		 */
		@Override
		public ValueType getValueType() {
			return valueType;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#getName()
		 */
		@Override
		public String getName() {
			return name;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#getDescription()
		 */
		@Override
		public String getDescription() {
			return description;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#getSupportedValues()
		 */
		@Override
		public ValueSet getSupportedValues() {
			return values;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#getSupportedRange()
		 */
		@Override
		public ValueRange getSupportedRange() {
			return range;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#getOptionGroup()
		 */
		@Override
		public String getOptionGroup() {
			return group;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#isPublished()
		 */
		@Override
		public boolean isPublished() {
			return published;
		}
		/**
		 * @see de.ims.icarus.model.api.manifest.OptionsManifest.Option#isMultiValue()
		 */
		@Override
		public boolean isMultiValue() {
			return multivalue;
		}

		/**
		 * @param defaultValue the defaultValue to set
		 */
		public void setDefaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
		}

		/**
		 * @param valueType the valueType to set
		 */
		public void setValueType(ValueType valueType) {
			this.valueType = valueType;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @param group the group to set
		 */
		public void setGroup(String group) {
			this.group = group;
		}

		/**
		 * @param values the values to set
		 */
		public void setValues(ValueSet values) {
			this.values = values;
		}

		/**
		 * @param range the range to set
		 */
		public void setRange(ValueRange range) {
			this.range = range;
		}

		/**
		 * @param published the published to set
		 */
		public void setPublished(boolean published) {
			this.published = published;
		}

		/**
		 * @param multivalue the multivalue to set
		 */
		public void setMultivalue(boolean multivalue) {
			this.multivalue = multivalue;
		}
	}
}
