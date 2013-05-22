/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.util.ClassProxy;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.id.DuplicateIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SearchUtils {

	/**
	 * Head value to mark the root node.
	 */
	public static final int DATA_HEAD_ROOT = -1;

	public static final String DATA_ROOT_LABEL = "<root>"; //$NON-NLS-1$

	public static final String DATA_UNDEFINED_LABEL = "?"; //$NON-NLS-1$

	public static final String DATA_GROUP_LABEL = "<*>"; //$NON-NLS-1$

	public static final String DATA_LEFT_LABEL = "<<"; //$NON-NLS-1$

	public static final String DATA_RIGHT_LABEL = ">>"; //$NON-NLS-1$

	public static final int DATA_LEFT_VALUE = -1;

	public static final int DATA_RIGHT_VALUE = 1;

	public static final int DATA_GROUP_VALUE = -3;

	public static final int DATA_UNDEFINED_VALUE = -2;

	public static final int DATA_YES_VALUE = 0;

	public static final int DATA_NO_VALUE = -1;
	
	// Maps constraint id to either className, class or instance
	private static Map<String, Object> constraintFactories;
	// Maps content type to collection of factories (by id) 
	// that are declared for that type
	private static Map<ContentType, Collection<String>> contentRestrictions;
	
	// Maps strings to their compiled Pattern instance.
	// We use a weak hash map here since we only need the Pattern
	// as long as the respective string is used in some constraint
	private static Map<String, Pattern> patterns = Collections.synchronizedMap(
			new WeakHashMap<String, Pattern>());

	private SearchUtils() {
		// no-op
	}

	public static Pattern getPattern(String s) {
		if(s==null || s.isEmpty()) {
			return null;
		}
		
		Pattern pattern = patterns.get(s);
		if(pattern==null) {
			// Do not catch PatternSyntaxException!
			// We want whatever operation the pattern request was originating 
			// from to be terminated by the exception.
			pattern = Pattern.compile(s);
			
			// Do not bother with 'duplicates' since all Pattern
			// compiled from the same string are in fact identical in
			// terms of functionality
			patterns.put(s, pattern);
		}
		
		return pattern;
	}
	
	public static void registerConstraintFactory(ConstraintFactory factory, ContentType contentType) {
		registerConstraintFactory(new String[]{factory.getId()},  factory, contentType);
	}
	
	public static void registerConstraintFactory(String[] ids, Object factory, ContentType contentType) {
		if(ids==null || ids.length==0)
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		if(factory==null)
			throw new IllegalArgumentException("Invalid factory"); //$NON-NLS-1$
		if(contentType==null)
			throw new IllegalArgumentException("Invalid contentType"); //$NON-NLS-1$
		
		if(constraintFactories==null) {
			constraintFactories = new HashMap<>();
		}
		if(contentRestrictions==null) {
			contentRestrictions = new HashMap<>();
		}
		
		for(String id : ids) {
			if(constraintFactories.containsKey(id))
				throw new DuplicateIdentifierException("Duplicate factory for id: "+id); //$NON-NLS-1$
			
			if(factory instanceof ConstraintFactory
					|| factory instanceof String
					|| factory instanceof Class
					|| factory instanceof ClassProxy) {
				constraintFactories.put(id, factory);
			} else
				throw new IllegalArgumentException("Invalid factory: "+factory.getClass()); //$NON-NLS-1$
		}
		
		String id = ids[0];
		Collection<String> items = contentRestrictions.get(contentType);
		if(items==null) {
			items = new ArrayList<>();
			contentRestrictions.put(contentType, items);
		}
		items.add(id);
	}
	
	public static ConstraintFactory getConstraintFactory(String id) {
		if(id==null)
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		
		Object factory = constraintFactories==null ? null : constraintFactories.get(id);
		if(factory!=null && !(factory instanceof ConstraintFactory)) {
			try {
				if(factory instanceof String) {
					factory = Class.forName((String)factory);
				}
				if(factory instanceof Class) {
					factory = ((Class<?>)factory).newInstance();
				} else if(factory instanceof ClassProxy) {
					factory = ((ClassProxy)factory).loadObjectUnsafe();
				}
				
				// Refresh mapping
				constraintFactories.put(id, factory);
			} catch(Exception e) {
				LoggerFactory.log(SearchUtils.class, Level.SEVERE, 
						"Failed to instantiate constraint factory for id: "+id, e); //$NON-NLS-1$
				
				constraintFactories.remove(id);				
				factory = null;
			}
		}
		
		return (ConstraintFactory) factory;
	}
	
	public static ConstraintFactory[] getConstraintFactories(ContentType contentType) {
		if(contentType==null)
			throw new IllegalArgumentException("Invalid contentType"); //$NON-NLS-1$
		
		if(contentRestrictions==null) {
			return null;
		}
		
		// Using a set should be sufficient since every factory gets 
		// registered by exactly one id in the content mapping, so even
		// when we fetch it for multiple content types it is still the same
		// instance within the constraintFactories map.
		Collection<ConstraintFactory> items = new HashSet<>();
		
		for(Entry<ContentType, Collection<String>> entry : contentRestrictions.entrySet()) {
			if(ContentTypeRegistry.isCompatible(contentType, entry.getKey())) {
				for(String id : entry.getValue()) {
					items.add(getConstraintFactory(id));
				}
			}
		}
		
		ConstraintFactory[] tmp = new ConstraintFactory[items.size()];
		return items.toArray(tmp);
	}
	
	public static ConstraintFactory[] getNodeConstraintFactories(ConstraintFactory[] items) {
		if(items==null)
			throw new IllegalArgumentException("Invalid items"); //$NON-NLS-1$
		
		List<ConstraintFactory> result = new ArrayList<>();
		
		for(ConstraintFactory factory : items) {
			if(factory.getConstraintType()==ConstraintFactory.NODE_CONSTRAINT_TYPE) {
				result.add(factory);
			}
		}
		
		return result.toArray(new ConstraintFactory[0]);
	}
	
	public static ConstraintFactory[] getEdgeConstraintFactories(ConstraintFactory[] items) {
		if(items==null)
			throw new IllegalArgumentException("Invalid items"); //$NON-NLS-1$
		
		List<ConstraintFactory> result = new ArrayList<>();
		
		for(ConstraintFactory factory : items) {
			if(factory.getConstraintType()==ConstraintFactory.EDGE_CONSTRAINT_TYPE) {
				result.add(factory);
			}
		}
		
		return result.toArray(new ConstraintFactory[0]);
	}
	
	public static boolean isRoot(int value) {
		return value==DATA_HEAD_ROOT;
	}
	
	public static boolean isRoot(String value) {
		return DATA_ROOT_LABEL.equals(value);
	}
	
	public static boolean isUndefined(int value) {
		return value==DATA_UNDEFINED_VALUE;
	}
	
	public static boolean isUndefined(String value) {
		return value==null || value.isEmpty() || value.equals(DATA_UNDEFINED_LABEL);
	}
	
	public static String getBooleanLabel(int value) {
		switch (value) {
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_YES_VALUE:
			return String.valueOf(true);
		case DATA_NO_VALUE:
			return String.valueOf(false);
		}
		
		throw new IllegalArgumentException("Unknown value: "+value); //$NON-NLS-1$
	}
	
	public static int parseBooleanLabel(String label) {
		if(DATA_GROUP_LABEL.equals(label))
			return DATA_GROUP_VALUE;
		else if(DATA_UNDEFINED_LABEL.equals(label))
			return DATA_UNDEFINED_VALUE;
		else if(Boolean.parseBoolean(label))
			return DATA_YES_VALUE;
		else
			return DATA_NO_VALUE;
	}

	public static String getHeadLabel(int head) {
		switch (head) {
		case DATA_HEAD_ROOT:
			return DATA_ROOT_LABEL;
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		default:
			return String.valueOf(head + 1);
		}
	}

	public static String getLabel(int value) {
		switch (value) {
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		default:
			return String.valueOf(value);
		}
	}

	public static String getDirectionLabel(int value) {
		switch (value) {
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		case DATA_LEFT_VALUE:
			return DATA_LEFT_LABEL;
		case DATA_RIGHT_VALUE:
			return DATA_RIGHT_LABEL;
		}

		return null;
	}

	public static int parseHeadLabel(String head) {
		head = head.trim();
		if (DATA_ROOT_LABEL.equals(head))
			return DATA_HEAD_ROOT;
		else if (DATA_UNDEFINED_LABEL.equals(head))
			return DATA_UNDEFINED_VALUE;
		else if (DATA_GROUP_LABEL.equals(head))
			return DATA_GROUP_VALUE;
		else
			return Integer.parseInt(head) - 1;
	}

	public static int parseLabel(String value) {
		value = value.trim();
		if (value.isEmpty() || DATA_UNDEFINED_LABEL.equals(value))
			return DATA_UNDEFINED_VALUE;
		else if (DATA_GROUP_LABEL.equals(value))
			return DATA_GROUP_VALUE;
		else
			return Math.abs(Integer.parseInt(value));
	}

	public static int parseDirectionLabel(String direction) {
		direction = direction.trim();
		if (DATA_GROUP_LABEL.equals(direction))
			return DATA_GROUP_VALUE;
		else if (DATA_LEFT_LABEL.equals(direction))
			return DATA_LEFT_VALUE;
		else if (DATA_RIGHT_LABEL.equals(direction))
			return DATA_RIGHT_VALUE;
		else
			return DATA_UNDEFINED_VALUE;
	}

	public static String normalizeLabel(String value) {
		if(value==null)
			return DATA_UNDEFINED_LABEL;
		
		value = value.trim();
		if (value.isEmpty())
			return DATA_UNDEFINED_LABEL;
		else
			return value;
	}
}
