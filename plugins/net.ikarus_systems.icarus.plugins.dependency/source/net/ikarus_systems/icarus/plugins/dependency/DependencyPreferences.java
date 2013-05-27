/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigConstants;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.EntryType;
import net.ikarus_systems.icarus.plugins.jgraph.JGraphPreferences;
import net.ikarus_systems.icarus.ui.table.ColumnInfo;
import net.ikarus_systems.icarus.ui.table.ColumnListHandler;

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
		JGraphPreferences.buildDefaultGraphConfig(builder);
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
		
		builder.back();
		// END GENERAL DEPENDENCY GROUP
	}

}
