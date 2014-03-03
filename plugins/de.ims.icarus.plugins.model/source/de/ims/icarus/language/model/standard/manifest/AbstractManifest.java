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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Icon;

import de.ims.icarus.language.model.api.manifest.Implementation;
import de.ims.icarus.language.model.api.manifest.MemberManifest;
import de.ims.icarus.language.model.api.manifest.OptionsManifest;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 *
 * This class is not thread-safe!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractManifest<M extends MemberManifest> extends AbstractDerivable<M> implements MemberManifest {

	private Map<String, Object> properties;
	private OptionsManifest optionsManifest;
	private Implementation implementation;

	private String name;
	private String description;
	private String id;
	private String rawId;
	private Icon icon;

	/**
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#readTemplate()
	 */
	@Override
	protected void readTemplate(M template) {
		super.readTemplate(template);

		if(id==null) {
			id = template.getId();
		}
		if(rawId==null) {
			rawId = template.getRawId();
		}
		if(name==null) {
			name = template.getName();
		}
		if(description==null) {
			description = template.getDescription();
		}
		if(icon==null) {
			icon = template.getIcon();
		}

		OptionsManifest optionsManifest = template.getOptionsManifest();
		if(optionsManifest!=null) {
			if(this.optionsManifest==null) {
				this.optionsManifest = new OptionsManifestImpl();
			}
			this.optionsManifest.setTemplate(optionsManifest);
		}

		for(String key : template.getPropertyNames()) {
			if(getProperty(key)==null) {
				setProperty(key, template.getProperty(key));
			}
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id==null ? 0 : id.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MemberManifest) {
			MemberManifest other = (MemberManifest) obj;
			return getManifestType()==other.getManifestType()
					&& ClassUtils.equals(id, other.getId());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = getManifestType().toString();

		if(id!=null) {
			s += "@"+id; //$NON-NLS-1$
		}

		return s;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.MemberManifest#getRawId()
	 */
	@Override
	public String getRawId() {
		return rawId;
	}

	/**
	 * @return the icon
	 */
	@Override
	public Icon getIcon() {
		return icon;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		if (description == null)
			throw new NullPointerException("Invalid description"); //$NON-NLS-1$

		this.description = description;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if(!CorpusRegistry.isValidId(id))
			throw new IllegalArgumentException("Id format not supported: "+id); //$NON-NLS-1$

		this.id = id;

		// Copy over raw id if not already defined
		if(rawId==null) {
			rawId = id;
		}
	}

	/**
	 * @param icon the icon to set
	 */
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

	/**
	 * @return the implementation
	 */
	@Override
	public Implementation getImplementation() {
		return implementation;
	}

	/**
	 * @param implementation the implementation to set
	 */
	public void setImplementation(Implementation implementation) {
		if (implementation == null)
			throw new NullPointerException("Invalid implementation"); //$NON-NLS-1$

		this.implementation = implementation;
	}

	/**
	 * @param optionsManifest the optionsManifest to set
	 */
	public void setOptionsManifest(OptionsManifest optionsManifest) {
		if (optionsManifest == null)
			throw new NullPointerException("Invalid optionsManifest"); //$NON-NLS-1$

		this.optionsManifest = optionsManifest;
	}

	protected boolean isTemplateKey(String name) {
		return hasTemplate() && getTemplate().getPropertyNames().contains(name);
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.MemberManifest#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String name) {
		return properties==null ? null : properties.get(name);
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.MemberManifest#getPropertyNames()
	 */
	@Override
	public Set<String> getPropertyNames() {
		Set<String> result = null;
		if(properties!=null) {
			result = CollectionUtils.getSetProxy(properties.keySet());
		}

		if(result==null) {
			result = Collections.emptySet();
		}

		return result;
	}

	@Override
	public void setProperty(String key, Object value) {
		if(key==null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		if(properties==null) {
			properties = new HashMap<>();
		}

		properties.put(key, value);
	}

	public void setProperties(Map<String, Object> values) {
		if(values==null)
			throw new NullPointerException("Invalid values"); //$NON-NLS-1$

		if(properties==null) {
			properties = new HashMap<>();
		}

		properties.putAll(values);
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.MemberManifest#getOptionsManifest()
	 */
	@Override
	public OptionsManifest getOptionsManifest() {
		return optionsManifest;
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#writeTemplateXmlAttributes(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "id", id, getTemplate().getId()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "name", name, getTemplate().getName()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "description", description, getTemplate().getDescription()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "icon", icon, getTemplate().getIcon()); //$NON-NLS-1$

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
		serializer.writeAttribute("name", name); //$NON-NLS-1$
		serializer.writeAttribute("description", description); //$NON-NLS-1$
		writeXmlAttribute(serializer, "icon", icon); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#writeTemplateXmlElements(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		if(optionsManifest!=null) {
			if(properties!=null) {

				// Find properties that differ from the template and only
				// serialize them
				for(Entry<String, Object> entry : properties.entrySet()) {
					String name = entry.getKey();
					Object value = entry.getValue();

					// Skip properties that are equal to those defined in the template
					if(value.equals(getTemplate().getProperty(name))) {
						continue;
					}

					XmlWriter.writePropertyElement(serializer, name,
							value, optionsManifest.getValueType(name));
				}
			}

			XmlWriter.writeOptionsManifestElement(serializer, optionsManifest);
		}

		if(implementation!=null && !implementation.equals(getTemplate().getImplementation())) {
			implementation.writeXml(serializer);
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

		if(optionsManifest!=null) {
			XmlWriter.writeProperties(serializer, properties, optionsManifest);
			XmlWriter.writeOptionsManifestElement(serializer, optionsManifest);
		}

		if(implementation!=null) {
			implementation.writeXml(serializer);
		};
	}
}
