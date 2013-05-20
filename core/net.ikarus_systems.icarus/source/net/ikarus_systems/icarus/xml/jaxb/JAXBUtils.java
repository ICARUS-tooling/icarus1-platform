/**
 * 
 */
package net.ikarus_systems.icarus.xml.jaxb;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class JAXBUtils {
	
	@XmlRootElement
	public static class Test1 {
		@XmlAttribute
		private String value= "test";
		
		@XmlElement
		private int index = 123;
	}
	
	@XmlRootElement
	public static class Test2 {
		@XmlElement
		@XmlList
		private int[] items = {1, 2, 3, 4, 5};
	}
	
	public static void main(String[] args) throws Throwable {
		Test1 t1 = new Test1();
		Test2 t2 = new Test2();
		
		OutputStream out = new FileOutputStream("temp/test.txt"); 
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
				out);
		JAXBContext context = JAXBContext.newInstance(Test1.class, Test2.class);
		
		writer.writeStartDocument();
		
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.setProperty(Marshaller.JAXB_FRAGMENT, true);
		m.marshal(t1, out);
		m.marshal(t2, out);
		
		writer.writeEndDocument();
		
		writer.close();
	}
	
	private static Collection<Class<?>> registeredClasses;
	
	private static JAXBContext sharedJAXBContext;

	private JAXBUtils(){
		// no-op
	};
	
	public static synchronized void registerClass(Class<?> clazz) {
		if(clazz==null)
			throw new IllegalArgumentException("Invalid class"); //$NON-NLS-1$
		
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
