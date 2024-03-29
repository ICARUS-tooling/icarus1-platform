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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.WeakHashMap;

import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.Filter;
import gnu.trove.TLongCollection;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CollectionUtils {

	public static final int DEFAULT_COLLECTION_CAPACITY = 10;
	public static final float DEFAULT_LOAD_FACTOR = 0.75f;
	public static final float DEFAULT_MIN_LOAD = 0.25f;

	private static final Map<Class<?>, Map<Object, Object>> proxyMaps = new HashMap<>();
	static {
		proxyMaps.put(Set.class, new WeakHashMap<>());
		proxyMaps.put(Collection.class, new WeakHashMap<>());
		proxyMaps.put(List.class, new WeakHashMap<>());
		proxyMaps.put(Map.class, new WeakHashMap<>());
	}
	private static final Object proxyLock = new Object();

	private CollectionUtils() {
		// no-op
	}

	public static <E extends Object> Set<E> getSetProxy(Set<E> set) {
		if(set==null)
			throw new NullPointerException("Invalid set"); //$NON-NLS-1$

		synchronized (proxyLock) {
			Map<Object, Object> proxies = proxyMaps.get(Set.class);

			@SuppressWarnings("unchecked")
			Set<E> proxy = (Set<E>) proxies.get(set);
			if(proxy==null) {
				proxy = unmodifiableSetProxy(set);
				proxies.put(set, proxy);
			}

			return proxy;
		}
	}

	public static <E extends Object> Collection<E> getCollectionProxy(Collection<E> collection) {
		if(collection==null)
			throw new NullPointerException("Invalid collection"); //$NON-NLS-1$

		synchronized (proxyLock) {
			Map<Object, Object> proxies = proxyMaps.get(Collection.class);

			@SuppressWarnings("unchecked")
			Collection<E> proxy = (Collection<E>) proxies.get(collection);
			if(proxy==null) {
				proxy = unmodifiableCollectionProxy(collection);
				proxies.put(collection, proxy);
			}

			return proxy;
		}
	}

	public static <E extends Object> List<E> getListProxy(List<E> list) {
		if(list==null)
			throw new NullPointerException("Invalid list"); //$NON-NLS-1$

		synchronized (proxyLock) {
			Map<Object, Object> proxies = proxyMaps.get(List.class);

			@SuppressWarnings("unchecked")
			List<E> proxy = (List<E>) proxies.get(list);
			if(proxy==null) {
				proxy = unmodifiableListProxy(list);
				proxies.put(list, proxy);
			}

			return proxy;
		}
	}

	public static <K extends Object, V extends Object> Map<K, V> getMapProxy(Map<K, V> map) {
		if(map==null)
			throw new NullPointerException("Invalid map"); //$NON-NLS-1$

		synchronized (proxyLock) {
			Map<Object, Object> proxies = proxyMaps.get(Map.class);

			@SuppressWarnings("unchecked")
			Map<K, V> proxy = (Map<K, V>) proxies.get(map);
			if(proxy==null) {
				proxy = unmodifiableMapProxy(map);
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
			if(target==null) {
				for(Object item : iterable) {
					if(item==null) {
						return true;
					}
				}
			} else {
				for(Object item : iterable) {
					if(target.equals(item)) {
						return true;
					}
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
			if(target==null) {
				for(int i=0; i<array.length; i++) {
					if(array[i]==null) {
						return i;
					}
				}
			} else {
				for(int i=0; i<array.length; i++) {
					if(target.equals(array[i])) {
						return i;
					}
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

    public static void feedItems(TLongCollection collection, long...items) {
    	for(long item : items) {
    		collection.add(item);
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

	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> items) {
		if (items == null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		List<T> result = Collections.emptyList();

		if(!items.isEmpty()) {
			result = new ArrayList<>(items);

			Collections.sort(result);
		}

		return result;
	}

	public static <T extends Object> List<T> asSortedList(Collection<T> items, Comparator<? super T> comparator) {
		if (items == null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		List<T> result = Collections.emptyList();

		if(!items.isEmpty()) {
			result = new ArrayList<>(items);

			Collections.sort(result, comparator);
		}

		return result;
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

	@SafeVarargs
	public static <K extends Object, V extends Object> V firstSet(Map<K, V> map, K...keys) {
		for(K key : keys) {
			V val = map.get(key);
			if(val!=null) {
				return val;
			}
		}

		return null;
	}

	public static String firstSetString(Map<String, String> map, String defaultValue, String...keys) {
		String val = firstSet(map, keys);
		return val==null ? defaultValue : val;
	}

	public static int firstSetInt(Map<String, String> map, int defaultValue, String...keys) {
		String val = firstSet(map, keys);
		return val==null ? defaultValue : Integer.parseInt(val);
	}

	public static long firstSetLong(Map<String, String> map, long defaultValue, String...keys) {
		String val = firstSet(map, keys);
		return val==null ? defaultValue : Long.parseLong(val);
	}

	public static float firstSetFloat(Map<String, String> map, float defaultValue, String...keys) {
		String val = firstSet(map, keys);
		return val==null ? defaultValue : Float.parseFloat(val);
	}

	public static double firstSetDouble(Map<String, String> map, double defaultValue, String...keys) {
		String val = firstSet(map, keys);
		return val==null ? defaultValue : Double.parseDouble(val);
	}

	public static boolean firstSetBoolean(Map<String, String> map, boolean defaultValue, String...keys) {
		String val = firstSet(map, keys);
		return val==null ? defaultValue : Boolean.parseBoolean(val);
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

    /**
     * Returns true if the specified arguments are equal, or both null.
     */
    static boolean eq(Object o1, Object o2) {
        return o1==null ? o2==null : o1.equals(o2);
    }

    static class EmptyIterator<E extends Object> implements Iterator<E> {

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return false;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public E next() {
			throw new NoSuchElementException();
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new IllegalStateException();
		}
    }

    static class EmptyListIterator<E extends Object> implements ListIterator<E> {

		/**
		 * @see java.util.ListIterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return false;
		}

		/**
		 * @see java.util.ListIterator#next()
		 */
		@Override
		public E next() {
			throw new NoSuchElementException();
		}

		/**
		 * @see java.util.ListIterator#hasPrevious()
		 */
		@Override
		public boolean hasPrevious() {
			return false;
		}

		/**
		 * @see java.util.ListIterator#previous()
		 */
		@Override
		public E previous() {
			throw new NoSuchElementException();
		}

		/**
		 * @see java.util.ListIterator#nextIndex()
		 */
		@Override
		public int nextIndex() {
			return 0;
		}

		/**
		 * @see java.util.ListIterator#previousIndex()
		 */
		@Override
		public int previousIndex() {
			return -1;
		}

		/**
		 * @see java.util.ListIterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}

    }

    public static <T> Collection<T> unmodifiableCollectionProxy(Collection<? extends T> c) {
        return new UnmodifiableCollection<>(c);
    }

    /**
     * @serial include
     */
    static class UnmodifiableCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 1820017752578914078L;

        transient Reference<?> ref;

        UnmodifiableCollection(Collection<? extends E> c) {
            if (c==null)
                throw new NullPointerException();
            this.ref = new WeakReference<Collection<?>>(c);
        }

        @SuppressWarnings("unchecked")
		public Collection<? extends E> ref() {
        	return (Collection<? extends E>) ref.get();
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        	Collection<?> c = (Collection<?>) in.readObject();
        	ref = new WeakReference<>(c);
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
        	Collection<? extends E> c = ref();
        	out.writeObject(c);
        }

        @Override
		public int size() {
        	Collection<? extends E> c = ref();
        	return c==null ? 0 : c.size();
        }
        @Override
		public boolean isEmpty() {
        	Collection<? extends E> c = ref();
        	return c==null ? true : c.isEmpty();
        }
        @Override
		public boolean contains(Object o) {
        	Collection<? extends E> c = ref();
        	return c==null ? false : c.contains(o);
        }
        @Override
		public Object[] toArray(){
        	Collection<? extends E> c = ref();
        	return c==null ? new Object[0] : c.toArray();
        }
        @Override
		public <T> T[] toArray(T[] a) {
        	Collection<? extends E> c = ref();
        	return c==null ? a : c.toArray(a);
        }
        @Override
		public String toString() {
        	Collection<? extends E> c = ref();
        	return c==null ? super.toString() : c.toString();
        }

        @Override
		public Iterator<E> iterator() {
        	final Collection<? extends E> c = ref();
        	if(c==null) {
        		return new EmptyIterator<>();
        	}

            return new Iterator<E>() {
                private final Iterator<? extends E> i = c.iterator();

                @Override
				public boolean hasNext() {return i.hasNext();}
                @Override
				public E next()          {return i.next();}
                @Override
				public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
		public boolean add(E e) {
            throw new UnsupportedOperationException();
        }
        @Override
		public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
		public boolean containsAll(Collection<?> coll) {
        	Collection<? extends E> c = ref();
            return c==null ? coll.isEmpty() : c.containsAll(coll);
        }
        @Override
		public boolean addAll(Collection<? extends E> coll) {
            throw new UnsupportedOperationException();
        }
        @Override
		public boolean removeAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }
        @Override
		public boolean retainAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }
        @Override
		public void clear() {
            throw new UnsupportedOperationException();
        }
    }
    public static <T> Set<T> unmodifiableSetProxy(Set<? extends T> s) {
        return new UnmodifiableSet<>(s);
    }

    /**
     * @serial include
     */
    static class UnmodifiableSet<E> extends UnmodifiableCollection<E>
                                 implements Set<E>, Serializable {
        private static final long serialVersionUID = -9215047833775013803L;

        UnmodifiableSet(Set<? extends E> s)     {super(s);}

        @SuppressWarnings("unchecked")
		@Override
		public Set<? extends E> ref() {
        	return (Set<? extends E>) ref.get();
        }
        @Override
		public boolean equals(Object o) {
        	Set<? extends E> s = ref();
        	return o == this || (s!=null && s.equals(o));
        }
        @Override
		public int hashCode() {
        	Set<? extends E> s = ref();
        	return s==null ? 0 : s.hashCode();
        }
    }

    /**
     * Returns an unmodifiable view of the specified sorted set.  This method
     * allows modules to provide users with "read-only" access to internal
     * sorted sets.  Query operations on the returned sorted set "read
     * through" to the specified sorted set.  Attempts to modify the returned
     * sorted set, whether direct, via its iterator, or via its
     * <tt>subSet</tt>, <tt>headSet</tt>, or <tt>tailSet</tt> views, result in
     * an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned sorted set will be serializable if the specified sorted set
     * is serializable.
     *
     * @param s the sorted set for which an unmodifiable view is to be
     *        returned.
     * @return an unmodifiable view of the specified sorted set.
     */
    public static <T> SortedSet<T> unmodifiableSortedSetProxy(SortedSet<T> s) {
        return new UnmodifiableSortedSet<>(s);
    }

    /**
     * @serial include
     */
    static class UnmodifiableSortedSet<E>
                             extends UnmodifiableSet<E>
                             implements SortedSet<E>, Serializable {
        private static final long serialVersionUID = -4929149591599911165L;

        UnmodifiableSortedSet(SortedSet<E> s) {super(s);}

        @SuppressWarnings("unchecked")
		@Override
		public SortedSet<E> ref() {
        	return (SortedSet<E>) ref.get();
        }

        @Override
		public Comparator<? super E> comparator() {
        	SortedSet<E> ss = ref();
        	return ss==null ? null : ss.comparator();
        }

        @Override
		public SortedSet<E> subSet(E fromElement, E toElement) {
        	SortedSet<E> ss = ref();
            return ss==null ? null : new UnmodifiableSortedSet<>(ss.subSet(fromElement,toElement));
        }
        @Override
		public SortedSet<E> headSet(E toElement) {
        	SortedSet<E> ss = ref();
            return ss==null ? null : new UnmodifiableSortedSet<>(ss.headSet(toElement));
        }
        @Override
		public SortedSet<E> tailSet(E fromElement) {
        	SortedSet<E> ss = ref();
            return ss==null ? null : new UnmodifiableSortedSet<>(ss.tailSet(fromElement));
        }

        @Override
		public E first() {
        	SortedSet<E> ss = ref();
        	return ss==null ? null : ss.first();
        }
        @Override
		public E last() {
        	SortedSet<E> ss = ref();
        	return ss==null ? null : ss.last();
        }
    }

    public static <T> List<T> unmodifiableListProxy(List<? extends T> list) {
        return (list instanceof RandomAccess ?
                new UnmodifiableRandomAccessList<>(list) :
                new UnmodifiableList<>(list));
    }

    /**
     * @serial include
     */
    static class UnmodifiableList<E> extends UnmodifiableCollection<E>
                                  implements List<E> {
        private static final long serialVersionUID = -283967356065247728L;

        UnmodifiableList(List<? extends E> list) {
            super(list);
        }

        @SuppressWarnings("unchecked")
		@Override
		public List<? extends E> ref() {
        	return (List<? extends E>) ref.get();
        }

        @Override
		public boolean equals(Object o) {
        	List<? extends E> list = ref();
        	return o == this || (list!=null && list.equals(o));
        }
        @Override
		public int hashCode() {
        	List<? extends E> list = ref();
        	return list==null ? 0 : list.hashCode();
        }

        @Override
		public E get(int index) {
        	List<? extends E> list = ref();
        	return list==null ? null : list.get(index);
        }
        @Override
		public E set(int index, E element) {
            throw new UnsupportedOperationException();
        }
        @Override
		public void add(int index, E element) {
            throw new UnsupportedOperationException();
        }
        @Override
		public E remove(int index) {
            throw new UnsupportedOperationException();
        }
        @Override
		public int indexOf(Object o) {
        	List<? extends E> list = ref();
        	return list==null ? -1 : list.indexOf(o);
        }
        @Override
		public int lastIndexOf(Object o) {
        	List<? extends E> list = ref();
        	return list==null ? -1 : list.lastIndexOf(o);
        }
        @Override
		public boolean addAll(int index, Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }
        @Override
		public ListIterator<E> listIterator()   {return listIterator(0);}

        @Override
		public ListIterator<E> listIterator(final int index) {
        	final List<? extends E> list = ref();
        	if(list==null) {
        		return new EmptyListIterator<>();
        	}

            return new ListIterator<E>() {
                private final ListIterator<? extends E> i
                    = list.listIterator(index);

                @Override
				public boolean hasNext()     {return i.hasNext();}
                @Override
				public E next()              {return i.next();}
                @Override
				public boolean hasPrevious() {return i.hasPrevious();}
                @Override
				public E previous()          {return i.previous();}
                @Override
				public int nextIndex()       {return i.nextIndex();}
                @Override
				public int previousIndex()   {return i.previousIndex();}

                @Override
				public void remove() {
                    throw new UnsupportedOperationException();
                }
                @Override
				public void set(E e) {
                    throw new UnsupportedOperationException();
                }
                @Override
				public void add(E e) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
		public List<E> subList(int fromIndex, int toIndex) {
        	List<? extends E> list = ref();
            return list==null ? null : new UnmodifiableList<>(list.subList(fromIndex, toIndex));
        }

        /**
         * UnmodifiableRandomAccessList instances are serialized as
         * UnmodifiableList instances to allow them to be deserialized
         * in pre-1.4 JREs (which do not have UnmodifiableRandomAccessList).
         * This method inverts the transformation.  As a beneficial
         * side-effect, it also grafts the RandomAccess marker onto
         * UnmodifiableList instances that were serialized in pre-1.4 JREs.
         *
         * Note: Unfortunately, UnmodifiableRandomAccessList instances
         * serialized in 1.4.1 and deserialized in 1.4 will become
         * UnmodifiableList instances, as this method was missing in 1.4.
         */
        private Object readResolve() {
        	List<? extends E> list = ref();
            return (list instanceof RandomAccess
                    ? new UnmodifiableRandomAccessList<>(list)
                    : this);
        }
    }

    /**
     * @serial include
     */
    static class UnmodifiableRandomAccessList<E> extends UnmodifiableList<E>
                                              implements RandomAccess
    {
        UnmodifiableRandomAccessList(List<? extends E> list) {
            super(list);
        }

        @Override
		public List<E> subList(int fromIndex, int toIndex) {
        	List<? extends E> list = ref();
            return list==null ? null : new UnmodifiableRandomAccessList<>(
                list.subList(fromIndex, toIndex));
        }

        private static final long serialVersionUID = -2542308836966382001L;

        /**
         * Allows instances to be deserialized in pre-1.4 JREs (which do
         * not have UnmodifiableRandomAccessList).  UnmodifiableList has
         * a readResolve method that inverts this transformation upon
         * deserialization.
         */
        private Object writeReplace() {
        	List<? extends E> list = ref();
            return list==null ? this : new UnmodifiableList<>(list);
        }
    }

    /**
     * Returns an unmodifiable view of the specified map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable.
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified map.
     */
    public static <K,V> Map<K,V> unmodifiableMapProxy(Map<? extends K, ? extends V> m) {
        return new UnmodifiableMap<>(m);
    }

    /**
     * @serial include
     */
    private static class UnmodifiableMap<K,V> implements Map<K,V>, Serializable {
        private static final long serialVersionUID = -1034234728574286014L;

        private transient Reference<?> ref;

        UnmodifiableMap(Map<? extends K, ? extends V> m) {
            if (m==null)
                throw new NullPointerException();
            this.ref = new WeakReference<Map<? extends K,? extends V>>(m);
        }

        @SuppressWarnings("unused")
		private void readObjectNoData() throws ObjectStreamException {
        	ref = new WeakReference<>(null);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        	@SuppressWarnings("unchecked")
        	Map<? extends K, ? extends V> m = (Map<? extends K, ? extends V>) in.readObject();
        	ref = new WeakReference<>(m);
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
        	Map<? extends K, ? extends V> m = ref();
        	out.writeObject(m);
        }

        @SuppressWarnings("unchecked")
		public Map<? extends K, ? extends V> ref() {
        	return (Map<? extends K, ? extends V>) ref.get();
        }

        @Override
		public int size(){
        	Map<? extends K, ? extends V> m = ref();
        	return ref==null ? 0 : m.size();
        }
        @Override
		public boolean isEmpty() {
        	Map<? extends K, ? extends V> m = ref();
        	return m==null ? true : m.isEmpty();
        }
        @Override
		public boolean containsKey(Object key) {
        	Map<? extends K, ? extends V> m = ref();
        	return m==null ? false : m.containsKey(key);
        }
        @Override
		public boolean containsValue(Object val) {
        	Map<? extends K, ? extends V> m = ref();
        	return m==null ? false : m.containsValue(val);
        }
        @Override
		public V get(Object key) {
        	Map<? extends K, ? extends V> m = ref();
        	return m==null ? null : m.get(key);
        }

        @Override
		public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }
        @Override
		public V remove(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
		public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }
        @Override
		public void clear() {
            throw new UnsupportedOperationException();
        }

        private transient Set<K> keySet = null;
        private transient Set<Map.Entry<K,V>> entrySet = null;
        private transient Collection<V> values = null;

        @Override
		public Set<K> keySet() {
        	Map<? extends K, ? extends V> m = ref();
        	if(m==null) {
        		return Collections.emptySet();
        	}
            if (keySet==null)
                keySet = unmodifiableSetProxy(m.keySet());
            return keySet;
        }

        @Override
		public Set<Map.Entry<K,V>> entrySet() {
        	Map<? extends K, ? extends V> m = ref();
        	if(m==null) {
        		return Collections.emptySet();
        	}
            if (entrySet==null)
                entrySet = new UnmodifiableEntrySet<>(m.entrySet());
            return entrySet;
        }

        @Override
		public Collection<V> values() {
        	Map<? extends K, ? extends V> m = ref();
        	if(m==null) {
        		return Collections.emptyList();
        	}
            if (values==null)
                values = unmodifiableCollectionProxy(m.values());
            return values;
        }

        @Override
		public boolean equals(Object o) {
        	Map<? extends K, ? extends V> m = ref();
        	return o == this || (m!=null && m.equals(o));
        }
        @Override
		public int hashCode() {
        	Map<? extends K, ? extends V> m = ref();
        	return m==null ? 0 : m.hashCode();
        }
        @Override
		public String toString() {
        	Map<? extends K, ? extends V> m = ref();
        	return m==null ? super.toString() : m.toString();
        }

        /**
         * We need this class in addition to UnmodifiableSet as
         * Map.Entries themselves permit modification of the backing Map
         * via their setValue operation.  This class is subtle: there are
         * many possible attacks that must be thwarted.
         *
         * @serial include
         */
        static class UnmodifiableEntrySet<K,V>
            extends UnmodifiableSet<Map.Entry<K,V>> {
            private static final long serialVersionUID = 7854390611657943733L;

            @SuppressWarnings({ "rawtypes", "unchecked" })
			UnmodifiableEntrySet(Set<? extends Map.Entry<? extends K, ? extends V>> s) {
                super((Set)s);
            }
            @Override
			public Iterator<Map.Entry<K,V>> iterator() {
            	final Set<? extends Map.Entry<K,V>> s = ref();
            	if(s==null) {
            		return new EmptyIterator<>();
            	}

                return new Iterator<Map.Entry<K,V>>() {
                    private final Iterator<? extends Map.Entry<? extends K, ? extends V>> i = s.iterator();

                    @Override
					public boolean hasNext() {
                        return i.hasNext();
                    }
                    @Override
					public Map.Entry<K,V> next() {
                        return new UnmodifiableEntry<>(i.next());
                    }
                    @Override
					public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @SuppressWarnings("unchecked")
			@Override
			public Object[] toArray() {
            	Set<? extends Map.Entry<K,V>> s = ref();
            	if(s==null) {
            		return new Object[0];
            	}
                Object[] a = s.toArray();
                for (int i=0; i<a.length; i++)
                    a[i] = new UnmodifiableEntry<>((Map.Entry<K,V>)a[i]);
                return a;
            }

            @SuppressWarnings("unchecked")
			@Override
			public <T> T[] toArray(T[] a) {
            	Set<? extends Map.Entry<K,V>> s = ref();
            	if(s==null) {
            		return a;
            	}
                // We don't pass a to c.toArray, to avoid window of
                // vulnerability wherein an unscrupulous multithreaded client
                // could get his hands on raw (unwrapped) Entries from c.
                Object[] arr = s.toArray(a.length==0 ? a : Arrays.copyOf(a, 0));

                for (int i=0; i<arr.length; i++)
                    arr[i] = new UnmodifiableEntry<>((Map.Entry<K,V>)arr[i]);

                if (arr.length > a.length)
                    return (T[])arr;

                System.arraycopy(arr, 0, a, 0, arr.length);
                if (a.length > arr.length)
                    a[arr.length] = null;
                return a;
            }

            /**
             * This method is overridden to protect the backing set against
             * an object with a nefarious equals function that senses
             * that the equality-candidate is Map.Entry and calls its
             * setValue method.
             */
            @Override
			public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
            	Set<? extends Map.Entry<K,V>> s = ref();

                return s.contains(
                    new UnmodifiableEntry<>((Map.Entry<?,?>) o));
            }

            /**
             * The next two methods are overridden to protect against
             * an unscrupulous List whose contains(Object o) method senses
             * when o is a Map.Entry, and calls o.setValue.
             */
            @Override
			public boolean containsAll(Collection<?> coll) {
                for (Object e : coll) {
                    if (!contains(e)) // Invokes safe contains() above
                        return false;
                }
                return true;
            }
            @Override
			public boolean equals(Object o) {
                if (o == this)
                    return true;

                if (!(o instanceof Set))
                    return false;

            	Set<? extends Map.Entry<K,V>> s = ref();
            	if(s==null) {
            		return o==null;
            	}
                @SuppressWarnings("rawtypes")
				Set other = (Set) o;
                if (other.size() != s.size())
                    return false;
                return containsAll(other); // Invokes safe containsAll() above
            }

            /**
             * This "wrapper class" serves two purposes: it prevents
             * the client from modifying the backing Map, by short-circuiting
             * the setValue method, and it protects the backing Map against
             * an ill-behaved Map.Entry that attempts to modify another
             * Map Entry when asked to perform an equality check.
             */
            private static class UnmodifiableEntry<K,V> implements Map.Entry<K,V> {
                private Map.Entry<? extends K, ? extends V> e;

                UnmodifiableEntry(Map.Entry<? extends K, ? extends V> e) {this.e = e;}

                @Override
				public K getKey()        {return e.getKey();}
                @Override
				public V getValue()      {return e.getValue();}
                @Override
				public V setValue(V value) {
                    throw new UnsupportedOperationException();
                }
                @Override
				public int hashCode()    {return e.hashCode();}
                @Override
				public boolean equals(Object o) {
                    if (this == o)
                        return true;
                    if (!(o instanceof Map.Entry))
                        return false;
                    @SuppressWarnings("rawtypes")
					Map.Entry t = (Map.Entry)o;
                    return eq(e.getKey(),   t.getKey()) &&
                           eq(e.getValue(), t.getValue());
                }
                @Override
				public String toString() {return e.toString();}
            }
        }
    }
}
