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
package de.ims.icarus.ui.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import org.java.plugin.registry.Extension;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.Exceptions;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ActionBinder {

	private ActionBinder() {
		// no-op
	}
	
	private static final Map<String, Object> emptyProperties = Collections.emptyMap();
	
	public static Action loadAction(Object owner, Extension extension) throws Exception {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(extension, "extension"); //$NON-NLS-1$

		BindableAction action = (BindableAction) PluginUtil.instantiate(extension);
		
		// load properties
		Map<String, Object> properties = null;
		Extension.Parameter param = extension.getParameter("properties"); //$NON-NLS-1$
		if(param!=null) {
			properties = readProperties(param);
		}
		
		if(properties==null) {
			properties = emptyProperties;
		}
		
		// bind and return action
		return action.bind(owner, properties);
	}
	
	private static Map<String, Object> readProperties(Extension.Parameter root) {
		Map<String, Object> properties = new HashMap<>();
		
		for(Extension.Parameter param : root.getSubParameters()) {
			if(param.getSubParameters()!=null)
				properties.put(param.getId(), readProperties(param));
			else
				properties.put(param.getId(), param.rawValue());
		}
		
		return properties;
	}
}
