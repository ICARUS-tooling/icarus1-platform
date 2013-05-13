/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.layout;

/**
 * Provides a default implementation of {@code GraphRenderer}
 * that uses the original methods as defined in the abstract class.
 * This implementation exists primarily as a target for an extension
 * definition.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultGraphRenderer extends GraphRenderer {

	public DefaultGraphRenderer() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(GraphOwner target) {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(GraphOwner target) {
		// no-op
	}

}
