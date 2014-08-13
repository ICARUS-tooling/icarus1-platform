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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.Documentation;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.MemberManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 *
 * This class is not thread-safe!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractMemberManifest<M extends MemberManifest> extends AbstractManifest<M> implements MemberManifest {

	private String name;
	private String description;
	private Icon icon;

	private final Map<String, Object> properties = new HashMap<>();
	private OptionsManifest optionsManifest;
	private Documentation documentation;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	protected AbstractMemberManifest(ManifestLocation manifestLocation,
			CorpusRegistry registry) {
		super(manifestLocation, registry);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	protected boolean isEmpty() {
		return properties.isEmpty() && optionsManifest==null && documentation==null;
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write documentation
		writeEmbedded(documentation, serializer);

		// Write options manifest
		writeEmbedded(optionsManifest, serializer);

		if(!properties.isEmpty()) {
			serializer.startElement(TAG_PROPERTIES);

			List<String> names = CollectionUtils.asSortedList(properties.keySet());

			// Use inherited options manifest to decide about value types if required!
			OptionsManifest optionsManifest = getOptionsManifest();

			for(String name : names) {
				Object value = properties.get(name);
				ValueType type = optionsManifest==null ?
						ValueType.STRING : optionsManifest.getOption(name).getValueType();

				boolean multiValue = optionsManifest==null ?
						false : optionsManifest.getOption(name).isMultiValue();

				// Multi-value conscious serialization
				if(multiValue && value instanceof Collection) {
					for(Object item : (Collection<?>) value) {
						ModelXmlUtils.writePropertyElement(serializer, name, item, type);
					}
				} else {
					ModelXmlUtils.writePropertyElement(serializer, name, value, type);
				}
			}

			serializer.endElement(TAG_PROPERTIES);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		ModelXmlUtils.writeIdentityAttributes(serializer, null, name, description, icon);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ModelXmlUtils.readIdentity(attributes, this);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_OPTIONS: {
			return new OptionsManifestImpl(getManifestLocation(), getRegistry());
		}

		case TAG_DOCUMENTATION: {
			return new DocumentationImpl();
		}

		case TAG_PROPERTIES: {
			return new PropertiesXmlHandler();
		}

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {

		switch (qName) {
		case TAG_OPTIONS: {
			setOptionsManifest((OptionsManifest) handler);
		} break;

		case TAG_DOCUMENTATION: {
			setDocumentation((Documentation) handler);
		} break;

		case TAG_PROPERTIES: {
			setProperties(((PropertiesXmlHandler)handler).getProperties());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}

	}

	/**
	 * @return the documentation
	 */
	@Override
	public Documentation getDocumentation() {
		Documentation documentation = this.documentation;
		if(documentation==null && hasTemplate()) {
			documentation = getTemplate().getDocumentation();
		}
		return documentation;
	}

	/**
	 * @param documentation the documentation to set
	 */
	public void setDocumentation(Documentation documentation) {
		this.documentation = documentation;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ModifiableManifest#getOptionsManifest()
	 */
	@Override
	public OptionsManifest getOptionsManifest() {
		OptionsManifest result = this.optionsManifest;
		if(result==null && hasTemplate()) {
			result = getTemplate().getOptionsManifest();
		}
		return result;
	}

	/**
	 * @param optionsManifest the optionsManifest to set
	 */
	@Override
	public void setOptionsManifest(OptionsManifest optionsManifest) {
		if (optionsManifest == null)
			throw new NullPointerException("Invalid optionsManifest"); //$NON-NLS-1$

		this.optionsManifest = optionsManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String name) {
		Object value = null;

		if(properties!=null) {
			value = properties.get(name);
		}

		if(value==null && hasTemplate()) {
			value = getTemplate().getProperty(name);
		}

		return value;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getPropertyNames()
	 */
	@Override
	public Set<String> getPropertyNames() {
		Set<String> result = new HashSet<>();
		if(properties!=null) {
			result.addAll(properties.keySet());
		}

		if(hasTemplate()) {
			result.addAll(getTemplate().getPropertyNames());
		}

		return result;
	}

	@Override
	public void setProperty(String key, Object value) {
		if(key==null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		//FIXME sanity check for value type!

		properties.put(key, value);
	}

	public void setProperties(Map<String, Object> values) {
		if(values==null)
			throw new NullPointerException("Invalid values"); //$NON-NLS-1$

		properties.putAll(values);
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		String result = name;
		if(result==null && hasTemplate()) {
			result = getTemplate().getName();
		}
		return result;
	}

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		String result = description;
		if(result==null && hasTemplate()) {
			result = getTemplate().getDescription();
		}
		return result;
	}

	/**
	 * @return the icon
	 */
	@Override
	public Icon getIcon() {
		Icon result = icon;
		if(result==null && hasTemplate()) {
			result = getTemplate().getIcon();
		}
		return result;
	}

	/**
	 * @param name the name to set
	 */
	@Override
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	@Override
	public void setDescription(String description) {
		if (description == null)
			throw new NullPointerException("Invalid description"); //$NON-NLS-1$

		this.description = description;
	}

	/**
	 * @param icon the icon to set
	 */
	@Override
	public void setIcon(Icon icon) {
		if (icon == null)
			throw new NullPointerException("Invalid icon"); //$NON-NLS-1$

		this.icon = icon;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	public static class PropertiesXmlHandler implements ModelXmlHandler {

		private final Map<String, Object> properties = new LinkedHashMap<>();

		private String name;
		private ValueType valueType;

		@SuppressWarnings("unchecked")
		private void addProperty(String key, Object value) {
			Object current = properties.get(key);

			if(current instanceof Collection) {
				Collection.class.cast(current).add(value);
			} else if(current!=null) {
				List<Object> list = new ArrayList<>(4);
				CollectionUtils.feedItems(list, current, value);
				properties.put(key, list);
			} else {
				properties.put(key, value);
			}
		}

		public Map<String, Object> getProperties() {
			return properties;
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public ModelXmlHandler startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			switch (qName) {
			case TAG_PROPERTIES: {
				// no-op
			} break;

			case TAG_PROPERTY: {
				name = ModelXmlUtils.normalize(attributes, ATTR_NAME);
				valueType = ModelXmlUtils.typeValue(attributes);
			} break;

			default:
				throw new SAXException("Unexpected opening tag in properties environment: "+qName); //$NON-NLS-1$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#endElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public ModelXmlHandler endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
				throws SAXException {
			switch (qName) {
			case TAG_PROPERTIES: {
				return null;
			}

			case TAG_PROPERTY: {

				Object value = valueType.parse(text, manifestLocation.getClassLoader());

				addProperty(name, value);

				name = null;
				valueType = null;
			} break;

			default:
				throw new SAXException("Unexpected end tag in properties environment: "+qName); //$NON-NLS-1$
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
			throw new SAXException("Unexpected nested element "+qName+" in "+TAG_PROPERTIES+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

	}
}
