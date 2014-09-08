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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.java.plugin.registry.Extension;

import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.util.types.DefaultIconLink;
import de.ims.icarus.model.util.types.DefaultLink;
import de.ims.icarus.model.util.types.Url;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.sax.IconWrapper;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TestUtils {

    private static boolean equalsRegardingNull(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }

        return isEquals(expected, actual);
    }

    private static boolean isEquals(Object expected, Object actual) {
        return expected.equals(actual);
    }

	public static void assertHashEquals(Object expected, Object actual) {
		assertHashEquals(null, expected, actual);
	}

	public static void assertObjectContract(Object obj) {
		assertFalse(obj.equals(null));
		assertFalse(obj.equals(new Dummy()));
		assertNotNull(obj.toString());
	}

	public static void assertHashEquals(String message, Object expected, Object actual) {
		assertNotNull("Expected", expected); //$NON-NLS-1$
		assertNotNull("Actual", actual); //$NON-NLS-1$

		int expectedHash = expected.hashCode();
		int actualHash = actual.hashCode();

		boolean isEquals = isEquals(expected, actual);
		boolean isHashEquals = expectedHash==actualHash;

		if(isEquals && !isHashEquals) {
			failForEquality(message, expectedHash, actualHash);
		} else if(!isEquals && isHashEquals) {
			failForInequality(message, expectedHash);
		}
	}

	private static void failForEquality(String message, int expectedHash, int actualHash) {
        String formatted = ""; //$NON-NLS-1$
        if (message != null) {
            formatted = message + " "; //$NON-NLS-1$
        }

        fail(formatted+"expected hash "+expectedHash+" for two equal objects, but got: "+actualHash); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void failForInequality(String message, int hash) {
        String formatted = ""; //$NON-NLS-1$
        if (message != null) {
            formatted = message + " "; //$NON-NLS-1$
        }

        fail(formatted+"expected hash different to "+hash+" for two inequal objects"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static Extension dummyExtension(final String id) {
		Extension extension = mock(Extension.class);

		when(extension.getId()).thenReturn(id);
		when(extension.getUniqueId()).thenReturn("<no_plugin>@"+id); //$NON-NLS-1$
//		when(extension.equals(anything())).thenReturn(true);
//		when(extension.hashCode()).thenReturn(id.hashCode());

		return extension;
	}

	private static final Map<ValueType, Object[]> testValues = new HashMap<>();

	private static final Extension[] extensions = {
		dummyExtension("extension1"), //$NON-NLS-1$
		dummyExtension("extension2"), //$NON-NLS-1$
		dummyExtension("extension3"), //$NON-NLS-1$
	};

	public enum TestEnum {
		TEST1,
		TEST2,
		TEST3
	}

//	public static final ValueType EXTENSION_TYPE = spy(ValueType.EXTENSION);
//	static {
//		doReturn(extensions[0]).when(EXTENSION_TYPE).parse("extension1", null); //$NON-NLS-1$
//		doReturn(extensions[1]).when(EXTENSION_TYPE).parse("extension2", null); //$NON-NLS-1$
//		doReturn(extensions[2]).when(EXTENSION_TYPE).parse("extension3", null); //$NON-NLS-1$
//	}

	private static void addTestValues(ValueType type, Object...values) {
		testValues.put(type, values);
	}
	static {
		addTestValues(ValueType.STRING, "test1", "test2", "test3");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		addTestValues(ValueType.INTEGER, 1, 20, 300);
		addTestValues(ValueType.LONG, 1L, 20L, 300L);
		addTestValues(ValueType.FLOAT, 1.1F, 2.5F, 3F);
		addTestValues(ValueType.DOUBLE, 1.765324D, 2.56789D, -3D);
		addTestValues(ValueType.BOOLEAN, true, false);
		addTestValues(ValueType.ENUM, (Object[]) TestEnum.values());
		addTestValues(ValueType.IMAGE,
				new IconWrapper("testIconName1"), //$NON-NLS-1$
				new IconWrapper("testIconName2"), //$NON-NLS-1$
				new IconWrapper("testIconName3")); //$NON-NLS-1$

		try {
			addTestValues(ValueType.URL,
					new Url("http://www.uni-stuttgart.de"), //$NON-NLS-1$
					new Url("http://www.uni-stuttgart.de/linguistik"), //$NON-NLS-1$
					new Url("http://www.dict.cc")); //$NON-NLS-1$
		} catch(MalformedURLException e) {
			// ignore
		}

		try {
			addTestValues(ValueType.URL_RESOURCE,
					new DefaultLink(new Url("http://www.uni-stuttgart.de"), "Url-Link 1", "Some test link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultLink(new Url("http://www.uni-stuttgart.de/linguistik"), "Url-Link 2", "Another link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					new DefaultLink(new Url("http://www.dict.cc"), "Url-Link 3 (no desciption)")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(MalformedURLException e) {
			// ignore
		}

		addTestValues(ValueType.IMAGE_RESOURCE,
				new DefaultIconLink(new IconWrapper("testIconName1"), "Icon-Link 1", "Some test icon link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new DefaultIconLink(new IconWrapper("testIconName2"), "Icon-Link 2", "Some test icon link"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new DefaultIconLink(new IconWrapper("testIconName3"), "Icon-Link 3 (no description)")); //$NON-NLS-1$ //$NON-NLS-2$$

		addTestValues(ValueType.UNKNOWN, new Object(), new int[3], 456);
		addTestValues(ValueType.CUSTOM, new Dummy(), new Dummy(), new Dummy());

		addTestValues(ValueType.EXTENSION,
				"my.plugin@extension1",
				"my.plugin@extension2",
				"my.plugin2@extension1");

		//FIXME add some test values for the other more complex types!
	}

	public static Object[] getTestValues(ValueType type) {
//		if(type==EXTENSION_TYPE) {
//			type = ValueType.EXTENSION;
//		}
		return testValues.get(type);
	}

	public static Object getTestValue(ValueType type) {
		return testValues.get(type)[0];
	}

	private static final Set<String> methodBlacklist = new HashSet<>();
	static {
		methodBlacklist.add("getId"); //$NON-NLS-1$
		methodBlacklist.add("getOwner"); //$NON-NLS-1$
		methodBlacklist.add("getTemplate"); //$NON-NLS-1$
	}

	private static String getId(Object manifest) {
		String id = null;

		if(manifest instanceof Manifest) {
			id = ((Manifest)manifest).getId();
		} else if(manifest instanceof Identity) {
			id = ((Identity)manifest).getId();
		}

		if(id==null) {
			id = manifest.getClass()+"@<unnamed>"; //$NON-NLS-1$
		}
		return id;
	}

	public static void assertTemplateGetters(Class<?> interfaceClass, Object instance, Object template) throws Exception {

		for(Method method : interfaceClass.getMethods()) {

			// Ignore getters that rely on parameters
			if(method.getParameterTypes().length>0 || method.isVarArgs()) {
				continue;
			}

			// Ignore non getter methods
			if(methodBlacklist.contains(method.getName()) || !method.getName().startsWith("get")) { //$NON-NLS-1$
				continue;
			}

			Class<?> resultType = method.getReturnType();

			Object instanceValue = null, templateValue = null;

			try {
				instanceValue = method.invoke(instance);
			} catch(Exception e) {
				failForInvocation(method, instance, e);
			}

			try {
				templateValue = method.invoke(template);
			} catch(Exception e) {
				failForInvocation(method, instance, e);
			}

			if(resultType.isPrimitive() || Collection.class.isAssignableFrom(resultType)) {
				// Use equality for wrapped primitives or collection types
				assertEquals("Method '"+method+"' ignored template value", templateValue, instanceValue); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Use identity for all objects
				assertSame("Method '"+method+"' ignored template value", templateValue, instanceValue); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	private static void failForInvocation(Method method, Object target, Exception exception) {
		String message = " Invoking method '"+method+"' on target '"+getId(target)+"' failed: \n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		message += Exceptions.stackTraceToString(exception);

		throw new AssertionError(message, exception);
	}

	private static class Dummy {
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Dummy@"+hashCode(); //$NON-NLS-1$
		}
	}
}
