/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.jgraph.JGraphPlugin;
import net.ikarus_systems.icarus.resources.DefaultResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.SearchUtils;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.util.ClassProxy;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchToolPlugin extends Plugin {

	public SearchToolPlugin() {
		// no-op
	}

	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {

		// Make our resources accessible via the global domain
		ResourceLoader resourceLoader = new DefaultResourceLoader(
				getManager().getPluginClassLoader(getDescriptor()));
		ResourceManager.getInstance().addResource(
				"net.ikarus_systems.icarus.plugins.search_tools.resources.search_tools", resourceLoader); //$NON-NLS-1$

		// Register our icons
		IconRegistry.getGlobalRegistry().addSearchPath(getClass().getClassLoader(), 
				"net/ikarus_systems/icarus/plugins/search_tools/icons/"); //$NON-NLS-1$
		
		registerConstraintFactories();
		
		initConfig();
	}
	
	/**
	 * Load and register all content types that are defined at plug-in manifest level
	 */
	private void registerConstraintFactories() {
		for(Extension extension : getDescriptor().getExtensionPoint("ConstraintFactory").getConnectedExtensions()) { //$NON-NLS-1$
			try {
				// Collect ids
				List<String> ids = new ArrayList<>();				
				for(Extension.Parameter tokenParam : extension.getParameters("token")) { //$NON-NLS-1$
					ids.add(tokenParam.valueAsString());
				}
				
				Extension.Parameter contentTypeParam = extension.getParameter("contentType"); //$NON-NLS-1$
				ContentType contentType = ContentTypeRegistry.getInstance().getType(contentTypeParam.valueAsExtension());
				
				SearchUtils.registerConstraintFactory(ids.toArray(new String[0]), 
						new ClassProxy(extension), contentType);
				
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to register cosntraint factory: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
	}
	
	private void initConfig() {
		ConfigBuilder builder = new ConfigBuilder(ConfigRegistry.getGlobalRegistry());
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// JGRAPH GROUP
		builder.addGroup("jgraph", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// DEFAULT GROUP
		builder.addGroup("constraints", true); //$NON-NLS-1$
		// CONSTRAINTS GRAPH GROUP
		JGraphPlugin.buildDefaultGraphConfig(builder);
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}

}
