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
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigConstants;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.EntryType;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.ExtensionListCellRenderer;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.resources.DefaultResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.config.ConfigDialog;
import net.ikarus_systems.icarus.ui.helper.UIHelperRegistry;
import net.ikarus_systems.icarus.util.ClassProxy;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.ErrorFormatter;
import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.data.ExtensionContentType;
import net.ikarus_systems.icarus.xml.jaxb.JAXBUtils;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class IcarusCorePlugin extends Plugin {
	
	public static final String PLUGIN_ID = PluginUtil.CORE_PLUGIN_ID;

	private CallbackHandler callbackHandler;
	
	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		// Delegate all uncaught exceptions to the default logging facility
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

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
			LoggerFactory.log(this, Level.SEVERE, "Failed to load actions from file: "+actionLocation, e); //$NON-NLS-1$
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
		
		// Register serializables before any other loading happens!
		// This is to enable proper use of the shared JAXBContext
		// by the config registry and all other facilities
		registerSerializables();

		// Init config
		initConfig();
		
		// Reload config data from storage
		ConfigRegistry.getGlobalRegistry().updateConfigTree(null);
		
		// Register content types
		registerContentTypes();
		
		// Register ui-helper objects
		registerUIHelpers();
		
		// Register error formatters
		registerErrorFormatters();
		
		// Show ui elements
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					initAndShowGUI();
				} catch (Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to init core plug-in interface", e); //$NON-NLS-1$
				}
			}
		});
	}
	
	private void registerUIHelpers() {
		for(Extension extension : getDescriptor().getExtensionPoint("UIHelper").getConnectedExtensions()) { //$NON-NLS-1$
			try {
				boolean override = false;
				try {
					Extension.Parameter param = extension.getParameter("override"); //$NON-NLS-1$
					override = param==null ? false : param.valueAsBoolean();
				} catch(IllegalArgumentException e) {
					// ignore
				}
				
				// No need to use a special class-loader since the helper
				// interfaces should be globally accessible to all plug-ins
				// and preferably be hosted within the icarus core.
				// UPDATE: We allow to host helper interfaces in external plug-ins!
				Collection<Extension.Parameter> interfaceParams = extension.getParameters("interface"); //$NON-NLS-1$
				for(Extension.Parameter param : interfaceParams) {
					String helperClass = param.valueAsString();
					Extension contentTypeExtension = extension.getParameter("contentType").valueAsExtension(); //$NON-NLS-1$
					String contentTypeId = ContentTypeRegistry.getContentTypeId(contentTypeExtension);
					
					// The registry already knows how to wrap extension objects
					UIHelperRegistry.globalRegistry().registerHelper(
							helperClass, contentTypeId, extension, override);
				}
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to register ui-helper: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Load and register all content types that are defined at plug-in manifest level
	 */
	private void registerContentTypes() {
		for(Extension extension : getDescriptor().getExtensionPoint("ContentType").getConnectedExtensions()) { //$NON-NLS-1$
			try {
				ContentTypeRegistry.getInstance().addType(new ExtensionContentType(extension));
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to register content type: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Load and register all {@code ErrorFormatter} extensions.
	 */
	private void registerErrorFormatters() {
		for(Extension extension : getDescriptor().getExtensionPoint("ErrorFormatter").getConnectedExtensions()) { //$NON-NLS-1$
			ErrorFormatter formatter = null;
			try {
				formatter = (ErrorFormatter) PluginUtil.instantiate(extension);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to instantiate error formatter: "+extension.getUniqueId(), e); //$NON-NLS-1$
				continue;
			}
			
			for(Extension.Parameter param : extension.getParameters("throwableClass")) { //$NON-NLS-1$
				try {
					Exceptions.addFormatter(param.valueAsString(), formatter);
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to register formatter: "+extension.getUniqueId()+" for throwable "+param.valueAsString(), e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}
	
	/**
	 * Load and register all classes associated with the JAXB-framework.
	 */
	private void registerSerializables() {
		for(Extension extension : getDescriptor().getExtensionPoint("Serializable").getConnectedExtensions()) { //$NON-NLS-1$
			ClassLoader loader = PluginUtil.getClassLoader(extension);
			
			for(Extension.Parameter param : extension.getParameters("class")) { //$NON-NLS-1$
				try {
					Class<?> clazz = loader.loadClass(param.valueAsString());
					JAXBUtils.registerClass(clazz);
					
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to register serializable: "+param.getId()+" at extension "+extension.getUniqueId(), e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
			for(Extension.Parameter param : extension.getParameters("adapter")) { //$NON-NLS-1$
				try {
					Class<?> clazz = loader.loadClass(param.valueAsString());
					
					for(Extension.Parameter subParam : param.getSubParameters("class")) { //$NON-NLS-1$
						JAXBUtils.registerAdapter(clazz, new ClassProxy(
								subParam.valueAsString(), loader));
					}
					
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to register adapter: "+param.getId()+" at extension "+extension.getUniqueId(), e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}
	
	private void initConfig() {
		ConfigBuilder builder = new ConfigBuilder();
		
		// GENERAL GROUP
		builder.addGroup("general", true); //$NON-NLS-1$
		builder.addOptionsEntry("language", 0,  //$NON-NLS-1$
				"en", "de"); // TODO add more language options //$NON-NLS-1$ //$NON-NLS-2$
		builder.addEntry("workingDirectory", EntryType.FILE,  //$NON-NLS-1$
				new File(System.getProperty("user.dir")).getAbsolutePath()); //$NON-NLS-1$
		
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
		builder.back();
		// END APPEARANCE GROUP
		
		builder.back();
		// END GENERAL GROUP
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// TODO add plugins options
		builder.back();
		// END PLUGINS GROUP
		
		try {
			ConfigRegistry.getGlobalRegistry().loadNow();
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load config data", e); //$NON-NLS-1$
		}
	}
	
	private void initAndShowGUI() throws Exception {
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		
		String lafClassName = null;
		
		// Load look&feel class name
		if(config.getBoolean("general.appearance.useSystemLaF")) { //$NON-NLS-1$
			lafClassName = UIManager.getSystemLookAndFeelClassName();
		} else {
			Object lafValue = config.getValue("general.appearance.lookAndFeel"); //$NON-NLS-1$
			if("DEFAULT_LAF".equals(lafClassName)) { //$NON-NLS-1$
				lafClassName = null;
			} else if(lafValue instanceof String) {
				lafClassName = (String)lafValue;
			} else if(lafValue instanceof Extension) {
				Extension lafExtension = (Extension)lafValue;
				lafClassName = lafExtension.getParameter("class").valueAsString(); //$NON-NLS-1$
			}
		}
		
		//Apply look&feel if required
		if(lafClassName!=null) {
			try {
				UIManager.setLookAndFeel(lafClassName);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to set up Look&Feel: "+lafClassName, e); //$NON-NLS-1$
			}
		}
		
		// Show first frame
		FrameManager.getInstance().newFrame();
	}
	
	private Object[] collectAvailableLookAndFeels() {
		List<Object> items = new ArrayList<>();
		
		items.add("DEFAULT_LAF"); //$NON-NLS-1$
		items.add("javax.swing.plaf.basic.BasicLookAndFeel"); //$NON-NLS-1$
		items.add("javax.swing.plaf.metal.MetalLookAndFeel"); //$NON-NLS-1$
		items.add("javax.swing.plaf.nimbus.NimbusLookAndFeel"); //$NON-NLS-1$
		
		ExtensionPoint extensionPoint = getDescriptor().getExtensionPoint("UITheme"); //$NON-NLS-1$
		items.addAll(extensionPoint.getConnectedExtensions());
		
		return items.toArray();
	}
	
	private Object[] collectAvailablePerspectives() {
		Set<Object> items = new LinkedHashSet<>();
		
		items.add("NONE"); //$NON-NLS-1$
		
		ExtensionPoint extensionPoint = getDescriptor().getExtensionPoint("Perspective"); //$NON-NLS-1$
		items.addAll(PluginUtil.findExtensions(extensionPoint, null));
		
		return items.toArray();
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

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
			ShutdownDialog.getDialog().shutdown();
		}
		
		public void about(ActionEvent e) {
			try {
				AboutDialog.showDialog(null);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to display about-screen", ex); //$NON-NLS-1$
			}
		}
		
		public void openPreferences(ActionEvent e) {
			try {
				new ConfigDialog(ConfigRegistry.getGlobalRegistry()).setVisible(true);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to show config dialog", ex); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private static class ExceptionHandler implements UncaughtExceptionHandler {

		/**
		 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
		 */
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Uncaught exception on thread "+t.getName(), e); //$NON-NLS-1$
		}
		
	}
}
