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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java.plugin.registry.Extension;

import de.ims.icarus.plugins.PluginUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ClassUtils {

	private ClassUtils() {
		// no-op
	}

	public static boolean equals(Object o1, Object o2) {
		return o1==null ? o2==null : o1.equals(o2);
	}

	public static Object instantiate(Object source) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		if(source instanceof String) {
			source = Class.forName((String) source);
		}

		if(source instanceof ClassProxy) {
			source = ((ClassProxy)source).loadClass();
		}

		if(source instanceof Class) {
			return ((Class<?>) source).newInstance();
		} else if(source instanceof Extension) {
			return PluginUtil.instantiate((Extension) source);
		}

		return source;
	}

	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type.
	 *
	 * @param type
	 *            the type
	 * @return the underlying class
	 */
	public static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type)
					.getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Get the actual type arguments a child class has used to extend a generic
	 * base class.
	 *
	 * @param baseClass
	 *            the base class
	 * @param childClass
	 *            the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass,
			Class<? extends T> childClass) {
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = childClass;
		// start walking up the inheritance hierarchy until we hit baseClass
		while (!getClass(type).equals(baseClass)) {
			if (type instanceof Class) {
				// there is no useful information for us in raw types, so just
				// keep going.
				type = ((Class<?>) type).getGenericSuperclass();
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class<?>) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
				}

				if (!rawType.equals(baseClass)) {
					type = rawType.getGenericSuperclass();
				}
			}
		}

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		if (type instanceof Class) {
			actualTypeArguments = ((Class<?>) type).getTypeParameters();
		} else {
			actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
		}
		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
		// resolve types by chasing down type variables.
		for (Type baseType : actualTypeArguments) {
			while (resolvedTypes.containsKey(baseType)) {
				baseType = resolvedTypes.get(baseType);
			}
			typeArgumentsAsClasses.add(getClass(baseType));
		}
		return typeArgumentsAsClasses;
	}

	public static int cast(Integer value) {
		return value==null ? 0 : value.intValue();
	}

	public static long cast(Long value) {
		return value==null ? 0L : value.intValue();
	}

	public static double cast(Double value) {
		return value==null ? 0D : value.doubleValue();
	}

	public static float cast(Float value) {
		return value==null ? 0F : value.floatValue();
	}

	public static short cast(Short value) {
		return value==null ? 0 : value.shortValue();
	}

	public static byte cast(Byte value) {
		return value==null ? 0 : value.byteValue();
	}

	public static boolean cast(Boolean value) {
		return value==null ? false : value.booleanValue();
	}
}
