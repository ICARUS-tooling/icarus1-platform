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
package de.ims.icarus.config;

import java.util.Hashtable;
import java.util.Map;

import de.ims.icarus.config.ConfigRegistry.Handle;


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
