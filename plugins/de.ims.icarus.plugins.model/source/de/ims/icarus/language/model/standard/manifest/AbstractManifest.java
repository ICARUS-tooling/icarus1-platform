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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import de.ims.icarus.language.model.manifest.Manifest;
import de.ims.icarus.language.model.manifest.OptionsManifest;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.language.model.xml.XmlResource;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.logging.LoggerFactory;
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
public abstract class AbstractManifest<M extends Manifest> extends DerivedObject<M> implements Manifest {

	private Map<String, Object> properties;
	private OptionsManifest optionsManifest;

	private String name;
	private String description;
	private String id;
	private Icon icon;

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
		if(obj instanceof Manifest) {
			Manifest other = (Manifest) obj;
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
		return name==null && hasTemplate() ? getTemplate().getName() : name;
	}

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return description==null && hasTemplate() ? getTemplate().getDescription() : description;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @return the icon
	 */
	@Override
	public Icon getIcon() {
		return icon==null && hasTemplate() ? getTemplate().getIcon() : icon;
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

		this.id = id;
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
	 * @param optionsManifest the optionsManifest to set
	 */
	public void setOptionsManifest(OptionsManifest optionsManifest) {
		if (optionsManifest == null)
			throw new NullPointerException("Invalid optionsManifest"); //$NON-NLS-1$

		this.optionsManifest = optionsManifest;
	}

	protected boolean isLocalKey(String name) {
		return properties!=null && properties.containsKey(name);
	}

	protected boolean isTemplateKey(String name) {
		return hasTemplate() && getTemplate().getPropertyNames().contains(name);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String name) {
		Object result = properties==null ? null : properties.get(name);
		if(result==null && hasTemplate()) {
			result = getTemplate().getProperty(name);
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getPropertyNames()
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
	 * @see de.ims.icarus.language.model.manifest.Manifest#getOptionsManifest()
	 */
	@Override
	public OptionsManifest getOptionsManifest() {
		OptionsManifest optionsManifest = this.optionsManifest;

		if(optionsManifest==null && hasTemplate()) {
			optionsManifest = getTemplate().getOptionsManifest();
		}

		return optionsManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.DerivedObject#resolveTemplate(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected M resolveTemplate(String templateId) {
		Manifest tpl = CorpusRegistry.getInstance().getTemplate(templateId);

		if(tpl==null)
			throw new IllegalStateException("No such template: "+templateId); //$NON-NLS-1$
		if(tpl.getManifestType()!=getManifestType())
			throw new IllegalStateException("Manifest type mismatch"); //$NON-NLS-1$

		return (M) tpl;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.DerivedObject#templateLoaded(java.lang.Object)
	 */
	@Override
	protected void templateLoaded(M template) {
		super.templateLoaded(template);

		// Copy over all properties from the template
		for(String key : template.getPropertyNames()) {
			setProperty(key, template.getProperty(key));
		}
	}

	/**
	 * Writes out the set of current properties on this manifest and the
	 * {@code OptionsManifest} if one was set.
	 * <p>
	 * Calls {@code super#defaultWriteXml(XmlSerializer)} to take care of
	 * the template-id if present.
	 *
	 * @param serializer
	 * @throws IOException
	 * @see {@link DerivedObject#defaultWriteXml(XmlSerializer)}
	 */
	@Override
	protected void defaultWriteXml(XmlSerializer serializer) throws IOException {
		// Ensure serialization of template-id
		super.defaultWriteXml(serializer);

		// Serialize only local information, do NOT use
		// default getter methods that might resolve values
		// from template!
		serializer.writeAttribute("id", id); //$NON-NLS-1$
		serializer.writeAttribute("name", name); //$NON-NLS-1$
		serializer.writeAttribute("description", description); //$NON-NLS-1$

		if(icon instanceof XmlResource) {
			serializer.writeAttribute("icon", ((XmlResource)icon).getValue()); //$NON-NLS-1$
		} else if(icon != null) {
			LoggerFactory.warning(XmlWriter.class, "Skipping serialization of icon for manifest: "+id); //$NON-NLS-1$
		}

		if(optionsManifest!=null) {
			XmlWriter.writeProperties(serializer, properties, optionsManifest);
			XmlWriter.writeOptionsManifestElement(serializer, optionsManifest);
		}
	}
}
