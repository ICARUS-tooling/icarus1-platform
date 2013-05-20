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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CollectionUtils {

	/**
	 * 
	 */
	private CollectionUtils() {
		// no-op
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

    @SafeVarargs
	private static <T extends Object> void feedItems(
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

}
