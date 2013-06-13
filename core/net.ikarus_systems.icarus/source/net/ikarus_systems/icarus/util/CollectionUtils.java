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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CollectionUtils {

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

	
	public static int min(int...values) {
		if(values.length<2)
			throw new IllegalArgumentException();
		
		int min = Integer.MAX_VALUE;
		
		for(int val : values)
			if(val<min)
				min = val;
		
		return min;
	}
	
	public static int max(int...values) {
		if(values.length<2)
			throw new IllegalArgumentException();
		
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
}
