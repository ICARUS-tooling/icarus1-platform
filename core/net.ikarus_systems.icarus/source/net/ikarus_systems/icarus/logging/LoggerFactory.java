/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.logging;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.ikarus_systems.icarus.util.Exceptions;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class LoggerFactory {

	private static final File logFolder;
	
	private static Level initialLevel = Level.INFO;
	
	private static Map<String, Logger> loggers = new HashMap<>();
	private static Map<String, File> logFiles = new HashMap<>();
	
	private static BufferedHandler rootHandler;
	
	// obtain valid log folder
	static {
		File rootFolder = new File(System.getProperty("user.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		
		logFolder = new File(rootFolder, "logs"); //$NON-NLS-1$
		
		try {
			if(!logFolder.exists())
				logFolder.createNewFile();
			
			if(!logFolder.isDirectory())
				throw new Error("Log folder is not a directory!"); //$NON-NLS-1$
		} catch(Exception e) {
			throw new Error("Unable to init logging facility", e); //$NON-NLS-1$
		}
		
		registerLogFile("net.ikarus_systems.icarus", "icarus"); //$NON-NLS-1$ //$NON-NLS-2$
		
		getLogger(LoggerFactory.class);
	}
	
	private LoggerFactory() {
		// no-op
	}
	
	public static void registerLogFile(String name, String fileName) {
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		Exceptions.testNullArgument(fileName, "fileName"); //$NON-NLS-1$
		
		File file = getLogFile(fileName);
		
		File currentFile = logFiles.get(name);
		
		if(currentFile!=null && currentFile.equals(file)) {
			return;
		}
		
		if(currentFile!=null)
			throw new IllegalArgumentException("Log file already set for logger: "+name); //$NON-NLS-1$
		
		logFiles.put(name, file);
		
		if(loggers.containsKey(name)) {
			addFileHandler(loggers.get(name), file);
		}
	}
	
	private static File getLogFile(String name) {
		name = name.toLowerCase()+".log"; //$NON-NLS-1$
		return new File(logFolder, name);
	}
    
    private static String getParentName(String name) {
    	int idx = name.lastIndexOf('.');
    	if(idx==-1) {
    		return null;
    	}
    	return name.substring(0, idx);
    }
    
    private static void addFileHandler(Logger logger, File file) {
		try {
			logger.addHandler(new FileHandler(file.getPath()));
			
			logger.setLevel(getInitialLevel());
		} catch (SecurityException | IOException e) {
			throw new IllegalStateException("Unable to create file based logger: "+file, e); //$NON-NLS-1$
		}
    }
	
	private static Logger createLogger(String name) {		
		// Load parents if required
		String parentName = getParentName(name);
		if(parentName!=null && !loggers.containsKey(parentName)) {
			createLogger(parentName);
		}
		
		Logger logger = Logger.getLogger(name);
		
		// Special handling for the root logger
		if(loggers.isEmpty()) {
			rootHandler = new BufferedHandler();
			logger.addHandler(rootHandler);
		}
		
		loggers.put(name, logger);
		
		File logFile = logFiles.get(name);
		if(logFile!=null) {
			addFileHandler(logger, logFile);
		}
		
		return logger;
	}
	
	public static synchronized Logger getLogger(Object owner) {
		if(!(owner instanceof Class<?>)) {
			owner = owner.getClass();
		}
		return getLogger((Class<?>) owner);
	}
	
	public static synchronized Logger getLogger(Class<?> owner) {
		String name = owner.getName();
		
		Logger logger = loggers.get(name);
		
		if(logger==null) {
			logger = createLogger(name);
		}
		
		return logger;
	}
	
	public static synchronized Logger getRootLogger() {
		if(loggers.isEmpty()) {
			return null;
		}
		
		Logger logger = loggers.values().iterator().next();
		while(logger.getParent()!=null) {
			logger = logger.getParent();
		}
		return logger;
	}
	
	public static BufferedHandler getRootHandler() {
		return rootHandler;
	}

	/**
	 * @return the initialLevel
	 */
	public static Level getInitialLevel() {
		return initialLevel;
	}

	/**
	 * @param initialLevel the initialLevel to set
	 */
	public static void setInitialLevel(Level initialLevel) {
		LoggerFactory.initialLevel = initialLevel;
	}
	
	public static LogRecord record(Level level, String message) {
		LogRecord record = new LogRecord(level, message);
		setCallerFields(record);
		return record;
	}
	
	public static LogRecord record(Level level, String message, Throwable t) {
		LogRecord record = new LogRecord(level, message);
		record.setThrown(t);
		setCallerFields(record);
		return record;
	}

	public static void setCallerFields(LogRecord record) {
		Throwable throwable = new Throwable();
		StackTraceElement[] trace = throwable.getStackTrace();
		int depth = trace.length;

		boolean lookingForLogger = true;
		for (int i = 0; i < depth; i++) {
			StackTraceElement frame = trace[i];
			String cname = frame.getClassName();
			// System.out.println(cname);
			boolean isLoggerImpl = isLoggerImplFrame(cname);
			if (lookingForLogger) {
				// Skip all frames until we have found the first logger frame.
				if (isLoggerImpl) {
					lookingForLogger = false;
				}
			} else {
				if (!isLoggerImpl) {
					// skip reflection call
					if (!cname.startsWith("java.lang.reflect.") && !cname.startsWith("sun.reflect.")) { //$NON-NLS-1$ //$NON-NLS-2$
						// We've found the relevant frame.
						record.setSourceClassName(cname);
						record.setSourceMethodName(frame.getMethodName());
						return;
					}
				}
			}
		}
		// We haven't found a suitable frame, so just punt. This is
		// OK as we are only committed to making a "best effort" here.
	}

	private static boolean isLoggerImplFrame(String cname) {
		// the log record could be created for a platform logger
		return (cname.equals("java.util.logging.Logger") || //$NON-NLS-1$
				cname.startsWith("net.ikarus_systems.icarus.logging") || //$NON-NLS-1$
				cname.startsWith("java.util.logging.LoggingProxyImpl") || //$NON-NLS-1$
				cname.startsWith("sun.util.logging.")); //$NON-NLS-1$
	}
}
