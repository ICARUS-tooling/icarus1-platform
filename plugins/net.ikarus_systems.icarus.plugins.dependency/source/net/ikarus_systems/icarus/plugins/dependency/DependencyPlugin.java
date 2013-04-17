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
import net.ikarus_systems.icarus.ui.table.ColumnInfo;
import net.ikarus_systems.icarus.ui.table.ColumnListHandler;

import org.java.plugin.Plugin;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class DependencyPlugin extends Plugin {

	public DependencyPlugin() {
		// no-op
	}

	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		
		// Init config section
		initConfig();
	}
	
	private void initConfig() {
		ConfigBuilder builder = new ConfigBuilder(ConfigRegistry.getGlobalRegistry());
		builder.forward("plugins"); //$NON-NLS-1$
		
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

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}

}
