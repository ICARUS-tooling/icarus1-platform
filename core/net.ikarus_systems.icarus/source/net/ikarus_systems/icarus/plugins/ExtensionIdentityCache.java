/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.util.id.ExtensionIdentity;
import net.ikarus_systems.icarus.util.id.Identity;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ExtensionIdentityCache {
	
	private static ExtensionIdentityCache instance;
	
	public static ExtensionIdentityCache getInstance() {
		if(instance==null) {
			synchronized (ExtensionIdentityCache.class) {
				if(instance==null) {
					instance = new ExtensionIdentityCache();
				}
			}
		}
		
		return instance;
	}
	
	private Map<Extension, Identity> cache;

	private ExtensionIdentityCache() {
		// no-op
	}

	public Identity getIdentity(Extension extension) {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		if(cache==null) {
			cache = Collections.synchronizedMap(new HashMap<Extension, Identity>());
		}
		
		Identity identity = cache.get(extension);
		if(identity==null) {
			synchronized (this) {
				if(!cache.containsKey(extension)) {
					identity = new ExtensionIdentity(extension);
					cache.put(extension, identity);
				} else {
					identity = cache.get(extension);
				}
			}
		}
		
		return identity;
	}
}
