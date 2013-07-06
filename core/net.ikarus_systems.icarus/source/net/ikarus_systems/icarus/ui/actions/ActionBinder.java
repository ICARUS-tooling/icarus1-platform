/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/ui/actions/ActionBinder.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.ui.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.util.Exceptions;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id: ActionBinder.java 23 2013-04-17 12:39:04Z mcgaerty $
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

		// fetch lcass information
		Extension.Parameter param = extension.getParameter("class"); //$NON-NLS-1$
		if(param==null)
			throw new IllegalArgumentException("Extension does not define 'class' parameter"); //$NON-NLS-1$
		
		// load and instantiate bindable action
		ClassLoader loader = PluginUtil.getClassLoader(extension);
		BindableAction action = (BindableAction) loader.loadClass(param.valueAsString()).newInstance();
		
		// load properties
		Map<String, Object> properties = null;
		param = extension.getParameter("properties"); //$NON-NLS-1$
		if(param!=null)
			properties = readProperties(param);
		
		if(properties==null)
			properties = emptyProperties;
		
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
