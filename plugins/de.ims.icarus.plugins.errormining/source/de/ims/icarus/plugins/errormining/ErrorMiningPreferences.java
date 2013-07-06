/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining;

import java.awt.Color;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigUtils;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class ErrorMiningPreferences {
	public ErrorMiningPreferences() {
		ConfigBuilder builder = new ConfigBuilder();
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// JGRAPH GROUP
		builder.addGroup("errorMining", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		
		builder.addColorEntry("background", Color.white.getRGB()); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder, "Tahoma"); //$NON-NLS-1$
	}

}
