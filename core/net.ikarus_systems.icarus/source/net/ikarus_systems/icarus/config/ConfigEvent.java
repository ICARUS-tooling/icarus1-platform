/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/config/ConfigEvent.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.config;

import java.util.Hashtable;
import java.util.Map;

import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;

/**
 * 
 * @author Markus Gärtner
 * @version $Id: ConfigEvent.java 23 2013-04-17 12:39:04Z mcgaerty $
 *
 */
public class ConfigEvent {
	
	public static final String VALUE_CHANGE = "value"; //$NON-NLS-1$
	public static final String DEFAULT_VALUE_CHANGE = "defaultValue"; //$NON-NLS-1$
	
	public static final String ITEM_UPDATED = "updated";	 //$NON-NLS-1$
	public static final String ITEM_ADDED = "added"; //$NON-NLS-1$
	public static final String ITEM_MODIFIED = "modified"; //$NON-NLS-1$
	public static final String ITEM_DELETED = "deleted"; //$NON-NLS-1$
	public static final String ITEM_MOVED = "moved"; //$NON-NLS-1$

	protected String name;

	protected Map<String, Object> properties;

	public ConfigEvent(String name) {
		this(name, (Object[]) null);
	}

	public ConfigEvent(String name, Object... args) {
		this.name = name;
		properties = new Hashtable<String, Object>();

		if (args != null) {
			for (int i = 0; i < args.length; i += 2) {
				if (args[i + 1] != null) {
					properties.put(String.valueOf(args[i]), args[i + 1]);
				}
			}
		}
	}
	
	public Handle getHandle() {
		return (Handle) getProperty("handle"); //$NON-NLS-1$
	}
	
	@Override
	public String toString() {
		return String.format("ConfigEvent: name=%s handle=%s", name, getProperty("handle")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getName() {
		return name;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}
}
