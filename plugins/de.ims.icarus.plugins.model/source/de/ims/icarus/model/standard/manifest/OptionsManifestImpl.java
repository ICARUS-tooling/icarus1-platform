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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class OptionsManifestImpl extends AbstractManifest<OptionsManifest> implements OptionsManifest {

	private final Set<String> baseNames = new HashSet<>();
	private final Set<Identity> groupIdentifiers = new HashSet<>();
	private final Map<String, Option> options = new HashMap<>();

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public OptionsManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry) {
		super(manifestLocation, registry);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	protected boolean isEmpty() {
		return options.isEmpty();
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#writeElements(de.ims.icarus.model.xml.XmlSerializer)
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

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_OPTIONS: {
			readAttributes(attributes);
		} break;

		case TAG_OPTION: {
			return new OptionImpl();
		}

		case TAG_GROUP: {
			DefaultModifiableIdentity identity = new DefaultModifiableIdentity();
			ModelXmlUtils.readIdentity(attributes, identity);

			addGroupIdentifier(identity);
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_OPTIONS: {
			return null;
		}

		case TAG_GROUP: {
			// no-op
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_OPTION: {
			addOption((Option) handler);
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
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

	public void addGroupIdentifier(Identity identity) {
		if (identity == null)
			throw new NullPointerException("Invalid identity"); //$NON-NLS-1$

		if(groupIdentifiers.contains(identity))
			throw new IllegalArgumentException("Duplicate group identifier: "+identity); //$NON-NLS-1$

		groupIdentifiers.add(identity);
	}

	public static class OptionImpl extends DefaultModifiableIdentity implements Option, ModelXmlHandler {
		private Object defaultValue;
		private ValueType valueType;
		private String group;
		private ValueSet values;
		private ValueRange range;
		private boolean published = DEFAULT_PUBLISHED_VALUE;
		private boolean multivalue = DEFAULT_MULTIVALUE_VALUE;

		protected OptionImpl() {
			// for parsing
		}

		public OptionImpl(String id) {
			if (id == null)
				throw new NullPointerException("Invalid id"); //$NON-NLS-1$
			setId(id);
		}

		/**
		 * @param attributes
		 */
		protected void readAttributes(Attributes attributes) {
			setValueType(ModelXmlUtils.typeValue(attributes));
			ModelXmlUtils.readIdentity(attributes, this);

			String published = ModelXmlUtils.normalize(attributes, ATTR_PUBLISHED);
			if(published!=null) {
				this.published = Boolean.parseBoolean(published);
			} else {
				this.published = DEFAULT_PUBLISHED_VALUE;
			}

			String multivalue = ModelXmlUtils.normalize(attributes, ATTR_MULTI_VALUE);
			if(multivalue!=null) {
				this.multivalue = Boolean.parseBoolean(multivalue);
			} else {
				this.multivalue = DEFAULT_MULTIVALUE_VALUE;
			}

			setGroup(ModelXmlUtils.normalize(attributes, ATTR_GROUP));
		}

		@Override
		public ModelXmlHandler startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, Attributes attributes)
						throws SAXException {
			switch (qName) {
			case TAG_OPTION: {
				readAttributes(attributes);
			} break;

			case TAG_RANGE : {
				return new ValueRangeImpl();
			}

			case TAG_VALUES : {
				return new ValueSetImpl(valueType);
			}

			case TAG_DEFAULT_VALUE : {
				// no-op
			} break;

			default:
				throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_OPTION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			return this;
		}

		@Override
		public ModelXmlHandler endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
						throws SAXException {
			switch (qName) {
			case TAG_OPTION: {
				return null;
			}

			case TAG_DEFAULT_VALUE : {
				addDefaultValue(valueType.parse(text, manifestLocation.getClassLoader()));
			} break;

			default:
				throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_OPTION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
		 */
		@Override
		public void endNestedHandler(ManifestLocation manifestLocation, String uri,
				String localName, String qName, ModelXmlHandler handler)
				throws SAXException {
			switch (qName) {

			case TAG_RANGE : {
				setRange((ValueRange) handler);
			} break;

			case TAG_VALUES : {
				setValues((ValueSet) handler);
			} break;

			default:
				throw new SAXException("Unrecognized nested tag  '"+qName+"' in "+TAG_OPTION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}

		@SuppressWarnings("unchecked")
		protected void addDefaultValue(Object value) {
			if(defaultValue instanceof Collection) {
				Collection.class.cast(defaultValue).add(value);
			} else if(defaultValue!=null) {
				if(!multivalue)
					throw new IllegalStateException("Cannot add more than one default value to optin that is not declared as multivalue"); //$NON-NLS-1$

				List<Object> list = new ArrayList<>(4);
				CollectionUtils.feedItems(list, defaultValue, value);
				defaultValue = list;
			}  else {
				defaultValue = value;
			}
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
