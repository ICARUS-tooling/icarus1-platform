/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.tcf;

import org.java.plugin.Plugin;

/**
 * 
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class TCFPlugin extends Plugin {

	public TCFPlugin() {
		// no-op
	}

	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		
		// Ensure some default treebank files
		/*ClassLoader loader = TCFPlugin.class.getClassLoader();
		String[] resources = {
			"tcf04-karin-wl.xml", //$NON-NLS-1$
		};
		String root = "de/ims/icarus/plugins/tcf/resources/"; //$NON-NLS-1$
		String folder = "treebanks"; //$NON-NLS-1$
		
		for(String resource : resources) {
			String path = root+resource;
			Core.getCore().ensureResource(folder, resource, path, loader);
		}*/
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}

}
