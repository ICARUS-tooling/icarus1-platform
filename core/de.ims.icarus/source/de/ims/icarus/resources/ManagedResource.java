/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/resources/ManagedResource.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.resources;

import java.util.ResourceBundle;

/**
 * @author Markus Gärtner 
 * @version $Id: ManagedResource.java 7 2013-02-27 13:18:56Z mcgaerty $
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
