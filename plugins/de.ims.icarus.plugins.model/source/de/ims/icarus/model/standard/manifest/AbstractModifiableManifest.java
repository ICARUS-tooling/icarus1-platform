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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.model.api.manifest.Documentation;
import de.ims.icarus.model.api.manifest.ModifiableManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
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

	/**
	 * @return the documentation
	 */
	@Override
	public Documentation getDocumentation() {
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
		return properties==null ? null : properties.get(name);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getPropertyNames()
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
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#copyFrom(de.ims.icarus.model.api.manifest.Derivable)
	 */
	@Override
	protected void copyFrom(T template) {
		optionsManifest = template.getOptionsManifest();
		documentation = template.getDocumentation();

		for(String key : template.getPropertyNames()) {
			setProperty(key, template.getProperty(key));
		}
	}
}
