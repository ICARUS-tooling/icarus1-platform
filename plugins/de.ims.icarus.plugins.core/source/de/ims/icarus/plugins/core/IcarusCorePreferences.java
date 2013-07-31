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
package de.ims.icarus.plugins.core;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigConstants;
import de.ims.icarus.config.ConfigRegistry.EntryType;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.PluginUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IcarusCorePreferences {

	public IcarusCorePreferences() {
		ConfigBuilder builder = new ConfigBuilder();
		
		// GENERAL GROUP
		builder.addGroup("general", true); //$NON-NLS-1$
		builder.addOptionsEntry("language", 0,  //$NON-NLS-1$
				"en", "de"); // TODO add more language options //$NON-NLS-1$ //$NON-NLS-2$
		builder.addEntry("workingDirectory", EntryType.FILE,  //$NON-NLS-1$
				new File(System.getProperty("user.dir")).getAbsolutePath()); //$NON-NLS-1$
		builder.addBooleanEntry("eula", false);  //$NON-NLS-1$
				
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		builder.addBooleanEntry("useSystemLaF", true); //$NON-NLS-1$
		builder.setProperties(
				builder.addOptionsEntry("lookAndFeel", 0, collectAvailableLookAndFeels()), //$NON-NLS-1$
				ConfigConstants.RENDERER, ExtensionListCellRenderer.getSharedInstance());
		builder.addBooleanEntry("exitWithoutPrompt", false); //$NON-NLS-1$
		builder.addBooleanEntry("sortPerspectivesByStatistics", true); //$NON-NLS-1$
		builder.setProperties(
				builder.addOptionsEntry("defaultPerspective", 0, collectAvailablePerspectives()), //$NON-NLS-1$
				ConfigConstants.RENDERER, ExtensionListCellRenderer.getSharedInstance());
		builder.addBooleanEntry("showSystemStreams", true); //$NON-NLS-1$
		builder.back();
		// END APPEARANCE GROUP
		
		builder.back();
		// END GENERAL GROUP
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// TODO add plugins options
		builder.back();
		// END PLUGINS GROUP
	}
	
	private Object[] collectAvailableLookAndFeels() {
		List<Object> items = new ArrayList<>();
		
		items.add("DEFAULT_LAF"); //$NON-NLS-1$
		items.add("javax.swing.plaf.basic.BasicLookAndFeel"); //$NON-NLS-1$
		items.add("javax.swing.plaf.metal.MetalLookAndFeel"); //$NON-NLS-1$
		items.add("javax.swing.plaf.nimbus.NimbusLookAndFeel"); //$NON-NLS-1$
		
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				PluginUtil.CORE_PLUGIN_ID, "UITheme"); //$NON-NLS-1$
		items.addAll(extensionPoint.getConnectedExtensions());
		
		return items.toArray();
	}
	
	private Object[] collectAvailablePerspectives() {
		Set<Object> items = new LinkedHashSet<>();
		
		items.add("NONE"); //$NON-NLS-1$

		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				PluginUtil.CORE_PLUGIN_ID, "Perspective"); //$NON-NLS-1$
		for(Extension extension : PluginUtil.findExtensions(extensionPoint, null)) {
			items.add(extension.getUniqueId());
		}
		
		return items.toArray();
	}

}
