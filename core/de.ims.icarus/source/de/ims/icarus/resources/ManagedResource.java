/*
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
	
	private final ResourceLoader laoder;
	
	private ResourceBundle bundle;

	/**
	 * 
	 */
	ManagedResource(String baseName, ResourceLoader loader) {
		this.baseName = baseName;
		this.laoder = loader;
	}

	public synchronized void reload() {
		bundle = laoder.loadResource(baseName, ResourceManager.getInstance().getLocale());
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
	 * @return the laoder
	 */
	public ResourceLoader getLaoder() {
		return laoder;
	}

	/**
	 * @return the bundle
	 */
	public ResourceBundle getBundle() {
		return bundle;
	}
}
