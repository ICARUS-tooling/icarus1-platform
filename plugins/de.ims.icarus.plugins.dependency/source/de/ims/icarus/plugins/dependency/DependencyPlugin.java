/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.dependency;


import org.java.plugin.Plugin;

import de.ims.icarus.language.dependency.annotation.DependencyHighlighting;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class DependencyPlugin extends Plugin {

	public DependencyPlugin() {
		// no-op
	}

	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		DependencyHighlighting.loadConfig();
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}

}
