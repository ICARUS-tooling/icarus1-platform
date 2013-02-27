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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.EntryType;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.resources.DefaultResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.config.ConfigDialog;
import net.ikarus_systems.icarus.ui.helper.UIHelperRegistry;
import net.ikarus_systems.icarus.util.CorruptedStateException;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class IcarusCorePlugin extends Plugin {
	
	public static final String PLUGIN_ID = PluginUtil.CORE_PLUGIN_ID;

	private CallbackHandler callbackHandler;
	
	private Logger logger = LoggerFactory.getLogger(IcarusCorePlugin.class);
	
	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {

		LoggerFactory.registerLogFile("net.ikarus_systems.icarus.plugins", "icarus.plugins"); //$NON-NLS-1$ //$NON-NLS-2$

		// Make our resources accessible via the global domain
		ResourceLoader resourceLoader = new DefaultResourceLoader(
				getManager().getPluginClassLoader(getDescriptor()));
		ResourceManager.getInstance().addResource(
				"net.ikarus_systems.icarus.plugins.core.resources.core", resourceLoader); //$NON-NLS-1$

		// Register our icons
		IconRegistry.getGlobalRegistry().addSearchPath(getClass().getClassLoader(), 
				"net/ikarus_systems/icarus/plugins/core/icons/"); //$NON-NLS-1$
		
		// Define some global actions
		URL actionLocation = IcarusCorePlugin.class.getResource(
				"icarus-core-plugin-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: icarus-core-plugin-actions.xml"); //$NON-NLS-1$
		ActionManager actionManager = ActionManager.globalManager();
		try {
			actionManager.loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.getLogger(IcarusCorePlugin.class).log(LoggerFactory.record(
					Level.SEVERE, "Failed to load actions from file: "+actionLocation, e)); //$NON-NLS-1$
			throw e;
		}
		
		// Register global callbacks
		callbackHandler = new CallbackHandler();
		actionManager.addHandler("plugins.core.icarusCorePlugin.exitAction",  //$NON-NLS-1$
				callbackHandler, "exit"); //$NON-NLS-1$
		actionManager.addHandler("plugins.core.icarusCorePlugin.aboutAction",  //$NON-NLS-1$
				callbackHandler, "about"); //$NON-NLS-1$
		actionManager.addHandler("plugins.core.icarusCorePlugin.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$

		// Init config		
		ConfigBuilder builder = new ConfigBuilder(ConfigRegistry.getGlobalRegistry());
		
		// GENERAL GROUP
		builder.addGroup("general", true); //$NON-NLS-1$
		builder.addOptionsEntry("language", 0,  //$NON-NLS-1$
				"en", "de"); // TODO add more language options //$NON-NLS-1$ //$NON-NLS-2$
		builder.addEntry("workingDirectory", EntryType.FILE,  //$NON-NLS-1$
				new File(System.getProperty("user.dir")).getAbsolutePath()); //$NON-NLS-1$
		
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		builder.addBooleanEntry("useSystemLaF", true); //$NON-NLS-1$
		builder.addOptionsEntry("lookAndFeel", 0,  //$NON-NLS-1$
				"DEFAULT", //$NON-NLS-1$
				"javax.swing.plaf.basic.BasicLookAndFeel", //$NON-NLS-1$
				"javax.swing.plaf.metal.MetalLookAndFeel", //$NON-NLS-1$
				"javax.swing.plaf.nimbus.NimbusLookAndFeel"); //$NON-NLS-1$
		builder.addBooleanEntry("exitWithoutPrompt", false); //$NON-NLS-1$
		builder.back();
		// END APPEARANCE GROUP
		
		builder.back();
		// END GENERAL GROUP
		
		// Reload config data from storage
		ConfigRegistry.getGlobalRegistry().updateConfigTree(null);
		
		// Register ui-helper objects
		for(Extension extension : getDescriptor().getExtensionPoint("UIHelper").getConnectedExtensions()) { //$NON-NLS-1$
			try {
				// No need to use a special class-loader since the helper
				// interfaces should be globally accessible to all plug-ins
				// and preferably be hosted within the icarus core.
				Class<?> helperClass = Class.forName(extension.getParameter("interface").valueAsString()); //$NON-NLS-1$
				String objectClassName = extension.getParameter("target").valueAsString(); //$NON-NLS-1$
				
				// The registry already knows how to wrap extension objects
				UIHelperRegistry.globalRegistry().registerHelper(
						helperClass, objectClassName, extension);
			} catch(Exception e) {
				logger.log(LoggerFactory.record(Level.SEVERE, 
						"Failed to register ui-helper: "+extension.getUniqueId(), e)); //$NON-NLS-1$
			}
		}
		
		// Show ui elements
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					initAndShowGUI();
				} catch (Exception e) {
					logger.log(LoggerFactory.record(Level.SEVERE, 
							"Failed to init core plug-in interface", e)); //$NON-NLS-1$
				}
			}
		});
	}
	
	private void initAndShowGUI() throws Exception {
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		
		String lafClassName = null;
		
		// Load look&feel class name
		if(config.getBoolean("general.appearance.useSystemLaF")) { //$NON-NLS-1$
			lafClassName = UIManager.getSystemLookAndFeelClassName();
		} else {
			lafClassName = config.getString("general.appearance.lookAndFeel"); //$NON-NLS-1$
			if("DEFAULT".equals(lafClassName)) { //$NON-NLS-1$
				lafClassName = null;
			}
		}
		
		//Apply look&feel if required
		if(lafClassName!=null) {
			try {
				UIManager.setLookAndFeel(lafClassName);
			} catch(Exception e) {
				logger.log(LoggerFactory.record(Level.SEVERE, 
						"Failed to set up Look&Feel: "+lafClassName, e)); //$NON-NLS-1$
			}
		}
		
		// Show first frame
		FrameManager.getInstance().newFrame();
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}
	
	static void exit() {
		// TODO check for non-ui tools running and shut down entire
		// platform in case nothing is active
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public final class CallbackHandler  {
		
		private CallbackHandler() {
			// no-op
		}
		
		public void exit(ActionEvent e) {
			IcarusCorePlugin.exit();
		}
		
		public void about(ActionEvent e) {
			// TODO
		}
		
		public void openPreferences(ActionEvent e) {
			try {
				new ConfigDialog(ConfigRegistry.getGlobalRegistry()).setVisible(true);
			} catch(Exception ex) {
				logger.log(LoggerFactory.record(Level.SEVERE, 
						"Failed to show config dialog", ex)); //$NON-NLS-1$
			}
		}
	}
}
