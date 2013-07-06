/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools;

import java.awt.Color;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigConstants;
import de.ims.icarus.config.ConfigRegistry.EntryType;
import de.ims.icarus.plugins.jgraph.JGraphPreferences;


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
		// CONSTRAINTS GRAPH GROUP
		builder.addGroup("constraints", true); //$NON-NLS-1$
		builder.addColorEntry("linkStrokeColor", Color.green.getRGB()); //$NON-NLS-1$
		builder.addIntegerEntry("linkStrokeWidth", 1, 1, 5); //$NON-NLS-1$
		JGraphPreferences.buildDefaultGraphConfig(builder);
		
		builder.reset();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// SEARCHTOOLS GROUP
		builder.addGroup("searchTools", true); //$NON-NLS-1$
		builder.addBooleanEntry("alwaysUseFallbackPresenter", false); //$NON-NLS-1$
		builder.addIntegerEntry("searchTimeout", 60); //$NON-NLS-1$
		builder.setProperties(builder.addIntegerEntry("maxCores", 0),  //$NON-NLS-1$
				ConfigConstants.NOTE_KEY, "config.searchTools.maxCores.note"); //$NON-NLS-1$
		builder.setProperties(builder.addListEntry("groupColors", EntryType.COLOR,  //$NON-NLS-1$
				Color.red.getRGB(), // red
				Color.green.getRGB(), // green
				Color.yellow.darker().getRGB()), // dark yellow
			ConfigConstants.MIN_ITEM_COUNT, 3);
	}

}
