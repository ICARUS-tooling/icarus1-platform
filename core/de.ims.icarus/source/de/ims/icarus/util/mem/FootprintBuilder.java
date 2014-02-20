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
package de.ims.icarus.util.mem;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Filter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FootprintBuilder {

	private static Map<Object, FootprintBuilder> builderMap = new WeakHashMap<>();

	public static FootprintBuilder getSharedBuilder(Object context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		synchronized (builderMap) {
			FootprintBuilder builder = builderMap.get(context);

			if(builder==null) {
				builder = new FootprintBuilder();

				builderMap.put(context, builder);
			}

			return builder;
		}
	}

	private static final MemoryCalculator emptyCalculator = new MemoryCalculator() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return buffer.addReference();
		}
	};

//	private final MemoryCalculator stringCalculator = new GeneralCalculator(String.class) {
//
//
//		@Override
//		public long appendFootprint(Object obj, FootprintBuffer buffer,
//				ObjectCache cache) {
//			long footprint = super.appendFootprint(obj, buffer, cache);
//
//			buffer.addStringFootprint(footprint);
//
//			return footprint;
//		}
//	};

	private Set<Class<?>> whitelist = new HashSet<>();
	private Set<Class<?>> blacklist = new HashSet<>();
	// Act as exclusion classFilters
	private Set<Filter> classFilters = new HashSet<>();
	private Set<Filter> objectFilters = new HashSet<>();

	private boolean isIgnored(Class<?> clazz) {
		if(whitelist.contains(clazz)) {
			return false;
		}

		if(blacklist.contains(clazz)) {
			return true;
		}

		if(!classFilters.isEmpty()) {
			for(Filter filter : classFilters) {
				if(filter.accepts(clazz)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isIgnored(Object obj) {

		if(!objectFilters.isEmpty()) {
			for(Filter filter : objectFilters) {
				if(filter.accepts(obj)) {
					return true;
				}
			}
		}

		return false;
	}

	private Map<Class<?>, MemoryCalculator> calculators = new HashMap<>();

	private MemoryCalculator getCalculator(Object obj) {
		if (obj == null)
			throw new NullPointerException("Invalid obj"); //$NON-NLS-1$

		if(isIgnored(obj)) {
			return emptyCalculator;
		} else {
			return getCalculator(obj.getClass());
		}
	}

	private MemoryCalculator getCalculator(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		if(clazz.isArray()) {
			return clazz.getComponentType().isPrimitive() ?
					primitiveArrayCalculator : arrayCalculator;
		}

		MemoryCalculator calculator = calculators.get(clazz);

		if(calculator==null) {

			if(isIgnored(clazz)) {
				calculator = emptyCalculator;
			} else {
				HeapMember heapMember = clazz.getAnnotation(HeapMember.class);

				if(heapMember!=null) {
					calculator = new HeapMemberCalculator(clazz);
				} else {
					calculator = new GeneralCalculator(clazz);
				}
			}

			calculators.put(clazz, calculator);
		}

		return calculator;
	}

	public FootprintBuilder() {
//		calculators.put(String.class, stringCalculator);
	}

	public void addToWhitelist(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		whitelist.add(clazz);
	}

	public <E extends Object> void addToWhitelist(Collection<Class<E>> classes) {
		if (classes == null)
			throw new NullPointerException("Invalid classes"); //$NON-NLS-1$

		whitelist.addAll(classes);
	}

	public void addToBlacklist(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		blacklist.add(clazz);
	}

	public <E extends Object> void addToBlacklist(Collection<Class<E>> classes) {
		if (classes == null)
			throw new NullPointerException("Invalid classes"); //$NON-NLS-1$

		blacklist.addAll(classes);
	}

	public void addClassFilter(Filter filter) {
		if (filter == null)
			throw new NullPointerException("Invalid filter"); //$NON-NLS-1$

		classFilters.add(filter);
	}

	public synchronized MemoryFootprint calculateFootprint(Object root) {
		if (root == null)
			throw new NullPointerException("Invalid root"); //$NON-NLS-1$

		FootprintBuffer buffer = new FootprintBuffer(root);
		ObjectCache cache = new ObjectCache();

		buffer.start();

		getCalculator(root).appendFootprint(root, buffer, cache);

		buffer.finalizeFootprint();

		return buffer;
	}

	private class GeneralCalculator implements MemoryCalculator {

		private final Class<?> clazz;
		private final MemoryCalculator parent;

		private List<Class<?>> primitiveFields;
		private List<Field> complexFields;

		GeneralCalculator(Class<?> clazz) {
			if (clazz == null)
				throw new NullPointerException("Invalid clazz");  //$NON-NLS-1$

			this.clazz = clazz;

			Class<?> parentClazz = clazz.getSuperclass();
			MemoryCalculator parent = null;
			if(parentClazz!=null && parentClazz!=Object.class) {
				parent = getCalculator(parentClazz);
			}
			this.parent = parent;

			for(Field field : clazz.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				Class<?> type = field.getType();

				if(type.isPrimitive()) {
					if(primitiveFields==null) {
						primitiveFields = new ArrayList<>(5);
					}
					primitiveFields.add(type);
				} else {
					if(complexFields==null) {
						complexFields = new ArrayList<>(5);
					}
					field.setAccessible(true);
					complexFields.add(field);
				}
			}
		}

		/**
		 * @see de.ims.icarus.util.mem.MemoryCalculator#appendFootprint(java.lang.Object, de.ims.icarus.util.mem.FootprintBuffer, de.ims.icarus.util.mem.ObjectCache)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {

			long footprint = 0;

			if(parent!=null) {
				footprint += parent.appendFootprint(obj, buffer, cache);
			}

			if(primitiveFields!=null) {
				for(int i=primitiveFields.size()-1; i>-1; i--) {
					footprint += buffer.addPrimitive(primitiveFields.get(i));
				}
			}

			if(complexFields!=null) {
				for(int i=complexFields.size()-1; i>-1; i--) {
					Field field = complexFields.get(i);
					try {
						Object value = field.get(obj);

						// If value is null or already cached simply count reference
						if(value==null || !cache.addIfAbsent(value)) {
							footprint += buffer.addReference();
						} else {
							long fp = getCalculator(value).appendFootprint(value, buffer, cache);
							buffer.addObject(value.getClass(), fp);
							footprint += fp;
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new CorruptedStateException("Unable to calculate footprint of class: "+clazz, e); //$NON-NLS-1$
					}
				}
			}

			return footprint;
		}
	}

	private class HeapMemberCalculator implements MemoryCalculator {

		private final Class<?> clazz;
		private final MemoryCalculator parent;

		private List<Class<?>> primitiveFields;
		private List<FieldHandler> complexFields;

		HeapMemberCalculator(Class<?> clazz) {
			if (clazz == null)
				throw new NullPointerException("Invalid clazz");  //$NON-NLS-1$
			if(clazz.getAnnotation(HeapMember.class)==null)
				throw new IllegalArgumentException("Missing @HeapMember annotation on type: "+clazz); //$NON-NLS-1$

			this.clazz = clazz;

			Class<?> parentClazz = clazz.getSuperclass();
			MemoryCalculator parent = null;
			if(parentClazz!=null && parentClazz!=Object.class) {
				parent = getCalculator(parentClazz);
			}
			this.parent = parent;

			for(Field field : clazz.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				Class<?> type = field.getType();

				Primitive primitive = field.getAnnotation(Primitive.class);
				if(primitive!=null) {
					if(!type.isPrimitive())
						throw new IllegalArgumentException("Illegal @Primitive annotation on non-primitive field: "+field); //$NON-NLS-1$

					if(primitiveFields==null) {
						primitiveFields = new ArrayList<>(5);
					}
					primitiveFields.add(type);
					continue;
				}

				Reference reference = field.getAnnotation(Reference.class);
				if(reference!=null) {
					if(type.isPrimitive())
						throw new IllegalArgumentException("Illegal @Reference annotation on primitive field: "+field); //$NON-NLS-1$

					if(complexFields==null) {
						complexFields = new ArrayList<>(5);
					}
					complexFields.add(getDefaultLinkdHandler(reference.value()));
					continue;
				}

				Link link = field.getAnnotation(Link.class);
				if(link!=null) {
					if(type.isPrimitive())
						throw new IllegalArgumentException("Illegal @Reference annotation on primitive field: "+field); //$NON-NLS-1$

					if(complexFields==null) {
						complexFields = new ArrayList<>(5);
					}

					field.setAccessible(true);

					if(type.isArray()) {
						if(type.getComponentType().isPrimitive()) {
							complexFields.add(new PrimitiveArrayFieldHandler(field));
						} else {
							complexFields.add(new ArrayFieldHandler(field));
						}
					} else {
						complexFields.add(new DefaultLinkHandler(field, link));
					}
					continue;
				}
			}
		}

		/**
		 * @see de.ims.icarus.util.mem.MemoryCalculator#appendFootprint(java.lang.Object, de.ims.icarus.util.mem.FootprintBuffer)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer, ObjectCache cache) {
			long footprint = 0;

			if(parent!=null) {
				footprint += parent.appendFootprint(obj, buffer, cache);
			}

			if(primitiveFields!=null) {
				for(int i=primitiveFields.size()-1; i>-1; i--) {
					footprint += buffer.addPrimitive(primitiveFields.get(i));
				}
			}

			if(complexFields!=null) {
				for(int i=complexFields.size()-1; i>-1; i--) {
					try {
						footprint += complexFields.get(i).appendFootprint(obj, buffer, cache);
					} catch (Exception e) {
						throw new CorruptedStateException("Unable to calculate footprint of class: "+clazz, e); //$NON-NLS-1$
					}
				}
			}

			return footprint;
		}

	}

	private static FieldHandler getDefaultLinkdHandler(ReferenceType type) {
		switch (type) {
		case DOWNLINK:
			return downlinkHandler;

		case UPLINK:
			return uplinkHandler;

		default:
			return unknownLinkHandler;
		}
	}

	private static long addPrimitiveArray(Object array, FootprintBuffer buffer) {
		if(array==null) {
			return buffer.addReference();
		} else {
			return buffer.addPrimitiveArray(array.getClass().getComponentType(), array);
		}
	}

	private long addArray(Object array, FootprintBuffer buffer, ObjectCache cache) {
		if (array == null)
			throw new NullPointerException("Invalid array");

		long footprint = buffer.addArray(array);

		int size = Array.getLength(array);
		for(int i=0; i<size; i++) {
			Object value = Array.get(array, i);

			if(value==null) {
				continue;
			}

			if(value.getClass().isArray()) {
				footprint += addArray(value, buffer, cache);
			} else {
				long fp = getCalculator(value).appendFootprint(value, buffer, cache);

				buffer.addObject(value.getClass(), fp);

				footprint += fp;
			}
		}

		return footprint;
	}

	private MemoryCalculator arrayCalculator = new MemoryCalculator() {

		/**
		 * @see de.ims.icarus.util.mem.MemoryCalculator#appendFootprint(java.lang.Object, de.ims.icarus.util.mem.FootprintBuffer, de.ims.icarus.util.mem.ObjectCache)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return addArray(obj, buffer, cache);
		}

	};

	private MemoryCalculator primitiveArrayCalculator = new MemoryCalculator() {

		/**
		 * @see de.ims.icarus.util.mem.MemoryCalculator#appendFootprint(java.lang.Object, de.ims.icarus.util.mem.FootprintBuffer, de.ims.icarus.util.mem.ObjectCache)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return addPrimitiveArray(obj, buffer);
		}

	};

	private interface FieldHandler {

		long appendFootprint(Object obj, FootprintBuffer buffer, ObjectCache cache) throws Exception;
	}

	private class DefaultLinkHandler implements FieldHandler {

		private final Field field;
		private final Link link;

		DefaultLinkHandler(Field field, Link link) {
			if (field == null)
				throw new NullPointerException("Invalid field"); //$NON-NLS-1$
			if (link == null)
				throw new NullPointerException("Invalid link"); //$NON-NLS-1$

			this.field = field;
			this.link = link;
		}

		/**
		 * @see de.ims.icarus.util.mem.FootprintBuilder.FieldHandler#appendFootprint(java.lang.Object, de.ims.icarus.util.mem.FootprintBuffer, de.ims.icarus.util.mem.ObjectCache)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) throws Exception {
			Object value = field.get(obj);

			// Simple count reference in case the value is null or
			// already cached (given that caching is active)
			if(value==null || (link.cache() && !cache.addIfAbsent(value))) {
				return getDefaultLinkdHandler(link.type()).appendFootprint(obj, buffer, cache);
			} else {
				long fp = getCalculator(value).appendFootprint(value, buffer, cache);

				buffer.addObject(value.getClass(), fp);

				return fp;
			}
		}

	}

	private class ArrayFieldHandler implements FieldHandler {

		private final Field field;

		ArrayFieldHandler(Field field) {
			if (field == null)
				throw new NullPointerException("Invalid field"); //$NON-NLS-1$

			this.field = field;
		}

		/**
		 * @see de.ims.icarus.util.mem.FootprintBuilder.FieldHandler#appendFootprint(java.lang.Object, de.ims.icarus.util.mem.FootprintBuffer, de.ims.icarus.util.collections.IdentityHashSet)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) throws Exception {
			return addArray(field.get(obj), buffer, cache);
		}

	}

	private class PrimitiveArrayFieldHandler implements FieldHandler {

		private final Field field;

		PrimitiveArrayFieldHandler(Field field) {
			if (field == null)
				throw new NullPointerException("Invalid field"); //$NON-NLS-1$

			this.field = field;
		}

		/**
		 * @see de.ims.icarus.util.mem.FootprintBuilder.FieldHandler#appendFootprint(java.lang.Object, de.ims.icarus.util.mem.FootprintBuffer, de.ims.icarus.util.collections.IdentityHashSet)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) throws Exception {
			return buffer.addPrimitiveArray(field.getType(), field.get(obj));
		}

	}

	private static final FieldHandler unknownLinkHandler = new FieldHandler() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return buffer.addReference();
		}
	};

	private static final FieldHandler uplinkHandler = new FieldHandler() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return buffer.addUplink();
		}
	};

	private static final FieldHandler downlinkHandler = new FieldHandler() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return buffer.addDownlink();
		}
	};
}
