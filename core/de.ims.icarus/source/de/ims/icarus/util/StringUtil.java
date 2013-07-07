/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.id.Identifiable;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class StringUtil {

	private StringUtil() {
		// no-op
	}

	
	private static Pattern indexPattern;
	
	public static String getName(Object obj) {
		if(obj==null) {
			return null;
		}
		
		if(obj instanceof NamedObject) {
			return ((NamedObject)obj).getName();
		}
		if(obj instanceof Identifiable) {
			obj = ((Identifiable)obj).getIdentity();
		}
		if(obj instanceof Identity) {
			return ((Identity)obj).getName();
		}
		
		return obj.toString();
	}
	
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
				LoggerFactory.log(StringUtil.class, Level.SEVERE, 
						"Failed to parse existing base name index suffix: "+name, e); //$NON-NLS-1$
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
	
	private static DecimalFormat decimalFormat = new DecimalFormat("#,###"); //$NON-NLS-1$
	
	public static synchronized String formatDecimal(int value) {
		return decimalFormat.format(value);
	}
	
	public static void trim(StringBuilder sb) {
		while(Character.isWhitespace(sb.charAt(0))) {
			sb.delete(0, 1);
		}
		while(Character.isWhitespace(sb.charAt(sb.length()-1))) {
			sb.delete(sb.length()-1, sb.length());
		}
	}
}