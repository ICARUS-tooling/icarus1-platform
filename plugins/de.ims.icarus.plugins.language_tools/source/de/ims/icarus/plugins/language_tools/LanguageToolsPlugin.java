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
package de.ims.icarus.plugins.language_tools;


import org.java.plugin.Plugin;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LanguageToolsPlugin extends Plugin {
	
	public static final String PLUGIN_ID = LanguageToolsConstants.LANGUAGE_TOOLS_PLUGIN_ID;

	public LanguageToolsPlugin() {
		// no-op
	}
	
	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		
		// Ensure some default treebank files
		/*ClassLoader loader = LanguageToolsPlugin.class.getClassLoader();
		String[] resources = {
			"CoNLL2009-ST-English-development.txt", //$NON-NLS-1$
			"CoNLL2009-ST-Chinese-development.txt", //$NON-NLS-1$
			"tiger_release_aug07.corrected.conll09_small.txt", //$NON-NLS-1$
		};
		String root = "de/ims/icarus/plugins/language_tools/resources/"; //$NON-NLS-1$
		String folder = "treebanks"; //$NON-NLS-1$
		
		for(String resource : resources) {
			String path = root+resource;
			Core.getCore().ensureResource(folder, resource, path, loader);
		}*/
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
