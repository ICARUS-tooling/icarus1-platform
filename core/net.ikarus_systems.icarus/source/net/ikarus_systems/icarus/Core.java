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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;

/**
 * Main class and application entry point for startup operation.
 * <p>
 * Manages default folder structure and the following levels of
 * properties (or <i>options</i>):
 * <ol>
 * <li>System properties</li>
 * <li>Plug-in properties</li>
 * <li>Application properties</li>
 * </ol>
 * 
 * <i>System properties</i> are the collection of system wide properties
 * accessible by Java via the {@link System#getProperties()} method.
 * <p>
 * <i>Plug-in properties</i> are properties defined within the {@code attributes}
 * section of a plug-in manifest file. Only attributes that are declared as child attributes
 * of the first {@value #PROPERTIES_KEY} attribute are considered to be 
 * property definitions and will be processed in order of their
 * definition and checked for being a plain property definition (using the
 * {@code id} field as key and {@code value} field as value for a new property
 * entry) or a link pointing to a resource that can be used to load properties from.
 * Such links have to use the {@value #PROPERTIES_PATH_KEY} string as {@code id} and their
 * {@code value} field will be used to locate the properties resource(s).<br>
 * Note that there can be multiple attributes legally sharing the same {@code id}, 
 * however only the <b>last</b> attribute's {@code value} will be used for the
 * property in question! In the case of multiple attributes linking to property
 * resources all of them will be read following the same pattern of overwriting.
 * If a plug-in wishes to disable the publishing of its declared property attributes
 * it can simply define an attribute with the {@value #IGNORE_ATTRIBUTES_KEY} {@code id}
 * and a {@code value} that causes a call to {@code Boolean#parseBoolean(String)} to
 * return {@code true} (any mix of upper and lower cases of the string "true").
 * This flag will prevent the traversal of all property definitions within the 
 * declaring plug-in!
 * <p>
 * <i>Application properties</i> can be specified on startup via the {@code -config}
 * argument that has to point to a file that contains property definitions in a format
 * suitable to either {@link Properties#load(InputStream)} or {@link Properties#loadFromXML(InputStream)}.
 * The method to be applied is determined by the file ending of the supplied path. If ending
 * on ".xml" the {@link Properties#loadFromXML(InputStream)} method will be used.
 * In addition one can set properties directly by using the same argument format as
 * of the Java VM itself:<br>
 * Any argument starting with {@code -D} will be interpreted as a <i>key=value</i>
 * string and parsed accordingly.<br>
 * <b>Example:</b> {@code -Dsome.property.key=some.value}
 * <p>
 * Access to properties is also handled by this class using a hierarchical
 * approach for searching. When asked for a property value the internal properties maps
 * will be checked in reverse order of the levels described above until a mapping for the given
 * key is found. So for a call to {@link #getProperty(String)} a property defined on
 * plug-in level will override default system settings but will be hidden by
 * an application property defined as command line argument or in a config file 
 * linked via command line argument. Additionally there are methods defined to 
 * query specific collections of properties and all properties related methods
 * exist in two forms, one allowing for a default value to be supplied that will
 * be used in case no property for the given key could be found. 
 * 
 * 
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
			
			// Collect plug-ins and verify integrity
			core.collectPlugins();
			
			// Collect properties defined by plug-ins
			// (this might technically override system settings)
			core.collectProperties();
			
			// Launch core plug-in
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
	
	private static final String DEFAULT_CORE_PLUGIN_ID = 
			"net.ikarus_systems.icarus.core"; //$NON-NLS-1$
	
	public static final String PROPERTIES_PATH_KEY = 
			"net.ikarus_systems.icarus.propertiesPath"; //$NON-NLS-1$
	
	public static final String PROPERTIES_KEY = 
			"net.ikarus_systems.icarus.properties"; //$NON-NLS-1$
	
	public static final String IGNORE_ATTRIBUTES_KEY = 
			"net.ikarus_systems.icarus.ignoreAttributes"; //$NON-NLS-1$
	
	public static final String CORE_PLUGIN_KEY =  
			"net.ikarus_systems.icarus.corePlugin"; //$NON-NLS-1$
	
	private Map<String, String> applicationProperties;
	private Map<String, String> pluginProperties;
	
	private final Options options;

	private Core(String[] args) {
		logger = Logger.getLogger("icarus.launcher"); //$NON-NLS-1$
		
		// Init options
		options = new Options(args);
		
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
	
	private void collectProperties() {
		// Just use the options set of properties as application properties
		applicationProperties = options.properties;
		
		// Read in all attributes from plug-ins
		for(PluginDescriptor descriptor : PluginUtil.getPluginRegistry().getPluginDescriptors()) {
			
			// Check if ignore flag is set
			PluginAttribute ignoreAttr = descriptor.getAttribute(IGNORE_ATTRIBUTES_KEY);
			if(ignoreAttr!=null && Boolean.parseBoolean(ignoreAttr.getValue())) {
				continue;
			}
			
			// Check if plug-in defines properties
			PluginAttribute propertiesAttr = descriptor.getAttribute(PROPERTIES_KEY);
			if(propertiesAttr==null) {
				continue;
			}
			
			// Fetch and read property attributes
			for(PluginAttribute attribute : propertiesAttr.getSubAttributes()) {
				readProperty(attribute);
			}
		}
	}
	
	private void readProperty(PluginAttribute attribute) {
		if(PROPERTIES_PATH_KEY.equals(attribute.getId())) {
			// Read in properties resource file
			ClassLoader classLoader = PluginUtil.getClassLoader(attribute);
			String path = attribute.getValue();
			URL url = classLoader.getResource(path);
			if(url==null) {
				logger.log(Level.WARNING, "Could not locate properties file: "+path); //$NON-NLS-1$
				return;
			}
			
			Properties properties = readProperties(url, path.endsWith(".xml")); //$NON-NLS-1$
			if(properties==null) {
				// Logging is done on the readProperties() method
				return;
			}

			if(pluginProperties==null) {
				pluginProperties = new HashMap<>();
			}
			for(String key : properties.stringPropertyNames()) {
				pluginProperties.put(key, properties.getProperty(key));
			}
		} else {
			// Simple key-value definition
			if(pluginProperties==null) {
				pluginProperties = new HashMap<>();
			}
			pluginProperties.put(attribute.getId(), attribute.getValue());
		}
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
		if(report.countErrors()>0) {
			exit(new Error("Integrity check failed - check log")); //$NON-NLS-1$
		}
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
			
			String corePluginId = getProperty(CORE_PLUGIN_KEY, DEFAULT_CORE_PLUGIN_ID);
			
			Plugin corePlugin = PluginUtil.getPluginManager().getPlugin(corePluginId);
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
		
		throw e;
	}

	/**
	 * Returns the singleton {@code Core} instance.
	 */
	public static Core getCore() {
		return core;
	}
	
	public Options getOptions() {
		return options;
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
	
	// PROPERTIES ACCESS

	public String getProperty(String key) {
		return getProperty(key, null);
	}
	
	public String getProperty(String key, String defaultValue) {
		String value = applicationProperties==null ? null : applicationProperties.get(key);
		if(value==null) {
			value = pluginProperties==null ? null : pluginProperties.get(key);
		}
		if(value==null) {
			value = System.getProperty(key, defaultValue);
		}
		
		return value;
	}
	
	public String getApplicationProperty(String key) {
		return getApplicationProperty(key, null);
	}
	
	public String getApplicationProperty(String key, String defaultValue) {
		String value = applicationProperties==null ? null : applicationProperties.get(key);
		return value==null ? defaultValue : value;
	}
	
	public String getPluginProperty(String key) {
		return getPluginProperty(key, null);
	}
	
	public String getPluginProperty(String key, String defaultValue) {
		String value = pluginProperties==null ? null : pluginProperties.get(key);
		return value==null ? defaultValue : value;
	}

	public Map<String, String> getApplicationProperties() {
		return Collections.unmodifiableMap(applicationProperties);
	}
	
	public Map<String, String> getPluginProperties() {
		return Collections.unmodifiableMap(pluginProperties);
	}
	
	private Properties readProperties(InputStream in, boolean xml) {
		Properties properties = new Properties();
		
		try {
			if(xml) {
				properties.loadFromXML(in);
			} else {
				properties.load(in);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to read properties", e); //$NON-NLS-1$
			properties = null;
		}
		
		return properties;
	}
	
	private Properties readProperties(File file, boolean xml) {
		try {
			return readProperties(new FileInputStream(file), xml);
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Properties file does not exist: "+file.getAbsolutePath(), e); //$NON-NLS-1$
		}
		return null;
	}
	
	private Properties readProperties(URL url, boolean xml) {
		try {
			return readProperties(url.openStream(), xml);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to connect to properties location: "+url, e); //$NON-NLS-1$
		}
		return null;
	}
	
	public class Options {
		private Map<String,String> properties;
		private boolean verbose = false;
		
		private final String[] args;
		
		public Options(String[] args) {
			this.args = args;
			
			for(int i=0; i<args.length; i++) {
				String token = args[i];
				if("-v".equals(token)) { //$NON-NLS-1$
					verbose = true;
				} else if("-plugin".equals(token)) { //$NON-NLS-1$
					putProperty(CORE_PLUGIN_KEY, args[++i]);
				} else if("-config".equals(token)) { //$NON-NLS-1$
					String path = args[++i];
					File file = new File(path);
					Properties props = readProperties(file, path.endsWith(".xml")); //$NON-NLS-1$
					putProperties(props);
				} else if(token.startsWith("-D")) { //$NON-NLS-1$
					int deli = token.indexOf('=', 3);
					if(deli==-1) {
						continue;
					}
					String key = token.substring(2, deli-1);
					String value = token.substring(deli+1);
					if(value.startsWith("\"") && value.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
						value = value.substring(1, value.length()-1);
					}
					
					putProperty(key, value);
				}
			}
		}
		
		private void putProperties(Properties props) {
			if(props==null || props.isEmpty()) {
				return;
			}
			
			if(properties==null) {
				properties = new HashMap<>();
			}

			for(String key : props.stringPropertyNames()) {
				properties.put(key, props.getProperty(key));
			}
		}
		
		private void putProperty(String key, String value) {
			if(properties==null) {
				properties = new HashMap<>();
			}
			properties.put(key, value);
		}
		
		public boolean isVerbose() {
			return verbose;
		}
		
		public String[] getArgs() {
			return Arrays.copyOf(args, args.length);
		}
		
		public String getProperty(String key) {
			return properties==null ? null : properties.get(key);
		}
	}
	
	@SuppressWarnings("unused")
	// XXX
	private class PluginManagerLog implements PluginManager.EventListener {
		

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
