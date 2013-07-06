/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.language_tools;


import org.java.plugin.Plugin;

import de.ims.icarus.resources.DefaultResourceLoader;
import de.ims.icarus.resources.ResourceLoader;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LanguageToolsPlugin extends Plugin {
	
	public static final String PLUGIN_ID = LanguageToolsConstants.LANGUAGE_TOOLS_PLUGIN_ID;

	public LanguageToolsPlugin() {
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
				"de.ims.icarus.plugins.language_tools.resources.language_tools", resourceLoader); //$NON-NLS-1$

		// Register our icons
		IconRegistry.getGlobalRegistry().addSearchPath(getClass().getClassLoader(), 
				"net/ikarus_systems/icarus/plugins/language_tools/icons/"); //$NON-NLS-1$
		
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
