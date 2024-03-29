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
package de.ims.icarus.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import de.ims.icarus.util.Exceptions;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class LoggerFactory {

	private static final Path logFolder;

	private static Level initialLevel = Level.INFO;

	private static Map<String, Logger> loggers = new HashMap<>();
	private static Map<String, Path> logFiles = new HashMap<>();

	private static BufferedHandler rootHandler;

	// obtain valid log folder
	static {
		Path rootFolder = Paths.get(System.getProperty("user.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$

		logFolder = rootFolder.resolve("logs"); //$NON-NLS-1$

		try {
			if(Files.notExists(logFolder)) {
				Files.createDirectory(logFolder);
			}

			if(!Files.isDirectory(logFolder))
				throw new Error("Log folder is not a directory!"); //$NON-NLS-1$
		} catch(Exception e) {
			throw new Error("Unable to init logging facility", e); //$NON-NLS-1$
		}

		registerLogFile("de.ims.icarus", "icarus"); //$NON-NLS-1$ //$NON-NLS-2$

		getLogger(LoggerFactory.class);
	}

	private LoggerFactory() {
		// no-op
	}

	public static void registerLogFile(String name, String fileName) {
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		Exceptions.testNullArgument(fileName, "fileName"); //$NON-NLS-1$

		Path file = getLogFile(fileName);

		Path currentFile = logFiles.get(name);

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

	private static Path getLogFile(String name) {
		name = name.toLowerCase()+".log"; //$NON-NLS-1$
		return logFolder.resolve(name);
	}

    private static String getParentName(String name) {
    	int idx = name.lastIndexOf('.');
    	if(idx==-1) {
    		return null;
    	}
    	return name.substring(0, idx);
    }

    private static void addFileHandler(Logger logger, Path file) {
		try {
			logger.addHandler(new FileHandler(file.toString()));

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

		Path logFile = logFiles.get(name);
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

	public static LogRecord log(Object owner, Level level, String message, Throwable t) {
		LogRecord record = record(level, message, t);
		getLogger(owner).log(record);
		return record;
	}

	public static LogRecord log(Object owner, Level level, String message) {
		LogRecord record = record(level, message);
		getLogger(owner).log(record);
		return record;
	}

	public static LogRecord debug(Object owner, String message) {
		return log(owner, Level.FINE, message);
	}

	public static LogRecord debug(Object owner, String message, Throwable t) {
		return log(owner, Level.FINE, message, t);
	}

	public static LogRecord info(Object owner, String message) {
		return log(owner, Level.INFO, message);
	}

	public static LogRecord warning(Object owner, String message) {
		return log(owner, Level.WARNING, message);
	}

	public static LogRecord warning(Object owner, String message, Throwable t) {
		return log(owner, Level.WARNING, message, t);
	}

	public static LogRecord error(Object owner, String message) {
		return log(owner, Level.SEVERE, message);
	}

	public static LogRecord error(Object owner, String message, Throwable t) {
		return log(owner, Level.SEVERE, message, t);
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
				cname.startsWith("de.ims.icarus.logging") || //$NON-NLS-1$
				cname.startsWith("java.util.logging.LoggingProxyImpl") || //$NON-NLS-1$
				cname.startsWith("sun.util.logging.")); //$NON-NLS-1$
	}
}
