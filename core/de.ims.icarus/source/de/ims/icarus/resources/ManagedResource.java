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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.resources;

import java.util.ResourceBundle;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class ManagedResource {
	
	private final String baseName;
	
	private final ResourceLoader loader;
	
	private ResourceBundle bundle;

	/**
	 * 
	 */
	ManagedResource(String baseName, ResourceLoader loader) {
		this.baseName = baseName;
		this.loader = loader;
	}

	public synchronized void reload() {
		bundle = loader.loadResource(baseName, ResourceManager.getInstance().getLocale());
	}
	
	synchronized void clear() {
		bundle = null;
	}
	
	public String getResource(String key) {
		if(bundle==null) {
			reload();
		}
		
		return bundle==null ? key : bundle.getString(key);
	}

	/**
	 * @return the name
	 */
	public String getBaseName() {
		return baseName;
	}

	/**
	 * @return the loader
	 */
	public ResourceLoader getLoader() {
		return loader;
	}

	/**
	 * @return the bundle
	 */
	public ResourceBundle getBundle() {
		return bundle;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return baseName.hashCode() *  loader.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ManagedResource) {
			ManagedResource other = (ManagedResource) obj;
			return baseName.equals(other.baseName) && loader.equals(other.loader);
		}
		
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return baseName;
	}
}
