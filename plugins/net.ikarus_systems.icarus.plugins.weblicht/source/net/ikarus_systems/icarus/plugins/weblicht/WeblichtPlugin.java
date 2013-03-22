package net.ikarus_systems.icarus.plugins.weblicht;

import net.ikarus_systems.icarus.resources.DefaultResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;

import org.java.plugin.Plugin;


/**
 * 
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WeblichtPlugin extends Plugin {

	/**
	 * 
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {

		// Make our resources accessible via the global domain
		ResourceLoader resourceLoader = new DefaultResourceLoader(
				getManager().getPluginClassLoader(getDescriptor()));
		ResourceManager.getInstance().addResource(
				"net.ikarus_systems.icarus.plugins.weblicht.resources.weblicht", resourceLoader); //$NON-NLS-1$
		
		// Register our icons
		IconRegistry.getGlobalRegistry().addSearchPath(getClass().getClassLoader(), 
				"net/ikarus_systems/icarus/plugins/weblicht/icons/"); //$NON-NLS-1$
	
	}

	/**
	 * 
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}

}
