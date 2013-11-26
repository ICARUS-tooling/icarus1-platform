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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.config.ConfigDialog;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.util.ClassProxy;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.ErrorFormatter;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.LazyExtensionContentType;
import de.ims.icarus.xml.jaxb.JAXBUtils;

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
	protected synchronized void doStart() throws Exception {
		// Crucial condition: plug-in has to be activated for dependent plug-ins to acess its classes
		new Loader().start();
	}
	
	private void initConfig() {
		
		PluginUtil.loadAttributes(getDescriptor());
		
		for(PluginDescriptor descriptor : PluginUtil.getPluginRegistry().getDependingPlugins(getDescriptor())) {
			try {
				PluginUtil.loadAttributes(descriptor);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to process plug-in attributes: "+descriptor.getUniqueId(), e); //$NON-NLS-1$
			}
		}

		// Now load all config data from the storages
		try {
			ConfigRegistry.getGlobalRegistry().loadNow();
		} catch(Exception e) {
			LoggerFactory.log(PluginUtil.class, Level.SEVERE, 
					"Failed to load config data", e); //$NON-NLS-1$
		}
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
				ContentTypeRegistry.getInstance().addType(new LazyExtensionContentType(extension));
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
			
			for(Extension.Parameter param : extension.getParameters("class")) { //$NON-NLS-1$
				try {
					JAXBUtils.registerClass(PluginUtil.loadClass(param));
					
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to register serializable: "+param.getId()+" at extension "+extension.getUniqueId(), e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
			for(Extension.Parameter param : extension.getParameters("adapter")) { //$NON-NLS-1$
				try {
					Class<?> clazz = PluginUtil.loadClass(param);
					ClassLoader loader = PluginUtil.getClassLoader(extension);
					
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
		
		boolean showFrame = true;
		boolean hideDisclaimer = config.getBoolean("general.eula"); //$NON-NLS-1$
		if(!hideDisclaimer && !Core.getCore().getOptions().isDevMode()) {
			showFrame = DisclaimerDialog.showDialog();
		}
		
		// Show first frame
		if(showFrame) {
			FrameManager.getInstance().newFrame();
		} else {
			ShutdownDialog.getDialog().shutdown();
		}
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// no-op
	}
	
	public static boolean isShowExampleData() {
		return ConfigRegistry.getGlobalRegistry().getBoolean(
				"general.appearance.showExampleData"); //$NON-NLS-1$
	}
	
	private synchronized void doInit() throws Exception {
		// Delegate all uncaught exceptions to the default logging facility
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

		LoggerFactory.registerLogFile("de.ims.icarus.plugins", "icarus.plugins"); //$NON-NLS-1$ //$NON-NLS-2$

		// Register content types and converters
		registerContentTypes();

		// Register serializables before any other loading happens!
		// This is to enable proper use of the shared JAXBContext
		// by the config registry and all other facilities
		registerSerializables();
		
		// Build config
		initConfig();
		
		// Register ui-helper objects
		registerUIHelpers();
		
		// Register error formatters
		registerErrorFormatters();
		

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
	
	private class Loader extends Thread {
		
		Loader() {
			super("Icarus-core-loader"); //$NON-NLS-1$
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				doInit();
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to init core plug-in", e); //$NON-NLS-1$
			}
		}
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
			boolean exitWoP = ConfigRegistry.getGlobalRegistry().getBoolean(
					"general.appearance.exitWithoutPrompt"); //$NON-NLS-1$
			if(!exitWoP) {
				MutableBoolean result = new MutableBoolean(exitWoP);
				if(!DialogFactory.getGlobalFactory().showCheckedConfirm(
						null, result, 
						"plugins.core.icarusFrame.dialogs.confirmTitle",  //$NON-NLS-1$
						"plugins.core.icarusFrame.dialogs.confirmInfo",  //$NON-NLS-1$
						"plugins.core.icarusFrame.dialogs.confirmMessage")) { //$NON-NLS-1$
					return;
				}
				ConfigRegistry.getGlobalRegistry().setValue(
						"general.appearance.exitWithoutPrompt", result.getValue()); //$NON-NLS-1$
			}
			
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
