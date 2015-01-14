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

/**
 *
 */
package de.ims.icarus;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.java.plugin.JpfException;
import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.IntegrityCheckReport;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.launcher.SplashWindow;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.ui.dialog.DialogDispatcher;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.vm.SystemMonitor;
import de.ims.icarus.util.Exceptions;

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
	private static boolean debugActive = false;

	public static void debugInit(String...args) {

		debugActive = true;

		try {
			if(core!=null)
				throw new IllegalStateException("Core already started!"); //$NON-NLS-1$

			core = new Core(args);

		} catch(Throwable e) {
			new CoreErrorDialog(e);
		}
	}

	public static boolean isDebugActive() {
		return debugActive;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if(core!=null)
				throw new IllegalStateException("Core already started!"); //$NON-NLS-1$

			core = new Core(args);

			SplashWindow.setMaxProgress(7);

			SplashWindow.setText("Collecting plug-ins"); //$NON-NLS-1$
			SplashWindow.step();
			// Collect plug-ins and verify integrity
			core.collectPlugins();

			SplashWindow.setText("Collecting properties"); //$NON-NLS-1$
			SplashWindow.step();
			// Collect properties defined by plug-ins
			// (this might technically override system settings)
			core.collectProperties();

			SplashWindow.setText("Launching core plug-in"); //$NON-NLS-1$
			SplashWindow.step();
			// Launch core plug-in
			core.launchCorePlugin();

		} catch(Throwable e) {
			new CoreErrorDialog(e);
		}
	}

	private final Logger logger;

	private final Path rootFolder;
	private final Path logFolder;
	private final Path pluginFolder;
	private final Path cacheFolder;
	private final Path dataFolder;
	private final Path tempFolder;
	private final Path externalFolder;

	public static final String IGNORE_STREAM_REDIRECT_PROPERTY =
			"de.ims.icarus.ignoreStreamRedirect"; //$NON-NLS-1$

	public static final String DEFAULT_CORE_PLUGIN_ID =
			"de.ims.icarus.core"; //$NON-NLS-1$

	public static final String PROPERTIES_PATH_KEY =
			"de.ims.icarus.propertiesPath"; //$NON-NLS-1$

	public static final String PROPERTIES_KEY =
			"de.ims.icarus.properties"; //$NON-NLS-1$

	public static final String IGNORE_ATTRIBUTES_KEY =
			"de.ims.icarus.ignoreAttributes"; //$NON-NLS-1$

	public static final String CORE_PLUGIN_KEY =
			"de.ims.icarus.corePlugin"; //$NON-NLS-1$

	private Map<String, String> applicationProperties;
	private Map<String, String> pluginProperties;
	private Map<String, String> versionProperties;

	private List<NamedRunnable> shutdownHooks;

	private final CoreOptions options;

	private Core(String[] args) {
		logger = Logger.getLogger("icarus.launcher"); //$NON-NLS-1$

		SplashWindow.setText("Processing options"); //$NON-NLS-1$
		SplashWindow.step();
		// Init options
		options = new CoreOptions(args);

		SplashWindow.setText("Checking folders"); //$NON-NLS-1$
		// init folders
		rootFolder = Paths.get(System.getProperty("user.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$

		// init log folder
		logFolder = rootFolder.resolve("logs"); //$NON-NLS-1$
		ensureDir(logFolder, "logging"); //$NON-NLS-1$

		// init plugins folder
		pluginFolder = rootFolder.resolve("plugins"); //$NON-NLS-1$
		ensureDir(pluginFolder, "plugins"); //$NON-NLS-1$

		// init data folder
		dataFolder = rootFolder.resolve("data"); //$NON-NLS-1$
		ensureDir(dataFolder, "data"); //$NON-NLS-1$

		// init temp folder
		tempFolder = rootFolder.resolve("temp"); //$NON-NLS-1$
		ensureDir(tempFolder, "temp"); //$NON-NLS-1$

		// init cache folder
		cacheFolder = rootFolder.resolve("cache"); //$NON-NLS-1$
		ensureDir(cacheFolder, "cache"); //$NON-NLS-1$

		// init cache folder
		externalFolder = rootFolder.resolve("external"); //$NON-NLS-1$
		ensureDir(externalFolder, "external"); //$NON-NLS-1$

		// Redirect default output
		if(!ignoreRedirect()) {
			Path outFile = logFolder.resolve("out.txt"); //$NON-NLS-1$
			PrintStream defaultOut = System.out;
			PrintStream defaultErr = System.err;
			try {
				if(!Files.isRegularFile(outFile)) {
					Files.createFile(outFile);
				}

				PrintStream ps = new PrintStream(Files.newOutputStream(outFile),
					true, "UTF-8"); //$NON-NLS-1$

				System.setOut(ps);
				System.setErr(ps);
			} catch(Exception e) {
				System.setOut(defaultOut);
				System.setErr(defaultErr);

				e.printStackTrace(defaultOut);
			}
		}

		SplashWindow.setText("Starting logger"); //$NON-NLS-1$
		SplashWindow.step();
		// Init default log file
		Path logFile = logFolder.resolve("launcher.log"); //$NON-NLS-1$
		try {
			logger.addHandler(new FileHandler(logFile.toString()));
		} catch (SecurityException e) {
			throw new Error("Unable to define log file: "+logFile, e); //$NON-NLS-1$
		} catch (IOException e) {
			throw new Error("Unable to access log file: "+logFile, e); //$NON-NLS-1$
		}

		SplashWindow.setText("Ensuring license file"); //$NON-NLS-1$
		SplashWindow.step();
		// Ensure license file
		Path licenseFile = rootFolder.resolve("license.txt"); //$NON-NLS-1$
		try {
			if(Files.notExists(licenseFile)) {
				Files.createFile(licenseFile);
				IOUtil.copyStream(getLicense(), Files.newOutputStream(licenseFile), 0);
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Failed to export license file", ex); //$NON-NLS-1$
		}

		SplashWindow.setText("Loading plugin registry"); //$NON-NLS-1$
		SplashWindow.step();
		// From here on we can use our logger
		try {
			PluginUtil.load(logger);
		} catch (Exception e) {
			throw new Error("Failed to load plug-in framework objects", e); //$NON-NLS-1$
		}

		PluginUtil.getPluginManager().registerListener(new PluginManagerLog());
	}

	private void ensureDir(Path path, String name) {
		if(!Files.isDirectory(path) && Files.notExists(path)) {
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				throw new Error("Unable to create "+name+" directory"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	public static boolean ignoreRedirect() {
		String ignore = System.getProperty(IGNORE_STREAM_REDIRECT_PROPERTY);
		return debugActive || (ignore!=null && Boolean.parseBoolean(ignore));
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

		// Load the "version.properties" file generated by the build script
		try {
			URL url = Core.class.getResource("version.properties"); //$NON-NLS-1$
			Properties props = readProperties(url, false);
			if(props!=null) {
				versionProperties = new HashMap<>();

				for(String key : props.stringPropertyNames()) {
					versionProperties.put(key, props.getProperty(key));
				}
			}
		} catch(Exception e) {
			logger.log(Level.WARNING, "Failed to load version information from 'version.properties' file", e); //$NON-NLS-1$
		}

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

    private void processFolder(final Path folder, final List<PluginLocation> result) {
    	logger.fine("processing folder - " + folder); //$NON-NLS-1$
        try {
            PluginLocation pluginLocation = StandardPluginLocation.create(folder.toFile());
            if (pluginLocation != null) {
                result.add(pluginLocation);
                return;
            }
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed collecting plug-in folder " + folder, e); //$NON-NLS-1$
            return;
        }

        // Recursively traverse content of folder till we find plug-ins
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
        	for(Path path : stream) {
        		if(Files.isDirectory(path)) {
                    processFolder(path, result);
        		} else if(Files.isRegularFile(path)) {
                    processFile(path, result);
        		}
        	}
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed traversing files in plug-in folder " + folder, e); //$NON-NLS-1$
		}
    }

    private void processFile(final Path file, final List<PluginLocation> result) {
    	logger.fine("processing file - " + file); //$NON-NLS-1$
        try {
            PluginLocation pluginLocation = StandardPluginLocation.create(file.toFile());
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

	// Exits the launcher by throwing the provided Error
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

	public synchronized void addShutdownHook(NamedRunnable hook) {
		if(shutdownHooks==null) {
			shutdownHooks = new ArrayList<>();
		}

		shutdownHooks.add(hook);
	}

	public synchronized void addShutdownHook(final String name, final Runnable r) {
		NamedRunnable hook = new NamedRunnable() {

			@Override
			public void run() throws Exception {
				r.run();
			}

			@Override
			public String getName() {
				return name;
			}
		};

		addShutdownHook(hook);
	}

	public synchronized NamedRunnable[] getShutdownHooks() {
		if(shutdownHooks==null) {
			return new NamedRunnable[0];
		}

		NamedRunnable[] hooks = new NamedRunnable[shutdownHooks.size()];

		return shutdownHooks.toArray(hooks);
	}

	private volatile boolean shuttingDown = false;

	/**
	 * @return the shuttingDown
	 */
	public boolean isShuttingDown() {
		return shuttingDown;
	}

	public synchronized void shutdown(UncaughtExceptionHandler handler) {
		shuttingDown = true;

		NamedRunnable[] hooks = getShutdownHooks();

		for(NamedRunnable hook : hooks) {
			try {
				hook.run();
			} catch(Exception e) {
				if(handler!=null) {
					handler.uncaughtException(Thread.currentThread(), e);
				} else {
					LoggerFactory.log(this, Level.SEVERE,
							"Uncaught exception in shutdown hook: "+hook.getName(), e); //$NON-NLS-1$
				}
			}
		}
	}

	public CoreOptions getOptions() {
		return options;
	}

	/**
	 * @return the rootFolder
	 */
	public Path getRootFolder() {
		return rootFolder;
	}

	/**
	 * @return the logFolder
	 */
	public Path getLogFolder() {
		return logFolder;
	}

	/**
	 * @return the pluginFolder
	 */
	public Path getPluginFolder() {
		return pluginFolder;
	}

	/**
	 * @return the dataFolder
	 */
	public Path getDataFolder() {
		return dataFolder;
	}

	/**
	 * @return the tempFolder
	 */
	public Path getTempFolder() {
		return tempFolder;
	}

	/**
	 * @return the tempFolder
	 */
	public Path getCacheFolder() {
		return cacheFolder;
	}

	/**
	 * @return the externalFolder
	 */
	public Path getExternalFolder() {
		return externalFolder;
	}

	public Path createTempFile(String baseName) throws IOException {
		return Files.createTempFile(tempFolder, baseName, "tmp"); //$NON-NLS-1$
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

	private Properties readProperties(Path file, boolean xml) {
		try {
			return readProperties(Files.newInputStream(file), xml);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Properties file does not exist: "+file, e); //$NON-NLS-1$
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

	public static void showNotice() {
		JLabel label = new JLabel("Soon "+(char)0x2122); //$NON-NLS-1$
		label.setForeground(Color.blue.brighter());
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Dialog", Font.PLAIN, 30)); //$NON-NLS-1$

		DialogFactory.getGlobalFactory().showGenericDialog(
				null, "Coming...", null, label, false); //$NON-NLS-1$
	}

	private static InputStream getLicense() {
		return Core.class.getResourceAsStream("license.txt"); //$NON-NLS-1$
	}

	public static String getLicenseText() {
		InputStream in = getLicense();
		try {
			return IOUtil.readStream(in);
		} catch (IOException e) {
			LoggerFactory.log(Core.class, Level.SEVERE,
					"Failed to load license file", e); //$NON-NLS-1$

			return null;
		} finally {
			try {
				in.close();
			} catch(IOException e) {
				LoggerFactory.error(Core.class, "Failed to close input stream", e); //$NON-NLS-1$
			}
		}
	}

	public static Icon getLicenseIcon() {
		return new ImageIcon(Core.class.getResource("cc-by-nc-sa.png")); //$NON-NLS-1$
	}

	private static ImageIcon logo_16;
	private static ImageIcon logo_32;
	private static ImageIcon logo_64;

	public static ImageIcon getSmallIcon() {
		if(logo_16==null) {
			logo_16 = new ImageIcon(Core.class.getResource("logo_16x16.png")); //$NON-NLS-1$
		}
		return logo_16;
	}

	public static ImageIcon getLargeIcon() {
		if(logo_64==null) {
			logo_64 = new ImageIcon(Core.class.getResource("logo_64x64.png")); //$NON-NLS-1$
		}
		return logo_64;
	}

	public static ImageIcon getMediumIcon() {
		if(logo_32==null) {
			logo_32 = new ImageIcon(Core.class.getResource("logo_32x32.png")); //$NON-NLS-1$
		}
		return logo_32;
	}

	public static List<Image> getIconImages() {
		List<Image> images = new ArrayList<>(3);

		images.add(getSmallIcon().getImage());
		images.add(getMediumIcon().getImage());
		images.add(getLargeIcon().getImage());

		return images;
	}

	public void ensureResource(String filename,	String path, ClassLoader loader) {
		ensureResource(null, filename, path, loader);
	}

	public void ensureResource(String foldername, String filename,
			String path, ClassLoader loader) {

		InputStream in = null;
		OutputStream out = null;

		try {
			Path folder = dataFolder;
			if(foldername!=null) {
				folder = folder.resolve(foldername);
				if(Files.notExists(folder)) {
					Files.createDirectory(folder);
				}
			}

			Path file = folder.resolve(filename);
			if(Files.isRegularFile(file)) {
				return;
			}

			Files.createFile(file);

//			System.out.println(path);

			in = loader.getResourceAsStream(path);
			out = Files.newOutputStream(file);

			IOUtil.copyStream(in, out);

		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to ensure resource: "+filename, e); //$NON-NLS-1$
		} finally {
			if(in!=null) {
				try {
					in.close();
				} catch(IOException e) {
					LoggerFactory.error(this, "Failed to clsoe input stream", e); //$NON-NLS-1$
				}
			}

			if(out!=null) {
				try {
					out.close();
				} catch(IOException e) {
					LoggerFactory.error(this, "Failed to clsoe output stream", e); //$NON-NLS-1$
				}
			}
		}
	}

	public String getVersionProperty(String key) {
		return versionProperties==null ? null : versionProperties.get(key);
	}

	public String getAppVendor() {
		String vendor = getVersionProperty("app.vendor"); //$NON-NLS-1$
		return vendor==null ? "???" : vendor; //$NON-NLS-1$
	}

	public String getAppName() {
		String name = getVersionProperty("app.name"); //$NON-NLS-1$
		return name==null ? "???" : name; //$NON-NLS-1$
	}

	public String getAppRevision() {
		String revision = getVersionProperty("app.revision"); //$NON-NLS-1$
		return revision==null ? "???" : revision; //$NON-NLS-1$
	}

	public String getAppBuildDate() {
		String date = getVersionProperty("build.date"); //$NON-NLS-1$
		return date==null ? "???" : date; //$NON-NLS-1$
	}

	public String getAppVersion() {
		String version = getVersionProperty("app.version"); //$NON-NLS-1$
		return version==null ? "???" : version; //$NON-NLS-1$
	}


	public boolean handleThrowable(Throwable t) {

		OutOfMemoryError oom = Exceptions.getThrowableOfType(t, OutOfMemoryError.class);

		if(oom!=null) {
			long used = SystemMonitor.getInstance().getUsed();
			long committed = SystemMonitor.getInstance().getCommitted();

			DialogDispatcher dispatcher = new DialogDispatcher(
					null,
					"core.dialogs.outOfMemory.title",  //$NON-NLS-1$
					"core.dialogs.outOfMemory.message",  //$NON-NLS-1$
					SystemMonitor.formatMemory(used),
					SystemMonitor.formatMemory(committed));

			dispatcher.showAsError();

			return true;
		}

		return false;
	}


	/**
	 * Command line argument wrapper
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public class CoreOptions {
		private Map<String,String> properties;
		private boolean verbose = false;
		private boolean devMode = false;

		private final String[] args;

		public CoreOptions(String[] args) {
			this.args = args;

			for(int i=0; i<args.length; i++) {
				String token = args[i];
				if("-v".equals(token)) { //$NON-NLS-1$
					verbose = true;
				} else if("-plugin".equals(token)) { //$NON-NLS-1$
					putProperty(CORE_PLUGIN_KEY, args[++i]);
				} else if("-dev".equals(token)) { //$NON-NLS-1$
					devMode = true;
				} else if("-config".equals(token)) { //$NON-NLS-1$
					String path = args[++i];
					Path file = Paths.get(path);
					Properties props = readProperties(file, path.endsWith(".xml")); //$NON-NLS-1$
					putProperties(props);
				} else if(token.startsWith("-D")) { //$NON-NLS-1$
					int deli = token.indexOf('=', 3);
					if(deli==-1) {
						continue;
					}
					String key = token.substring(2, deli);
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

		public boolean isDevMode() {
			return devMode;
		}

		public String[] getArgs() {
			return Arrays.copyOf(args, args.length);
		}

		public String getProperty(String key) {
			return properties==null ? null : properties.get(key);
		}
	}

	/**
	 *
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static interface NamedRunnable {

		String getName();

		void run() throws Exception;
	}

	private class PluginManagerLog implements PluginManager.EventListener {


		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginActivated(org.java.plugin.Plugin)
		 */
		@Override
		public void pluginActivated(Plugin plugin) {
            String msg = "plug-in started - " + plugin.getDescriptor().getUniqueId() //$NON-NLS-1$
                    + " (active/total: " + PluginUtil.countActive() //$NON-NLS-1$
                    + " of "  //$NON-NLS-1$
                    + PluginUtil.getPluginRegistry().getPluginDescriptors().size() + ")"; //$NON-NLS-1$

            LoggerFactory.log(this, Level.INFO, msg);
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginDeactivated(org.java.plugin.Plugin)
		 */
		@Override
		public void pluginDeactivated(Plugin plugin) {
			String msg = "plug-in stopped - " + plugin.getDescriptor().getUniqueId() //$NON-NLS-1$
                    + " (active/total: " + PluginUtil.countActive() //$NON-NLS-1$
                    + " of " //$NON-NLS-1$
                    + PluginUtil.getPluginRegistry().getPluginDescriptors().size() + ")"; //$NON-NLS-1$

            LoggerFactory.log(this, Level.INFO, msg);
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginDisabled(org.java.plugin.registry.PluginDescriptor)
		 */
		@Override
		public void pluginDisabled(PluginDescriptor descriptor) {
			String msg = "plug-in disabled - " + descriptor.getUniqueId() //$NON-NLS-1$
                    + " (enabled/total: " + PluginUtil.countEnabled() //$NON-NLS-1$
                    + " of " //$NON-NLS-1$
                    + PluginUtil.getPluginRegistry().getPluginDescriptors().size() + ")"; //$NON-NLS-1$

            LoggerFactory.log(this, Level.INFO, msg);
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginEnabled(org.java.plugin.registry.PluginDescriptor)
		 */
		@Override
		public void pluginEnabled(PluginDescriptor descriptor) {
			String msg = "plug-in enabled - " + descriptor.getUniqueId() //$NON-NLS-1$
                    + " (enabled/total: " + PluginUtil.countEnabled() //$NON-NLS-1$
                    + " of " //$NON-NLS-1$
                    + PluginUtil.getPluginRegistry().getPluginDescriptors().size() + ")"; //$NON-NLS-1$

            LoggerFactory.log(this, Level.INFO, msg);
		}

	}
}
