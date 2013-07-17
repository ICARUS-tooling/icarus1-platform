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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.util.id.DuplicateIdentifierException;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class Exceptions {
	
	private static Map<String, ErrorFormatter> formatters;

	private Exceptions() {
	}
	
	public static void addFormatter(String throwableClassName, ErrorFormatter formatter) {
		if(throwableClassName==null)
			throw new IllegalArgumentException("Invalid throwable class name"); //$NON-NLS-1$
		if(formatter==null)
			throw new IllegalArgumentException("Invalid formatter"); //$NON-NLS-1$
		
		if(formatters==null) {
			synchronized (Exceptions.class) {
				if(formatters==null) {
					formatters = new HashMap<>();
				}
			}
		}
		
		if(formatters.containsKey(throwableClassName))
			throw new DuplicateIdentifierException("Formatter already registered for throwable class: "+throwableClassName); //$NON-NLS-1$
		
		formatters.put(throwableClassName, formatter);
	}
	
	public static String getFormattedMessage(Throwable t) {
		if(formatters==null || formatters.isEmpty()) {
			return t.getMessage();
		}
		
		String message = null;
		ErrorFormatter formatter = formatters.get(t.getClass().getName());
		if(formatter!=null) {
			message = formatter.getMessage(t);
		}
		
		if(message==null) {
			message = t.getMessage();
		}
		
		return message;
	}

	public static void testNullArgument(Object o, String name) {
		if (o == null)
			throw new IllegalArgumentException("Invalid argument: " + name); //$NON-NLS-1$
	}

	public static final void testNonemptyArray(Object[] o, String name) {
		if (o == null || o.length == 0)
			throw new IllegalArgumentException(
					"Invalid array argument (null or empty): " + name); //$NON-NLS-1$
	}

	public static String stackTraceToString(Throwable thr) {
		Exceptions.testNullArgument(thr, "throwable"); //$NON-NLS-1$
		StringWriter sw = new StringWriter(1000);
		thr.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static void testArgumentClass(Object o, Class<?> clazz,
			String name) {
		Exceptions.testNullArgument(o, "o"); //$NON-NLS-1$
		Exceptions.testNullArgument(clazz, "clazz"); //$NON-NLS-1$
		
		if (!clazz.isAssignableFrom(o.getClass()))
			throw new IllegalArgumentException(String.format(
					"Argument '%s' is not of required class '%s'", name, clazz //$NON-NLS-1$
							.getName()));
	}
	
	public static void testBounds(int value, int min, int max, String name) {
		if(value<min || value>max)
			throw new IllegalArgumentException(String.format(
					"Argument '%s' is out of range [%d to %d]: %d",  //$NON-NLS-1$
					name, min, max, value));
	}
}