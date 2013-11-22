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
package de.ims.icarus.util.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;

import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.Filter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CollectionUtils {
	
	private static Map<Class<?>, Map<Object, Object>> proxyMaps;
	private static final Object proxyLock = new Object();
	
	private CollectionUtils() {
		// no-op
	}
	
	public static <E extends Object> Set<E> getSetProxy(Set<E> set) {
		if(set==null)
			throw new NullPointerException("Invalid set"); //$NON-NLS-1$
		
		synchronized (proxyLock) {
			if(proxyMaps==null) {
				proxyMaps = new HashMap<>();
			}
			
			Map<Object, Object> proxies = proxyMaps.get(Set.class);
			if(proxies==null) {
				proxies = new WeakHashMap<>();
				proxyMaps.put(Set.class, proxies);
			}
			
			@SuppressWarnings("unchecked")
			Set<E> proxy = (Set<E>) proxies.get(set);
			if(proxy==null) {
				proxy = Collections.unmodifiableSet(set);
				proxies.put(set, proxy);
			}
			
			return proxy;
		}
	}
	
	public static <E extends Object> Collection<E> getCollectionProxy(Collection<E> collection) {
		if(collection==null)
			throw new NullPointerException("Invalid collection"); //$NON-NLS-1$
		
		synchronized (proxyLock) {
			if(proxyMaps==null) {
				proxyMaps = new HashMap<>();
			}
			
			Map<Object, Object> proxies = proxyMaps.get(Collection.class);
			if(proxies==null) {
				proxies = new WeakHashMap<>();
				proxyMaps.put(Collection.class, proxies);
			}
			
			@SuppressWarnings("unchecked")
			Collection<E> proxy = (Collection<E>) proxies.get(collection);
			if(proxy==null) {
				proxy = Collections.unmodifiableCollection(collection);
				proxies.put(collection, proxy);
			}
			
			return proxy;
		}
	}
	
	public static <E extends Object> List<E> getListProxy(List<E> list) {
		if(list==null)
			throw new NullPointerException("Invalid list"); //$NON-NLS-1$
		
		synchronized (proxyLock) {
			if(proxyMaps==null) {
				proxyMaps = new HashMap<>();
			}
			
			Map<Object, Object> proxies = proxyMaps.get(List.class);
			if(proxies==null) {
				proxies = new WeakHashMap<>();
				proxyMaps.put(List.class, proxies);
			}
			
			@SuppressWarnings("unchecked")
			List<E> proxy = (List<E>) proxies.get(list);
			if(proxy==null) {
				proxy = Collections.unmodifiableList(list);
				proxies.put(list, proxy);
			}
			
			return proxy;
		}
	}
	
	public static <K extends Object, V extends Object> Map<K, V> getMapProxy(Map<K, V> map) {
		if(map==null)
			throw new NullPointerException("Invalid map"); //$NON-NLS-1$
		
		synchronized (proxyLock) {
			if(proxyMaps==null) {
				proxyMaps = new HashMap<>();
			}
			
			Map<Object, Object> proxies = proxyMaps.get(Map.class);
			if(proxies==null) {
				proxies = new WeakHashMap<>();
				proxyMaps.put(Map.class, proxies);
			}
			
			@SuppressWarnings("unchecked")
			Map<K, V> proxy = (Map<K, V>) proxies.get(map);
			if(proxy==null) {
				proxy = Collections.unmodifiableMap(map);
				proxies.put(map, proxy);
			}
			
			return proxy;
		}
	}
	
	public static boolean equals(Object o1, Object o2) {
		if(o1==null || o2==null) {
			return false;
		}
		
		return o1.equals(o2);
	}
	
	/**
	 * Tests whether a specific key maps to some value that represents
	 * the boolean value {@code true} either directly by being of type
	 * {@code boolean} or in textual form such that a call to
	 * {@link Boolean#parseBoolean(String)} returns {@code true}.
	 * If the {@code map} argument is {@code null} then the return value
	 * is {@code false};
	 */
	public static boolean isTrue(Map<?, ?> map, Object key) {
		if(map==null || map.isEmpty()) {
			return false;
		}
		
		Object value = map.get(key);
		if(value instanceof Boolean) {
			return (boolean)value;
		} else if(value instanceof String) {
			return Boolean.parseBoolean((String)value);
		}
		
		return false;
	}

	/**
	 * Checks whether the given {@code key} is mapped to an object equal
	 * to the {@code value} parameter. If the {@code map} argument is 
	 * {@code null} then the return value is {@code true} in case that
	 * the {@code value} parameter is {@code null} and {@code false} in any
	 * other case.
	 */
	public static boolean equals(Map<?, ?> map, Object key, Object value) {
		if(map==null || map.isEmpty()) {
			return value==null;
		}
		
		Object v = map.get(key);
		if(v==null) {
			return value==null;
		} else {
			return value.equals(v);
		}
	}
	
	public static <V extends Object> V get(Map<?, V> map, Object key) {
		return map==null ? null : map.get(key);
	}
	
	public static <T extends Object> Collection<T> filter(Collection<T> col, Filter filter) {
		Collection<T> result = new LinkedList<>();
		
		for(T element : col) {
			if(filter==null || filter.accepts(element)) {
				result.add(element);
			}
		}
		
		return result;
	}
	
	public static <T extends Object> boolean contains(Iterable<T> iterable, T target) {
		if(iterable!=null) {
			for(Object item : iterable) {
				if(target.equals(item)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static <T extends Object> boolean contains(T[] array, T target) {
		return indexOf(array, target)!=-1;
	}
	
	public static <T extends Object> int indexOf(T[] array, T target) {
		if(array!=null) {
			for(int i=0; i<array.length; i++) {
				if(target.equals(array[i])) {
					return i;
				}
			}
		}
		return -1;
	}

    @SafeVarargs
	public static <T extends Object> void feedItems(
			Collection<T> collection, T...items) {
		if(items==null || items.length==0) {
			return;
		}
		for(T item : items) {
			collection.add((T)item);
		}
	}

    @SafeVarargs
	public static <T extends Object> Set<T> asSet(T...items) {
    	int size = items==null ? 0 : items.length;
		Set<T> set = new HashSet<>(size);
		feedItems(set, items);
		return set;
	}

    @SafeVarargs
	public static <T extends Object> List<T> asList(T...items) {
    	int size = items==null ? 0 : items.length;
		List<T> list = new ArrayList<>(size);
		feedItems(list, items);
		return list;
	}

    @SafeVarargs
	public static <T extends Object> Stack<T> asStack(T...items) {
    	Stack<T> list = new Stack<>();
		feedItems(list, items);
		return list;
	}
	
	public static Map<Object, Object> asMap(Object...items) {
    	int size = items==null ? 0 : items.length;
		Map<Object, Object> map = new HashMap<>(Math.min(10, size/2));
		
		if(items!=null) {
			for(int i = 0, len = items.length-1; i<len; i+=2) {
				map.put(items[i], items[i+1]);
			}
		}
		
		return map;
	}
	
	public static Map<Object, Object> asLinkedMap(Object...items) {
    	int size = items==null ? 0 : items.length;
		Map<Object, Object> map = new LinkedHashMap<>(Math.min(10, size/2));
		
		if(items!=null) {
			for(int i = 0, len = items.length-1; i<len; i+=2) {
				map.put(items[i], items[i+1]);
			}
		}
		
		return map;
	}

	
	public static int min(int...values) {
		/*if(values.length<2)
			throw new IllegalArgumentException();*/
		
		int min = Integer.MAX_VALUE;
		
		for(int val : values)
			if(val<min)
				min = val;
		
		return min;
	}
	
	public static int max(int...values) {
		/*if(values.length<2)
			throw new IllegalArgumentException();*/
		
		int max = Integer.MIN_VALUE;
		
		for(int val : values)
			if(val>max)
				max = val;
		
		return max;
	}
	
	public static void fillAscending(int[] a) {
		for(int i=0; i<a.length; i++)
			a[i] = i;
	}
	
	public static void fillAscending(Integer[] a) {
		for(int i=0; i<a.length; i++)
			a[i] = i;
	}
	
	public static boolean isAscending(int[] a) {
		for(int i=0; i<a.length; i++)
			if(a[i]!=i)
				return false;
		
		return true;
	}
	
	public static void permutate(int[] a, int[] permutation) {
		if(a.length<permutation.length)
			throw new IllegalArgumentException();
		
		int[] tmp = new int[permutation.length];
		for(int i=0; i<permutation.length; i++) {
			if(permutation[i]>=permutation.length)
				throw new IllegalArgumentException();
			
			tmp[i] = a[permutation[i]];
		}
		
		System.arraycopy(tmp, 0, a, 0, permutation.length);
	}
	
	@SuppressWarnings("unchecked")
	public static<T extends Object> void permutate(T[] a, int[] permutation) {
		if(a.length<permutation.length)
			throw new IllegalArgumentException();

        T[] tmp = (T[])Array.newInstance(
        		a.getClass().getComponentType(), permutation.length);
        
		for(int i=0; i<permutation.length; i++) {
			if(permutation[i]>=permutation.length)
				throw new IllegalArgumentException();
			tmp[i] = a[permutation[i]];
		}
		
		System.arraycopy(tmp, 0, a, 0, permutation.length);
	}
	
	public static void reverse(int[] array, int offset, int length) {
		Exceptions.testNullArgument(array, "array"); //$NON-NLS-1$
		
		if(length==-1)
			length = array.length;
		
		length = Math.min(length, array.length-offset);
		
		int tmp, flipIndex;
		int steps = length/2;
		for(int i = 0; i<steps; i++) {
			flipIndex = offset+length-i-1;
			tmp = array[offset+i];
			array[offset+i] = array[flipIndex];
			array[flipIndex] = tmp;
		}
	}
	
	public static int count(boolean[] a, boolean value) {
		int count = 0;
		
		for(boolean val : a)
			if(val==value)
				count++;
		
		return count;
	}
	
	public static void reverse(Object[] array, int offset, int length) {
		Exceptions.testNullArgument(array, "array"); //$NON-NLS-1$
		
		if(length==-1)
			length = array.length;
		
		length = Math.min(length, array.length-offset);
		
		int flipIndex;
		Object tmp;
		int steps = length/2;
		for(int i = 0; i<steps; i++) {
			flipIndex = offset+length-i-1;
			tmp = array[offset+i];
			array[offset+i] = array[flipIndex];
			array[flipIndex] = tmp;
		}
	}
	
	public static boolean isUniform(Iterable<?> list) {
		Object prev = null;
		for(Iterator<?> i = list.iterator(); i.hasNext();) {
			Object item = i.next();
			if(item==null)
				throw new IllegalArgumentException("Null not supported by uniformity check!"); //$NON-NLS-1$
			
			if(prev!=null && !prev.equals(item)) {
				return false;
			}
			
			prev = item;
		}
		return true;
	}
	
	public static boolean isUniform(Object[] list) {
		Object prev = null;
		for(Object item : list) {
			if(item==null)
				throw new IllegalArgumentException("Null not supported by uniformity check!"); //$NON-NLS-1$
			
			if(prev!=null && !prev.equals(item)) {
				return false;
			}
			
			prev = item;
		}
		return true;
	}

	public static String toString(Collection<?> collection) {
		return toString(collection, '_');
	}
	
	public static String toString(Collection<?> collection, char delimiter) {
		StringBuilder sb = new StringBuilder();
		
		for(Iterator<?> i = collection.iterator(); i.hasNext(); ) {
			sb.append(i.next());
			if(i.hasNext()) {
				sb.append(delimiter);
			}
		}
		
		return sb.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String toString(Map map) {
		StringBuilder sb = new StringBuilder();
		
		sb.append('[');
		for(Iterator<Entry> i = map.entrySet().iterator(); i.hasNext();) {
			Entry entry = i.next();
			sb.append(entry.getKey()).append('=').append(entry.getValue());
			if(i.hasNext()) {
				sb.append(", "); //$NON-NLS-1$
			}
		}
		sb.append(']');
		
		return sb.toString();
	}
	
	public static int hashCode(Iterable<? extends Object> source) {
		int hc = 1;
		for(Iterator<?> i = source.iterator(); i.hasNext();) {
			hc *= (i.next().hashCode()+1);
		}
		return hc;
	}
	
	public static int hashCode(int[] source) {
		int hc = 1;
		for(int i : source) {
			hc *= (i+1);
		}
		return hc;
	}
	
	public static <E extends Object> boolean equals(Collection<E> c1, Collection<E> c2) {
		if(c1==null || c2==null) {
			return c1==c2;
		}
		
		if(c1.size()!=c2.size()) {
			return false;
		}
		
		Iterator<E> i1 = c1.iterator();
		Iterator<E> i2 = c2.iterator();
		
		while(i1.hasNext() && i2.hasNext()) {
			if(!i1.next().equals(i2.next())) {
				return false;
			}
		}
		
		return true;
	}
	
	public static <K extends Object, V extends Object> boolean equals(Map<K, V> m1, Map<K, V> m2) {
		if(m1==null || m2==null) {
			return m1==m2;
		}
		
		if(m1.size()!=m2.size()) {
			return false;
		}
		
		for(K key : m1.keySet()) {
			if(!m1.get(key).equals(m2.get(key))) {
				return false;
			}
		}
		
		return true;
	}
}
