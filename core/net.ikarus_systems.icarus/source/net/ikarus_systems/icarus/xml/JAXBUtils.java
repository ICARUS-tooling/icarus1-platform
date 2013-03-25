/**
 * 
 */
package net.ikarus_systems.icarus.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class JAXBUtils {
	
	private static List<Object> contextELements;

	private JAXBUtils(){
		// no-op
	};
	
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
	
	public static class MapImp<K, V> {
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
	
	public static class EntryImp<K, V> {
		private K key;
		private V value; 
		
	    public EntryImp() {
	    	// no-op
	    }
	    
	    public EntryImp(Map.Entry<K, V> entry) {
	        key = entry.getKey();
	        value = entry.getValue();
	    }
		
	    @XmlElement
	    public K getKey() {
	        return key;
	    }
	 
	    public void setKey(K key) {
	        this.key = key;
	    }
	 
	    @XmlElement
	    public V getValue() {
	        return value;
	    }
	 
	    public void setValue(V value) {
	        this.value = value;
	    }
	} 
}
