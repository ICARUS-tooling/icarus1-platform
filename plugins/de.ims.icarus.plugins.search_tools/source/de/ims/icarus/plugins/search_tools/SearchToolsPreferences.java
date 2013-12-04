/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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
		JGraphPreferences.buildDefaultGraphConfig(builder, null);
		
		builder.reset();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// SEARCHTOOLS GROUP
		builder.addGroup("searchTools", true); //$NON-NLS-1$
		builder.addBooleanEntry("alwaysUseFallbackPresenter", false); //$NON-NLS-1$
		builder.addBooleanEntry("alwaysUnifyNonAggregatedConstraints", false); //$NON-NLS-1$
		builder.addIntegerEntry("searchTimeout", 0); //$NON-NLS-1$
		builder.setProperties(builder.addIntegerEntry("maxCores", 0),  //$NON-NLS-1$
				ConfigConstants.NOTE_KEY, "config.searchTools.maxCores.note"); //$NON-NLS-1$
		builder.setProperties(builder.addListEntry("groupColors", EntryType.COLOR,  //$NON-NLS-1$
				Color.red.getRGB(), // red
				Color.green.getRGB(), // green
				Color.yellow.darker().getRGB()), // dark yellow
			ConfigConstants.MIN_ITEM_COUNT, 3);
	}

}
