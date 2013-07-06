/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency;

import net.ikarus_systems.icarus.language.dependency.annotation.DependencyHighlighting;

import org.java.plugin.Plugin;

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
