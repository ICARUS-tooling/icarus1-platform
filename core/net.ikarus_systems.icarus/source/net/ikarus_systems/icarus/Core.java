/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */

/**
 *
 */
package net.ikarus_systems.icarus;

import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;

import org.java.plugin.JpfException;
import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.IntegrityCheckReport;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;

/**
 * @author Markus Gärtner 
 *
 */
public class Core {
	
	private static Core core;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if(core!=null)
				throw new IllegalStateException("Core already started!"); //$NON-NLS-1$
			
			core = new Core(args);
			
			// collect plugins and verify integrity
			core.collectPlugins();
			
			// launch core plug-in
			core.launchCorePlugin();
		} catch(Throwable e) {
			new CoreErrorDialog(e).setVisible(true);
		}
	}
	
	private final Logger logger;
	
	private final File rootFolder;
	private final File logFolder;
	private final File pluginFolder;
	private final File dataFolder;
	private final File tempFolder;
	
	private static final String ICARUS_CORE_PLUGIN = 
			"net.ikarus_systems.icarus.core"; //$NON-NLS-1$

	private Core(String[] args) {
		logger = Logger.getLogger("icarus.launcher"); //$NON-NLS-1$
		
		// init folders
		rootFolder = new File(System.getProperty("user.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// init log folder
		logFolder = new File(rootFolder, "logs"); //$NON-NLS-1$
		if(!logFolder.isDirectory() && !logFolder.mkdir())
			throw new Error("Unable to create logging directory"); //$NON-NLS-1$
		
		// init plugins folder
		pluginFolder = new File(rootFolder, "plugins"); //$NON-NLS-1$
		if(!pluginFolder.isDirectory() && !pluginFolder.mkdir())
			throw new Error("Unable to create plugins directory"); //$NON-NLS-1$
		
		// init data folder
		dataFolder = new File(rootFolder, "data"); //$NON-NLS-1$
		if(!dataFolder.isDirectory() && !dataFolder.mkdir())
			throw new Error("Unable to create data directory"); //$NON-NLS-1$

		// init temp folder
		tempFolder = new File(rootFolder, "temp"); //$NON-NLS-1$
		if(!tempFolder.isDirectory() && !tempFolder.mkdir())
			throw new Error("Unable to create temp directory"); //$NON-NLS-1$

		// Init default log file
		File logFile = new File(logFolder, "launcher.log"); //$NON-NLS-1$
		try {
			logger.addHandler(new FileHandler(logFile.getPath()));
		} catch (SecurityException e) {
			throw new Error("Unable to define log file: "+logFile.getAbsolutePath(), e); //$NON-NLS-1$
		} catch (IOException e) {
			throw new Error("Unable to access log file: "+logFile.getAbsolutePath(), e); //$NON-NLS-1$
		}
		
		// From here on we can use our logger
		try {
			PluginUtil.load(logger);
		} catch (Exception e) {
			throw new Error("Failed to load plug-in framework objects", e); //$NON-NLS-1$
		}
		
		// XXX
		//PluginUtil.getPluginManager().registerListener(new PluginManagerLog());
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		throw new NotSerializableException(Core.class.getName());
	}
	
	// prevent cloning
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private void collectPlugins() {
		List<PluginLocation> pluginLocations = new LinkedList<>();
		logger.fine("Collecting plug-ins"); //$NON-NLS-1$
		processFolder(pluginFolder, pluginLocations);
		logger.info(String.format("Collected %d plug-ins", pluginLocations.size())); //$NON-NLS-1$
		
		try {
			logger.fine("Publishing plug-ins"); //$NON-NLS-1$
			PluginUtil.getPluginManager().publishPlugins(pluginLocations.toArray(new PluginLocation[pluginLocations.size()]));
		} catch (JpfException e) {
			logger.log(Level.SEVERE, "Failed to publish plug-ins", e); //$NON-NLS-1$
			exit(new Error("JpfException encountered: "+e.getMessage(), e)); //$NON-NLS-1$
		}
		
		if(logger.isLoggable(Level.FINE)) {
			IntegrityCheckReport report = PluginUtil.getPluginRegistry().getRegistrationReport();
			logger.fine(integrityCheckReport2str("Registration report", report)); //$NON-NLS-1$
		}
		
		// check plug-ins integrity
		logger.fine("Checking plug-ins set integrity"); //$NON-NLS-1$
		IntegrityCheckReport report = PluginUtil.getPluginRegistry().checkIntegrity(
				PluginUtil.getPathResolver());
		logger.info(String.format("Integrity check done: %d errors, %d warnings",  //$NON-NLS-1$
				report.countErrors(), report.countWarnings()));
		// output report
		if(report.countErrors()>0 || report.countWarnings()>0) {
			logger.warning(integrityCheckReport2str("Integrity check report", report)); //$NON-NLS-1$
		} else if(logger.isLoggable(Level.FINE)) {
			logger.fine(integrityCheckReport2str("Integrity check report", report)); //$NON-NLS-1$
		}
		
		// in case of errors simply exit launcher completely
		if(report.countErrors()>0)
			exit(new Error("Integrity check failed")); //$NON-NLS-1$
	}
    
    private String integrityCheckReport2str(final String header, final IntegrityCheckReport report) {
        StringBuilder buf = new StringBuilder();
        buf.append(header).append(":\r\n"); //$NON-NLS-1$
        buf.append("-------------- REPORT BEGIN -----------------\r\n"); //$NON-NLS-1$
        for (IntegrityCheckReport.ReportItem item : report.getItems()) {
            buf.append("\tSeverity=").append(item.getSeverity()) //$NON-NLS-1$
                .append("; Code=").append(item.getCode()) //$NON-NLS-1$
                .append("; Message=").append(item.getMessage()) //$NON-NLS-1$
                .append("; Source=").append(item.getSource()) //$NON-NLS-1$
                .append("\n"); //$NON-NLS-1$
        }
        buf.append("-------------- REPORT END -----------------"); //$NON-NLS-1$
        return buf.toString();
    }

    private void processFolder(final File folder, final List<PluginLocation> result) {
        logger.fine("processing folder - " + folder); //$NON-NLS-1$
        try {
            PluginLocation pluginLocation = StandardPluginLocation.create(folder);
            if (pluginLocation != null) {
                result.add(pluginLocation);
                return;
            }
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed collecting plug-in folder " + folder, e); //$NON-NLS-1$
            return;
        }
        
        // Recursively traverse content of folder till we find plug-ins
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                processFolder(file, result);
            } else if (file.isFile()) {
                processFile(file, result);
            }
        }
    }
    
    private void processFile(final File file, final List<PluginLocation> result) {
        logger.fine("processing file - " + file); //$NON-NLS-1$
        try {
            PluginLocation pluginLocation = StandardPluginLocation.create(file);
            if (pluginLocation != null) {
                result.add(pluginLocation);
            }
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed collecting plug-in file " + file , e); //$NON-NLS-1$
        }
    }
	
	private void launchCorePlugin() {
		try {
			logger.fine("Starting core plugin"); //$NON-NLS-1$
			
			// DEBUG
			LoggerFactory.setInitialLevel(Level.ALL);
			
			Plugin corePlugin = PluginUtil.getPluginManager().getPlugin(ICARUS_CORE_PLUGIN);
			logger.info("Started core plugin: "+corePlugin); //$NON-NLS-1$
		} catch (PluginLifecycleException e) {
			exit(new Error("Failed to start core plugin: "+e.getMessage(), e)); //$NON-NLS-1$
		}
	}
	
	// exits he launcher by throwing the provided Error
	// after closing all handlers on the internal logger
	private void exit(Error e) {
		logger.log(Level.SEVERE, "Fatal error encountered - launcher will exit", e); //$NON-NLS-1$
		
		for(Handler handler : logger.getHandlers()) {
			if(handler instanceof StreamHandler) {
				((StreamHandler)handler).close();
			}
		}
		
		new CoreErrorDialog(e).setVisible(true);
	}

	/**
	 * @return the launcher
	 */
	public static Core getCore() {
		return core;
	}

	/**
	 * @return the rootFolder
	 */
	public File getRootFolder() {
		return rootFolder;
	}

	/**
	 * @return the logFolder
	 */
	public File getLogFolder() {
		return logFolder;
	}

	/**
	 * @return the pluginFolder
	 */
	public File getPluginFolder() {
		return pluginFolder;
	}

	/**
	 * @return the dataFolder
	 */
	public File getDataFolder() {
		return dataFolder;
	}

	/**
	 * @return the tempFolder
	 */
	public File getTempFolder() {
		return tempFolder;
	}
	
	public File createTempFile(String baseName) throws IOException {
		return File.createTempFile(baseName, "tmp", getTempFolder()); //$NON-NLS-1$
	}
	
	private final class PluginManagerLog implements PluginManager.EventListener {
		

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginActivated(org.java.plugin.Plugin)
		 */
		@Override
		public void pluginActivated(Plugin plugin) {
            logger.info("plug-in started - " + plugin.getDescriptor().getUniqueId() //$NON-NLS-1$
                    + " (active/total: " + PluginUtil.countActive() //$NON-NLS-1$
                    + " of "  //$NON-NLS-1$
                    + PluginUtil.getPluginRegistry().getPluginDescriptors().size() + ")"); //$NON-NLS-1$
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginDeactivated(org.java.plugin.Plugin)
		 */
		@Override
		public void pluginDeactivated(Plugin plugin) {
            logger.info("plug-in stopped - " + plugin.getDescriptor().getUniqueId() //$NON-NLS-1$
                    + " (active/total: " + PluginUtil.countActive() //$NON-NLS-1$
                    + " of " //$NON-NLS-1$
                    + PluginUtil.getPluginRegistry().getPluginDescriptors().size() + ")"); //$NON-NLS-1$
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginDisabled(org.java.plugin.registry.PluginDescriptor)
		 */
		@Override
		public void pluginDisabled(PluginDescriptor descriptor) {
            logger.info("plug-in disabled - " + descriptor.getUniqueId() //$NON-NLS-1$
                    + " (enabled/total: " + PluginUtil.countEnabled() //$NON-NLS-1$
                    + " of " //$NON-NLS-1$
                    + PluginUtil.getPluginRegistry().getPluginDescriptors().size() + ")"); //$NON-NLS-1$
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginEnabled(org.java.plugin.registry.PluginDescriptor)
		 */
		@Override
		public void pluginEnabled(PluginDescriptor descriptor) {
            logger.info("plug-in enabled - " + descriptor.getUniqueId() //$NON-NLS-1$
                    + " (enabled/total: " + PluginUtil.countEnabled() //$NON-NLS-1$
                    + " of " //$NON-NLS-1$
                    + PluginUtil.getPluginRegistry().getPluginDescriptors().size() + ")"); //$NON-NLS-1$
		}
		
	}
}
