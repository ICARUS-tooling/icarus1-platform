/*
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

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.plugins.jgraph.JGraphPreferences;
import de.ims.icarus.util.Options;


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
		builder.addBooleanEntry("markFalseEdges", true); //$NON-NLS-1$
		builder.addBooleanEntry("showGoldEdges", false); //$NON-NLS-1$
		builder.addColorEntry("falseEdgeColor", Color.red.getRGB()); //$NON-NLS-1$
		builder.addBooleanEntry("filterSingletons", true); //$NON-NLS-1$
		Options options = new Options();
		options.put("gridEnabled", false); //$NON-NLS-1$
		options.put("selectedVertexShape", 1); //$NON-NLS-1$
		options.put("vertexStrokeColor", Color.black.getRGB()); //$NON-NLS-1$
		options.put("selectedVertexPerimeter", 1); //$NON-NLS-1$
		JGraphPreferences.buildDefaultGraphConfig(builder, options);
		builder.reset();
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// COREF GROUP
		builder.addGroup("coref", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		
		builder.addColorEntry("background", Color.white.getRGB()); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder, "Tahoma"); //$NON-NLS-1$
	}
}
