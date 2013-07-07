/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigConstants;
import net.ikarus_systems.icarus.config.ConfigRegistry.EntryType;
import net.ikarus_systems.icarus.plugins.ExtensionListCellRenderer;
import net.ikarus_systems.icarus.plugins.PluginUtil;

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
				ConfigConstants.RENDERER, new ExtensionListCellRenderer());
		builder.addBooleanEntry("exitWithoutPrompt", false); //$NON-NLS-1$
		builder.addBooleanEntry("sortPerspectivesByStatistics", true); //$NON-NLS-1$
		builder.setProperties(
				builder.addOptionsEntry("defaultPerspective", 0, collectAvailablePerspectives()), //$NON-NLS-1$
				ConfigConstants.RENDERER, new ExtensionListCellRenderer());
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
