/**
 * 
 */
package de.ims.icarus.xml.jaxb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.ClassProxy;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.DuplicateIdentifierException;
import de.ims.icarus.util.location.Location;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class JAXBUtils {
	
	private static Collection<Class<?>> registeredClasses;
	
	private static JAXBContext sharedJAXBContext;
	
	private static Map<Class<?>, Object> registeredAdapters;
	
	private static Set<Class<?>> illegalClasses = CollectionUtils.asSet(
			Object.class,
			String.class,
			Integer.class,
			Float.class,
			Short.class,
			Long.class,
			Double.class,
			Character.class
	);
	
	static {
		registerAdapter(Extension.class, "de.ims.icarus.xml.jaxb.ExtensionAdapter"); //$NON-NLS-1$
		registerAdapter(Location.class, "de.ims.icarus.xml.jaxb.LocationAdapter"); //$NON-NLS-1$
		registerAdapter(Color.class, "de.ims.icarus.xml.jaxb.ColorAdapter"); //$NON-NLS-1$
		registerAdapter(Map.class, "de.ims.icarus.xml.jaxb.MapAdapter"); //$NON-NLS-1$
	}

	private JAXBUtils(){
		// no-op
	};
	
	public static synchronized void registerClass(Class<?> clazz) {
		if(clazz==null || illegalClasses.contains(clazz))
			throw new NullPointerException("Invalid class"); //$NON-NLS-1$
		
		if(registeredClasses==null) {
			registeredClasses = new HashSet<>();
		}
		
		if(registeredClasses.contains(clazz)) {
			return;
		}
		
		invalidateContext();
		
		registeredClasses.add(clazz);
	}
	
	private static void invalidateContext() {
		sharedJAXBContext = null;
	}
	
	public static synchronized Class<?>[] getRegisteredClasses() {
		if(registeredClasses==null) {
			return new Class[0];
		}
		
		Class<?>[] result = new Class<?>[registeredClasses.size()];
		
		return registeredClasses.toArray(result);
	}
	
	public static synchronized JAXBContext getSharedJAXBContext() throws JAXBException {
		if(sharedJAXBContext==null) {
			sharedJAXBContext = JAXBContext.newInstance(getRegisteredClasses());
		}
		
		return sharedJAXBContext;
	}
	
	public static synchronized void registerAdapter(Class<?> clazz, Object adapter) {
		if(clazz==null || illegalClasses.contains(clazz))
			throw new NullPointerException("Invalid target class"); //$NON-NLS-1$
		if(adapter==null)
			throw new NullPointerException("Invalid adapter object"); //$NON-NLS-1$
		
		if(registeredAdapters==null) {
			registeredAdapters = new HashMap<>();
		}
		
		if(registeredAdapters.containsKey(clazz))
			throw new DuplicateIdentifierException("Duplicate target class: "+clazz.getName()); //$NON-NLS-1$
		
		if(adapter instanceof ClassProxy
				|| adapter instanceof String
				|| adapter instanceof Class
				|| adapter instanceof XmlAdapter) {
			registeredAdapters.put(clazz, adapter);
		} else
			throw new IllegalArgumentException("Adapter type not supported: "+adapter.getClass().getName()); //$NON-NLS-1$
	}
	
	public static synchronized XmlAdapter<?, ?> getAdapter(Object obj) {
		if(obj==null || illegalClasses.contains(obj)) {
			return null;
		}
		if(registeredAdapters==null || registeredAdapters.isEmpty()) {
			return null;
		}
		
		Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
		
		Object adapter = null;
		
		for(Class<?> classKey : registeredAdapters.keySet()) {
			if(classKey.isAssignableFrom(clazz)) {
				adapter = registeredAdapters.get(classKey);
				break;
			}
		}
		
		if(adapter != null && !(adapter instanceof XmlAdapter)) {
			try {
				if(adapter instanceof String) {
					adapter = Class.forName((String)adapter);
				}
				
				if(adapter instanceof Class) {
					adapter = ((Class<?>) adapter).newInstance();
				} else if(adapter instanceof ClassProxy) {
					adapter = ((ClassProxy)adapter).loadObjectUnsafe();
				}
				
				// Safe result of instantiation
				registeredAdapters.put(clazz, adapter);
			} catch(Exception e) {
				LoggerFactory.log(JAXBUtils.class, Level.SEVERE, 
						"Failed to intantiate adapter: "+adapter, e); //$NON-NLS-1$
				
				// To prevent future costly calls we simply delete the registered adapter
				registeredAdapters.remove(clazz);
			}
		}
		
		return (XmlAdapter<?, ?>) adapter;
	}
	
	
	@XmlRootElement(name="list")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ListBuffer {
		
		@XmlElement(name="item")
		private List<Object> items = new ArrayList<>();
		
		public ListBuffer() {
			// no-op
		}
		
		public ListBuffer(Collection<Object> items) {
			this.items.addAll(items);
		}
		
		public ListBuffer(Object...items) {
			for(Object item : items) {
				this.items.add(item);
			}
		}
		
		public void add(Object item) {
			items.add(item);
		}
		
		public List<Object> getItems() {
			return items;
		}
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class MapImp<K, V> {
		@XmlElement(name="entry")
		private List<EntryImp<K, V>> list = new ArrayList<EntryImp<K, V>>();

		public MapImp() {
			// no-op
		}

		public MapImp(Map<K, V> map) {
			for (Map.Entry<K, V> entry : map.entrySet()) {
				list.add(new EntryImp<K, V>(entry));
			}
		}

		public List<EntryImp<K, V>> getList() {
			return list;
		}

		public void setList(List<EntryImp<K, V>> list) {
			this.list = list;
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class EntryImp<K, V> {

		@XmlElements({
			@XmlElement(name="keyInteger", type=Integer.class),
			@XmlElement(name="keyFloat", type=Float.class),
			@XmlElement(name="keyString", type=String.class),
			@XmlElement(name="keyLong", type=Long.class),
			@XmlElement(name="keyDouble", type=Double.class),
			@XmlElement(name="keyShort", type=Short.class),
			@XmlElement(name="keyBoolean", type=Boolean.class),
			@XmlElement(name="keyCharacter", type=Character.class),
			@XmlElement(name="keyByte", type=Byte.class),
			@XmlElement(name="keyObject"),
		})
		private K key;
		
		@XmlElements({
			@XmlElement(name="integer", type=Integer.class),
			@XmlElement(name="float", type=Float.class),
			@XmlElement(name="string", type=String.class),
			@XmlElement(name="long", type=Long.class),
			@XmlElement(name="double", type=Double.class),
			@XmlElement(name="short", type=Short.class),
			@XmlElement(name="boolean", type=Boolean.class),
			@XmlElement(name="character", type=Character.class),
			@XmlElement(name="byte", type=Byte.class),
			@XmlElement(name="value"),
		})
		private V value; 
		
	    public EntryImp() {
	    	// no-op
	    }
	    
	    public EntryImp(Map.Entry<K, V> entry) {
	        key = entry.getKey();
	        value = entry.getValue();
	    }
		
	    public K getKey() {
	        return key;
	    }
	 
	    public void setKey(K key) {
	        this.key = key;
	    }
	 
	    public V getValue() {
	        return value;
	    }
	 
	    public void setValue(V value) {
	        this.value = value;
	    }
	} 
}
