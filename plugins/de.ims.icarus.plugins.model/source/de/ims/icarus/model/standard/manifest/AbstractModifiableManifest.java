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

import de.ims.icarus.model.api.manifest.Documentation;
import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.api.manifest.ModifiableManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractModifiableManifest<T extends ModifiableManifest> extends AbstractDerivable<T> implements ModifiableManifest {

	private Map<String, Object> properties;
	private OptionsManifest optionsManifest;
	private Documentation documentation;

	protected AbstractModifiableManifest(ManifestSource manifestSource,
			CorpusRegistry registry) {
		super(manifestSource, registry);
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write documentation
		writeEmbedded(documentation, serializer);

		// Write options manifest
		writeEmbedded(optionsManifest, serializer);

		if(properties!=null && !properties.isEmpty()) {
			List<String> names = CollectionUtils.asSortedList(properties.keySet());

			for(String name : names) {
				Object value = properties.get(name);
				ValueType type = optionsManifest==null ?
						ValueType.STRING : optionsManifest.getOption(name).getValueType();

				ModelXmlUtils.writePropertyElement(serializer, name, value, type);
			}
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
	@Override
	public void setDocumentation(Documentation documentation) {
		this.documentation = documentation;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ModifiableManifest#getOptionsManifest()
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
}
