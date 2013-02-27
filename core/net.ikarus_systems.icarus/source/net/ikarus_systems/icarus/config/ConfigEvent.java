/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.config;

import java.util.Hashtable;
import java.util.Map;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
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
