/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ikarus_systems.icarus.logging.LoggerFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class NamingUtil {
	
	private static Logger logger;
	
	private static Logger getLogger() {
		if(logger==null) {
			logger = LoggerFactory.getLogger(NamingUtil.class);
		}
		return logger;
	}

	private NamingUtil() {
		// no-op
	}

	
	private static Pattern indexPattern;
	
	public static String getBaseName(String name) {
		if(indexPattern==null) {
			indexPattern = Pattern.compile("\\((\\d+)\\)$"); //$NON-NLS-1$
		}
		
		Matcher matcher = indexPattern.matcher(name);
		if(matcher.find()) {
			return name.substring(0, name.length()-matcher.group().length()).trim();
		}
		
		return name;
	}
	
	public static int getCurrentCount(String name) {
		if(indexPattern==null) {
			indexPattern = Pattern.compile("\\((\\d+)\\)$"); //$NON-NLS-1$
		}
		
		Matcher matcher = indexPattern.matcher(name);
		if(matcher.find()) {
			int currentCount = 0;
			try {
				currentCount = Integer.parseInt(matcher.group(1));
			} catch(NumberFormatException e) {
				getLogger().log(LoggerFactory.record(Level.SEVERE, 
						"Failed to parse existing base name index suffix: "+name, e)); //$NON-NLS-1$
			}
			return currentCount;
		}
		
		return -1;
	}

	
	public static String getUniqueName(String baseName, Set<String> usedNames) {
		return getUniqueName(baseName, usedNames, false);
	}
	
	public static String getUniqueName(String baseName, Set<String> usedNames, boolean allowBaseName) {
		if(baseName==null)
			throw new IllegalArgumentException("invalid basename"); //$NON-NLS-1$
		if(usedNames==null)
			throw new IllegalArgumentException("invalid used name set"); //$NON-NLS-1$
		
		if(usedNames.isEmpty()) {
			return baseName;
		}
		
		if(allowBaseName) {
			usedNames.remove(baseName);
		}
		
		String name = baseName;
		int count = Math.max(2, getCurrentCount(baseName)+1);
		baseName = getBaseName(baseName);
		
		if(usedNames.contains(name)) {
			while(usedNames.contains((name = baseName+" ("+count+")"))) { //$NON-NLS-1$ //$NON-NLS-2$
				count++;
			}
		}
		
		return name;
	}
	
	public static String fit(String s, int maxLength) {
		if(s==null) {
			return ""; //$NON-NLS-1$
		}
		if(s.length()<=maxLength) {
			return s;
		}
		
		int chunkLength = (maxLength-3)/2;
		
		StringBuilder sb = new StringBuilder(maxLength);
		sb.append(s, 0, chunkLength)
		.append("...") //$NON-NLS-1$
		.append(s, chunkLength+3, maxLength-chunkLength);
		
		return sb.toString();
	}
}
