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
package de.ims.icarus.plugins.coref;

import java.awt.Color;
import java.util.ArrayList;

import org.java.plugin.registry.Extension;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigConstants;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.language.coref.CorefErrorType;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentHighlighting;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.coref.view.grid.ClusterLabelType;
import de.ims.icarus.plugins.jgraph.JGraphPreferences;
import de.ims.icarus.ui.list.TooltipListCellRenderer;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.HighlightType;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferencePreferences {

	public CoreferencePreferences() {
		ConfigBuilder builder = new ConfigBuilder();
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// JGRAPH GROUP
		builder.addGroup("jgraph", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// COREFERENCE GROUP
		builder.addGroup("coref", true); //$NON-NLS-1$
		builder.addBooleanEntry("markFalseNodes", true); //$NON-NLS-1$
		builder.addBooleanEntry("includeGoldNodes", false); //$NON-NLS-1$
		builder.addBooleanEntry("markFalseEdges", true); //$NON-NLS-1$
		builder.addBooleanEntry("includeGoldEdges", false); //$NON-NLS-1$
		builder.addBooleanEntry("filterSingletons", true); //$NON-NLS-1$
		Options options = new Options();
		options.put("gridEnabled", false); //$NON-NLS-1$
//		options.put("selectedVertexShape", 1); //$NON-NLS-1$
		options.put("vertexStrokeColor", Color.black.getRGB()); //$NON-NLS-1$
//		options.put("selectedVertexPerimeter", 1); //$NON-NLS-1$
		JGraphPreferences.buildDefaultGraphConfig(builder, options);
		builder.reset();
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// COREF GROUP
		builder.addGroup("coref", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		builder.setProperties(
				builder.addOptionsEntry("defaultDocumentPresenter", 0,  //$NON-NLS-1$
						collectPresenterExtensions()),
				ConfigConstants.RENDERER, ExtensionListCellRenderer.getSharedInstance());
		builder.addBooleanEntry("showRowIndex", true); //$NON-NLS-1$
		builder.addBooleanEntry("showSetIndex", false); //$NON-NLS-1$
		
		// ERROR COLORS SUBGROUP
		builder.addGroup("errorColors", true); //$NON-NLS-1$
		builder.virtual();
		builder.addColorEntry(CorefErrorType.FALSE_NEGATIVE_MENTION.getKey(), Color.green.getRGB());
		builder.addColorEntry(CorefErrorType.FALSE_POSITIVE_MENTION.getKey(), Color.red.getRGB());
		builder.addColorEntry(CorefErrorType.FOREIGN_CLUSTER_HEAD.getKey(), new Color(255, 127, 42).getRGB());
		builder.addColorEntry(CorefErrorType.HALLUCINATED_HEAD.getKey(), new Color(255, 85, 212).getRGB());
		builder.addColorEntry(CorefErrorType.INVALID_CLUSTER_START.getKey(), new Color(0, 255, 170).getRGB());
		builder.back();
		// END ERROR COLORS SUBGROUP
		
		// TEXT SUBGROUP
		builder.addGroup("text", true); //$NON-NLS-1$
		builder.virtual();
		builder.addColorEntry("background", Color.white.getRGB()); //$NON-NLS-1$
		builder.addColorEntry("clusterMarkup", Color.pink.getRGB()); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder, "Tahoma"); //$NON-NLS-1$
		builder.back();
		// END TEXT SUBGROUP
		
		// GRID SUBGROUP
		builder.addGroup("grid", true); //$NON-NLS-1$
		builder.virtual();
		builder.addColorEntry("background", Color.white.getRGB()); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder, "Tahoma"); //$NON-NLS-1$
		builder.addOptionsEntry("clusterLabelType", 3, (Object[])ClusterLabelType.values()); //$NON-NLS-1$
		builder.addBooleanEntry("usePatternLabel", true); //$NON-NLS-1$
		builder.addStringEntry("defaultLabelPattern", "$form$"); //$NON-NLS-1$ //$NON-NLS-2$
		builder.back();
		// END GRID SUBGROUP
		builder.back();
		// END APPEARANCE GROUP

		// HIGHLIGHTING GROUP
		builder.addGroup("highlighting", true); //$NON-NLS-1$
		builder.addBooleanEntry("showIndex", true); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("highlightType", 0,  //$NON-NLS-1$
				(Object[])HighlightType.values()),
				ConfigConstants.RENDERER, TooltipListCellRenderer.getSharedInstance());
		builder.setProperties(builder.addOptionsEntry("groupHighlightType", 0,  //$NON-NLS-1$
				(Object[])HighlightType.values()),
				ConfigConstants.RENDERER, TooltipListCellRenderer.getSharedInstance());
		builder.addBooleanEntry("markMultipleAnnotations", true); //$NON-NLS-1$
		builder.addColorEntry("nodeHighlight", CoreferenceDocumentHighlighting.getInstance().getNodeHighlightColor().getRGB()); //$NON-NLS-1$
		builder.addColorEntry("edgeHighlight", CoreferenceDocumentHighlighting.getInstance().getEdgeHighlightColor().getRGB()); //$NON-NLS-1$
		builder.addColorEntry("transitiveHighlight", CoreferenceDocumentHighlighting.getInstance().getTransitiveHighlightColor().getRGB()); //$NON-NLS-1$
		for(String token : CoreferenceDocumentHighlighting.getInstance().getTokens()) {
			builder.addColorEntry(token+"Highlight", CoreferenceDocumentHighlighting.getInstance().getHighlightColor(token).getRGB()); //$NON-NLS-1$
		}
		builder.back();
		// END HIGHLIGHTING GROUP
		
		CoreferenceDocumentHighlighting.getInstance().loadConfig();
	}
	
	private Object[] collectPresenterExtensions() {
		java.util.List<Object> items = new ArrayList<>();
		
		for(Extension extension : CoreferencePlugin.getCoreferencePresenterExtensions()) {
			items.add(extension.getUniqueId());
		}
		
		return items.toArray();
	}
}
