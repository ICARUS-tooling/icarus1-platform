/**
 * 
 */
package net.ikarus_systems.icarus.util;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Markus Gärtner
 * 
 */
public class Options extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6318648432239062316L;
	
	public static final Options emptyOptions = new Options() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6172790615021617955L;

		@Override
		public Object put(String key, Object value) {
			return null;
		}
	};

	public Options() {
	}

	public Options(Options source) {
		putAll(source);
	}

	public Options(Object... args) {
		putAll(args);
	}
	
	@SuppressWarnings("unchecked")
	public <O extends Object> O get(String key, O defaultValue) {
		Object value = get(key);
		return value==null ? defaultValue : (O) value;
	}
	
	@Override
	public Object put(String key, Object value) {
		if(value==null) {
			return remove(key);
		} else {
			return super.put(key, value);
		}
	}

	public void putAll(Object... args) {
		if (args == null || args.length % 2 != 0)
			return;

		for (int i = 0; i < args.length; i += 2) {
			put(String.valueOf(args[i]), args[i + 1]);
		}
	}
	
	public void putIfAbsent(String key, Object value) {
		if(!containsKey(key)) {
			put(key, value);
		}
	}
	
	public void dump() {
		System.out.println("Options: "); //$NON-NLS-1$
		for(Entry<String, Object> entry : entrySet())
			System.out.printf("  -key=%s value=%s\n",  //$NON-NLS-1$
					entry.getKey(), String.valueOf(entry.getValue()));
	}
	
	// Collection of commonly used option keys
	
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String LABEL = "label"; //$NON-NLS-1$
	public static final String TITLE = "title"; //$NON-NLS-1$
	public static final String CONTENT_TYPE = "contentType"; //$NON-NLS-1$
	public static final String CONVERTER = "converter"; //$NON-NLS-1$
	public static final String LOCATION = "location"; //$NON-NLS-1$
	public static final String LANGUAGE = "language"; //$NON-NLS-1$
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String FILTER = "filter"; //$NON-NLS-1$
	public static final String EXTENSION = "extension"; //$NON-NLS-1$
	public static final String PLUGIN = "plugin"; //$NON-NLS-1$
	public static final String DATA = "data"; //$NON-NLS-1$
	public static final String OWNER = "owner"; //$NON-NLS-1$
	public static final String INDEX = "index"; //$NON-NLS-1$
}
