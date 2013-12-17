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
package de.ims.icarus.plugins.dependency;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigConstants;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.EntryType;
import de.ims.icarus.language.dependency.annotation.DependencyHighlighting;
import de.ims.icarus.plugins.jgraph.JGraphPreferences;
import de.ims.icarus.ui.list.TooltipListCellRenderer;
import de.ims.icarus.ui.table.ColumnInfo;
import de.ims.icarus.ui.table.ColumnListHandler;
import de.ims.icarus.util.annotation.HighlightType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyPreferences {

	public DependencyPreferences() {
		ConfigBuilder builder = new ConfigBuilder(ConfigRegistry.getGlobalRegistry());

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// JGRAPH GROUP
		builder.addGroup("jgraph", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// DEFAULT GROUP
		builder.addGroup("dependency", true); //$NON-NLS-1$
		// DEPENDENCY GRAPH GROUP
		builder.addBooleanEntry("showIndex", true); //$NON-NLS-1$
		builder.addBooleanEntry("showLemma", true); //$NON-NLS-1$
		builder.addBooleanEntry("showFeatures", true); //$NON-NLS-1$
		builder.addBooleanEntry("showForm", true); //$NON-NLS-1$
		builder.addBooleanEntry("showPos", true); //$NON-NLS-1$
		builder.addBooleanEntry("showRelation", true); //$NON-NLS-1$
		builder.addBooleanEntry("showDirection", false); //$NON-NLS-1$
		builder.addBooleanEntry("showDistance", false); //$NON-NLS-1$
		builder.addBooleanEntry("markRoot", true); //$NON-NLS-1$
		builder.addBooleanEntry("markNonProjective", false); //$NON-NLS-1$
		JGraphPreferences.buildDefaultGraphConfig(builder, null);
		builder.reset();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// GENERAL DEPENDENCY GROUP
		builder.addGroup("dependency", true); //$NON-NLS-1$

		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		builder.setProperties(builder.addListEntry("tableColumns", EntryType.CUSTOM,  //$NON-NLS-1$
			new ColumnInfo("plugins.dependency.captions.index", true, 10, 60, 30, true, true), //$NON-NLS-1$
			new ColumnInfo("plugins.dependency.captions.form", true, 30, 200, 70, true, true), //$NON-NLS-1$
			new ColumnInfo("plugins.dependency.captions.lemma", true, 10, 60, 30, true, false), //$NON-NLS-1$
			new ColumnInfo("plugins.dependency.captions.features", true, 10, 60, 30, true, false), //$NON-NLS-1$
			new ColumnInfo("plugins.dependency.captions.pos", true, 10, 60, 30, true, false), //$NON-NLS-1$
			new ColumnInfo("plugins.dependency.captions.head", true, 10, 60, 30, true, false), //$NON-NLS-1$
			new ColumnInfo("plugins.dependency.captions.relation", true, 10, 60, 30, true, false)), //$NON-NLS-1$
				ConfigConstants.HANDLER, new ColumnListHandler());
		builder.back();
		// END APPEARANCE GROUP

		// HIGHLIGHTING GROUP
		builder.addGroup("highlighting", true); //$NON-NLS-1$
//		builder.addBooleanEntry("showIndex", true); //$NON-NLS-1$
//		builder.addBooleanEntry("showCorpusIndex", false); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("highlightType", 0,  //$NON-NLS-1$
				(Object[])HighlightType.values()),
				ConfigConstants.RENDERER, TooltipListCellRenderer.getSharedInstance());
		builder.setProperties(builder.addOptionsEntry("groupHighlightType", 0,  //$NON-NLS-1$
				(Object[])HighlightType.values()),
				ConfigConstants.RENDERER, TooltipListCellRenderer.getSharedInstance());
		builder.addBooleanEntry("markMultipleAnnotations", true); //$NON-NLS-1$
		builder.addColorEntry("nodeHighlight", DependencyHighlighting.getInstance().getNodeHighlightColor().getRGB()); //$NON-NLS-1$
		builder.addColorEntry("edgeHighlight", DependencyHighlighting.getInstance().getEdgeHighlightColor().getRGB()); //$NON-NLS-1$
		builder.addColorEntry("transitiveHighlight", DependencyHighlighting.getInstance().getTransitiveHighlightColor().getRGB()); //$NON-NLS-1$
		for(String token : DependencyHighlighting.getInstance().getTokens()) {
			builder.addColorEntry(token+"Highlight", DependencyHighlighting.getInstance().getHighlightColor(token).getRGB()); //$NON-NLS-1$
		}
		builder.back();
		// END HIGHLIGHTING GROUP

		builder.back();
		// END GENERAL DEPENDENCY GROUP

		DependencyHighlighting.getInstance().loadConfig();
	}

}
