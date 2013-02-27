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

    @SafeVarargs
	private static <T extends Object> void feedItems(
			Collection<T> collection, T...items) {
		if(items==null) {
			return;
		}
		for(T item : items) {
			collection.add((T)item);
		}
	}

    @SafeVarargs
	public static <T extends Object> Set<T> asSet(T...items) {
		Set<T> set = new HashSet<>(items.length);
		feedItems(set, items);
		return set;
	}

    @SafeVarargs
	public static <T extends Object> List<T> asList(T...items) {
		List<T> list = new ArrayList<>(items.length);
		feedItems(list, items);
		return list;
	}
	
	public static Map<Object, Object> asMap(Object...items) {
		Map<Object, Object> map = new HashMap<>();
		
		if(items!=null) {
			for(int i = 0, len = items.length-1; i<len; i+=2) {
				map.put(items[i], items[i+1]);
			}
		}
		
		return map;
	}

}
