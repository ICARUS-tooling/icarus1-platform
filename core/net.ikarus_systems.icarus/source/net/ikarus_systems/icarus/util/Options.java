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
}
