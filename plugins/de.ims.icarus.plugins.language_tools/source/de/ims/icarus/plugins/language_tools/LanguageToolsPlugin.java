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

import de.ims.icarus.Core;

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
		
		// Ensure some default treebank files
		ClassLoader loader = LanguageToolsPlugin.class.getClassLoader();
		String[] resources = {
			"CoNLL2009-ST-English-development.txt", //$NON-NLS-1$
			"tiger_release_aug07.corrected.conll09_small.txt", //$NON-NLS-1$
		};
		String root = "de/ims/icarus/plugins/language_tools/resources/"; //$NON-NLS-1$
		String folder = "treebanks"; //$NON-NLS-1$
		
		for(String resource : resources) {
			String path = root+resource;
			Core.getCore().ensureResource(folder, resource, path, loader);
		}
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
