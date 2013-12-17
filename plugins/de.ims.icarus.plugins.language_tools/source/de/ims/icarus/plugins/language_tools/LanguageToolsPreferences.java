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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.language_tools;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LanguageToolsPreferences {

	public LanguageToolsPreferences() {
		ConfigBuilder builder = new ConfigBuilder(ConfigRegistry.getGlobalRegistry());

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// GENERAL DEPENDENCY GROUP
		builder.addGroup("languageTools", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$

		builder.addBooleanEntry("showIndex", true); //$NON-NLS-1$
		builder.addBooleanEntry("showCorpusIndex", false); //$NON-NLS-1$
	}

}
