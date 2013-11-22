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
import java.util.Set;

import javax.swing.Icon;

import de.ims.icarus.language.model.manifest.Manifest;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.Identity;

/**
 * 
 * This class is not thread-safe!
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractManifest implements Manifest {
	
	private Identity identity;
	private Map<String, Object> properties;

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return identity==null ? null : identity.getId();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return identity==null ? null : identity.getIcon();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return identity==null ? null : identity.getOwner();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getName()
	 */
	@Override
	public String getName() {
		return identity==null ? null : identity.getName();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getDescription()
	 */
	@Override
	public String getDescription() {
		return identity==null ? null : identity.getDescription();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String name) {
		return properties==null ? null : properties.get(name);
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

	public void setIdentity(Identity identity) {
		if(identity==null)
			throw new NullPointerException("Invalid identity"); //$NON-NLS-1$
		
		this.identity = identity;
	}
	
	public void setProperty(String key, Object value) {
		if(key==null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$
		
		if(properties==null) {
			properties = new HashMap<>();
		}
		
		properties.put(key, value);
	}
}
