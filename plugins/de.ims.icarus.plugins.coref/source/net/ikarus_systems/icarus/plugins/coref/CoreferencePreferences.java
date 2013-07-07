/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.coref;

import java.awt.Color;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigUtils;

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
		builder.addGroup("coref", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		
		builder.addColorEntry("background", Color.white.getRGB()); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder, "Tahoma"); //$NON-NLS-1$
	}
}
