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

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.util.Comparator;
import java.util.logging.Logger;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.standard.StandardObjectFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class PluginUtil {
	
	public static final String CORE_PLUGIN_ID = "net.ikarus_systems.icarus.core"; //$NON-NLS-1$
	
	private static PluginRegistry pluginRegistry;
	private static PathResolver pathResolver;
	private static PluginManager pluginManager;

	private PluginUtil() {
		// no-op
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		throw new NotSerializableException(PluginUtil.class.getName());
	}
	
	// prevent cloning
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public static void load(Logger logger) throws Exception {
		if(pluginRegistry!=null)
			throw new IllegalStateException("Plug-in registry object already loaded"); //$NON-NLS-1$
		if(pathResolver!=null)
			throw new IllegalStateException("Path resolver object already loaded"); //$NON-NLS-1$
		if(pluginManager!=null)
			throw new IllegalStateException("Plug-in manager object already loaded"); //$NON-NLS-1$
		
		// init plu-gin management objects
		ObjectFactory objectFactory = StandardObjectFactory.newInstance();
		logger.info("Using object factory: "+objectFactory); //$NON-NLS-1$
		
		pluginRegistry = objectFactory.createRegistry();
		logger.info("Using plugin registry: "+pluginRegistry); //$NON-NLS-1$
		
		pathResolver = objectFactory.createPathResolver();
		logger.info("Using path resolver: "+pathResolver); //$NON-NLS-1$
		
		pluginManager = objectFactory.createManager(pluginRegistry, pathResolver);
		logger.info("Using plugin manager: "+pluginManager); //$NON-NLS-1$
	}
	
	public static PluginManager getPluginManager() {
		if(pluginManager==null)
			throw new IllegalStateException("Plug-in manager not yet loaded!"); //$NON-NLS-1$
		
		return pluginManager;
	}

	public static PluginRegistry getPluginRegistry() {
		if(pluginRegistry==null)
			throw new IllegalStateException("Plug-in registry not yet loaded!"); //$NON-NLS-1$
		return pluginRegistry;
	}

	public static PathResolver getPathResolver() {
		if(pathResolver==null)
			throw new IllegalStateException("Path resolver not yet loaded!"); //$NON-NLS-1$
		return pathResolver;
	}
	
	public static PluginDescriptor getCorePlugin() {
		return getPluginRegistry().getPluginDescriptor(CORE_PLUGIN_ID);
	}

	public static final Comparator<org.java.plugin.registry.Identity> IDENTITY_COMPARATOR = new Comparator<org.java.plugin.registry.Identity>() {
	
		@Override
		public int compare(org.java.plugin.registry.Identity o1,
				org.java.plugin.registry.Identity o2) {
			return o1.getId().compareTo(o2.getId());
		}
	
	};

	public static Extension findExtension(String pluginId, String extensionPointId, String uid) {
		PluginDescriptor descriptor = getPluginManager().getRegistry().getPluginDescriptor(pluginId);
		if(descriptor==null) {
			return null;
		}
		ExtensionPoint extensionPoint = descriptor.getExtensionPoint(extensionPointId);
		if(extensionPoint==null) {
			return null;
		}
		for(Extension extension : extensionPoint.getConnectedExtensions()) {
			if(extension.getUniqueId().equals(uid)) {
				return extension;
			}
		}
		return null;
	}
}
