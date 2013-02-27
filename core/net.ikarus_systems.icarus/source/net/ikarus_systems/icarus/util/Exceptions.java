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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Exceptions {

	private Exceptions() {
	}

	public static final void testNullArgument(Object o, String name) {
		if (o == null)
			throw new IllegalArgumentException("Invalid argument: " + name); //$NON-NLS-1$
	}

	public static final void testNonemptyArray(Object[] o, String name) {
		if (o == null || o.length == 0)
			throw new IllegalArgumentException(
					"Invalid array argument (null or empty): " + name); //$NON-NLS-1$
	}

	public static final String stackTraceToString(Throwable thr) {
		Exceptions.testNullArgument(thr, "throwable"); //$NON-NLS-1$
		StringWriter sw = new StringWriter(1000);
		thr.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static final void testArgumentClass(Object o, Class<?> clazz,
			String name) {
		Exceptions.testNullArgument(o, "o"); //$NON-NLS-1$
		Exceptions.testNullArgument(clazz, "clazz"); //$NON-NLS-1$
		
		if (!clazz.isAssignableFrom(o.getClass()))
			throw new IllegalArgumentException(String.format(
					"Argument '%s' is not of required class '%s'", name, clazz //$NON-NLS-1$
							.getName()));
	}
	
	public static final void testBounds(int value, int min, int max, String name) {
		if(value<min || value>max)
			throw new IllegalArgumentException(String.format(
					"Argument '%s' is out of range [%d to %d]: %d",  //$NON-NLS-1$
					name, min, max, value));
	}
}