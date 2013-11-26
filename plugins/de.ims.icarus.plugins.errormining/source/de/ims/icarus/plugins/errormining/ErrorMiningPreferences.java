/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus G�rtner and Gregor Thiele
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
package de.ims.icarus.plugins.errormining;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.plugins.errormining.annotation.NGramHighlighting;


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
		// GENERAL ERRORMINING GROUP
		builder.addGroup("errorMining", true); //$NON-NLS-1$
		
		// APPEARANCE GROUP

		builder.addGroup("appearance", true); //$NON-NLS-1$
		
		ConfigUtils.buildDefaultFontConfig(builder, "Tahoma"); //$NON-NLS-1$
		
		builder.addStringEntry("filepath", "E:\\errormining_result.xml"); //$NON-NLS-1$ //$NON-NLS-2$
		builder.addStringEntry("inputfiledebug", "E:\\test_small_modded.txt"); //$NON-NLS-1$ //$NON-NLS-2$
		builder.addIntegerEntry("limit",10); //$NON-NLS-1$
		
		builder.addBooleanEntry("showOriginalIndex", true); //$NON-NLS-1$
		
		builder.back();
		// END APPEARANCE GROUP
		
		
		// HIGHLIGHTING GROUP
		builder.addGroup("highlighting", true); //$NON-NLS-1$
		
		for(String token : NGramHighlighting.getInstance().getTokens()) {
			builder.addColorEntry(token+"Highlight", NGramHighlighting.getInstance().getHighlightColor(token).getRGB()); //$NON-NLS-1$
		}

		
		builder.back();
		// END HIGHLIGHTING GROUP
	}

}
