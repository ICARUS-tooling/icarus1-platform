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
package de.ims.icarus.util;

import java.awt.Component;
import java.awt.FontMetrics;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.helper.TextItem;
import de.ims.icarus.util.id.Identifiable;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.intern.Interner;
import de.ims.icarus.util.intern.StrongInterner;
import de.ims.icarus.util.intern.WeakInterner;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class StringUtil {
	
	public static final String WEAK_INTERN_PROPERTY = 
			"de.ims.icarus.strings.useWeakIntern"; //$NON-NLS-1$

	private StringUtil() {
		// no-op
	}

	private static Interner<String> interner;
	private static final int defaultInternerCapacity = 500;
	
	public static String intern(String s) {
		Interner<String> i = interner;
		if(i==null) {
			synchronized (StringUtil.class) {
				i = interner;
				if(i==null) {
					if("true".equals(Core.getCore().getProperty(WEAK_INTERN_PROPERTY, "true"))) { //$NON-NLS-1$ //$NON-NLS-2$
						interner = new WeakInterner<>(defaultInternerCapacity);
					} else {
						interner = new StrongInterner<>(defaultInternerCapacity);
					}
				}
			}
		}
		
		return interner.intern(s);
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
	
	public static String asText(Object obj) {
		if(obj==null) {
			return null;
		}
		
		if(obj instanceof TextItem) {
			return ((TextItem)obj).getText();
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
			throw new NullPointerException("Invalid basename"); //$NON-NLS-1$
		if(usedNames==null)
			throw new NullPointerException("Invalid used name set"); //$NON-NLS-1$
		
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
	
	public static String formatDecimal(int value) {
		synchronized (decimalFormat) {
			return decimalFormat.format(value);
		}
	}

	private static DecimalFormat fractionDecimalFormat = new DecimalFormat("#,##0.00"); //$NON-NLS-1$

	public static String formatShortenedDecimal(double value) {
		synchronized (fractionDecimalFormat) {
			if(value>=1_000_000_000) {
				return fractionDecimalFormat.format(value/1_000_000_000)+'G';
			} else if(value>=1_000_000) {
				return fractionDecimalFormat.format(value/1_000_000)+'M';
			} else if(value>=1_000) {
				return fractionDecimalFormat.format(value/1_000)+'K';
			} else {
				return formatDecimal((int) value);
				
			}
		}
	}

	public static String formatShortenedDecimal(int value) {
		if(value>=1_000_000_000) {
			return formatDecimal(value/1_000_000_000)+'G';
		} else if(value>=1_000_000) {
			return formatDecimal(value/1_000_000)+'M';
		} else if(value>=1_000) {
			return formatDecimal(value/1_000)+'K';
		} else {
			return formatDecimal(value);
			
		}
	}
	
	public static void trim(StringBuilder sb) {
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(0))) {
			sb.delete(0, 1);
		}
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(sb.length()-1))) {
			sb.delete(sb.length()-1, sb.length());
		}
	}
	
	public static final int MIN_WRAP_WIDTH = 50;

	public static String wrap(String s, Component comp, int width) {
		return wrap(s, comp.getFontMetrics(comp.getFont()), width);
	}
	
	/**
	 * Wraps a given {@code String} so that its lines do not exceed
	 * the specified {@code width} value in length. 
	 */
	public static String wrap(String s, FontMetrics fm, int width) {
		if(fm==null)
			throw new NullPointerException("Invalid font metrics"); //$NON-NLS-1$
		//if(width<MIN_WRAP_WIDTH)
		//	throw new IllegalArgumentException("Width must not be less than "+MIN_WRAP_WIDTH+" pixels"); //$NON-NLS-1$ //$NON-NLS-2$
		
		if(s==null || s.length()==0) {
			return s;
		}
		
		// FIXME if input string contains multiple linebreaks the last one will be omitted
		
		StringBuilder sb = new StringBuilder();
		StringBuilder line = new StringBuilder();
		StringBuilder block = new StringBuilder();
		
		int size = s.length();
		int len = 0;
		int blen = 0;
		for(int i=0; i<size; i++) {
			char c = s.charAt(i);
			boolean lb = false;
			if(c=='\r') {
				continue;
			} else if(c=='\n' || isBreakable(c)) {
				lb = c=='\n';
				if(!lb) {
					block.append(c);
					blen += fm.charWidth(c);
				}
				line.append(block);
				block.setLength(0);
				len += blen;
				blen = 0;
			} else {
				block.append(c);
				blen += fm.charWidth(c);
				lb = (len+blen) >= width;
			}
			
			if(lb && sb.length()>0) {
				sb.append('\n');
			}
			if(i==size-1) {
				line.append(block);
			}
			if(lb || i==size-1) {
				sb.append(line.toString().trim());
				line.setLength(0);
				len = 0;
			}
		}
		
		return sb.toString();
	}
	
	public static String[] split(String s, Component comp, int width) {
		return split(s, comp.getFontMetrics(comp.getFont()), width);
	}
	
	public static String[] split(String s, FontMetrics fm, int width) {
		if(fm==null)
			throw new NullPointerException("Invalid font metrics"); //$NON-NLS-1$
		//if(width<MIN_WRAP_WIDTH)
		//	throw new IllegalArgumentException("Width must not be less than "+MIN_WRAP_WIDTH+" pixels"); //$NON-NLS-1$ //$NON-NLS-2$
		
		if(s==null || s.length()==0) {
			return new String[0];
		}
		
		List<String> result = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		StringBuilder block = new StringBuilder();
		
		int size = s.length();
		int len = 0;
		int blen = 0;
		for(int i=0; i<size; i++) {
			char c = s.charAt(i);
			boolean lb = false;
			if(c=='\r') {
				continue;
			} else if(c=='\n' || isBreakable(c)) {
				lb = c=='\n';
				if(!lb) {
					block.append(c);
					blen += fm.charWidth(c);
				}
				line.append(block);
				block.setLength(0);
				len += blen;
				blen = 0;
			} else {
				block.append(c);
				blen += fm.charWidth(c);
				lb = (len+blen) >= width;
			}
			
			if(i==size-1) {
				line.append(block);
				lb = true;
			}
			
			if(lb) {
				result.add(line.toString().trim());
				line.setLength(0);
				len = 0;
			}
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	private static boolean isBreakable(char c) {
		return Character.isWhitespace(c) || !Character.isLetterOrDigit(c);
	}
	
	public static String capitalize(String s) {
		if(s==null || s.length()<2)
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$
		
		return Character.toUpperCase(s.charAt(0))+s.substring(1);
	}
	
	public static String join(String[] tokens) {
		return join(tokens, ", ", '[', ']'); //$NON-NLS-1$
	}
	
	public static String join(String[] tokens, String separator, char start, char end) {
		if(tokens==null || tokens.length==0) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append(start);		
		for(int i=0; i<tokens.length; i++) {
			if(i>0) {
				sb.append(separator);
			}
			sb.append(tokens[i]);
		}
		sb.append(end);
		
		return sb.toString();
	}
	
	public static int compareNumberAwareIgnoreCase(String s1, String s2) {
		try {
			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);
			
			return i1-i2;
		} catch(NumberFormatException e) {
			// ignore
		}
		
		return s1.compareToIgnoreCase(s2);
	}
	
	public static int compareNumberAware(String s1, String s2) {
		try {
			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);
			
			return i1-i2;
		} catch(NumberFormatException e) {
			// ignore
		}
		
		return s1.compareToIgnoreCase(s2);
	}
}
