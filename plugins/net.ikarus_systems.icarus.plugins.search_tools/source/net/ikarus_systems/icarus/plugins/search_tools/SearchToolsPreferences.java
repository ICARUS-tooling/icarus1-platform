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

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.plugins.jgraph.JGraphPreferences;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchToolsPreferences {

	public SearchToolsPreferences() {
		ConfigBuilder builder = new ConfigBuilder();
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// JGRAPH GROUP
		builder.addGroup("jgraph", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// DEFAULT GROUP
		builder.addGroup("constraints", true); //$NON-NLS-1$
		// CONSTRAINTS GRAPH GROUP
		JGraphPreferences.buildDefaultGraphConfig(builder);
		
		builder.reset();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// SEARCHTOOLS GROUP
		builder.addGroup("searchTools", true); //$NON-NLS-1$
	}

}
