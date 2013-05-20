/**
 * 
 */
package net.ikarus_systems.icarus.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.util.CollectionUtils;
import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.xml.jaxb.JAXBUtils;
import net.ikarus_systems.icarus.xml.jaxb.MapAdapter;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class JAXBConfigStorage extends AbstractConfigStorage {
	
	static {
		JAXBUtils.registerClass(Buffer.class);
	}
	
	protected File file;
	
	protected Map<String, Object> map;
	
	public JAXBConfigStorage(String path, int strategy) {
		this(new File(path), strategy);
	}
	
	public JAXBConfigStorage(String path) {
		this(new File(path), MANUAL_SAVING);
	}
	
	public JAXBConfigStorage(File file) {
		this(file, MANUAL_SAVING);
	}
	
	public JAXBConfigStorage(File file, int strategy) {
		Exceptions.testNullArgument(file, "file"); //$NON-NLS-1$
		
		//System.out.println(file.getAbsolutePath());
		
		this.file = file;
		
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to create config storage file: "+file, e); //$NON-NLS-1$
			}
		}
		
		setStrategy(strategy);
	}

	@Override
	public Object getValue(String path) {
		return map==null ? null : map.get(path);
	}

	@Override
	protected boolean setValue0(String path, Object value) {
		if(map==null) {
			map = new HashMap<>();
		}

		if(value==null || !value.equals(map.get(path))) {
			map.put(path, value);
			return true;
		}
		return false;
		
	}

	@Override
	protected void read() throws Exception {
		if(!file.exists() || file.length()==0) {
			return;
		}

		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Buffer buffer = (Buffer) unmarshaller.unmarshal(file);
		map = buffer.toMap();
	}

	@Override
	protected void write() throws Exception {
		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		Buffer buffer = new Buffer();
		buffer.fromMap(map);
		
		marshaller.marshal(buffer, file);
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlRootElement(name="properties")
	private static class Buffer {
		
		@XmlElements({
			@XmlElement(name="entry", type=Entry.class),
			@XmlElement(name="listEntry", type=ListEntry.class),
			@XmlElement(name="mapEntry", type=MapEntry.class),
		})
		private List<Entry> entries = new ArrayList<>();
		
		private Map<String, Object> toMap() {
			Map<String, Object> map = new HashMap<>(entries.size());
			
			for(Entry entry : entries) {
				map.put(entry.getKey(), entry.getValue());
			}
			
			return map;
		}
		
		private void fromMap(Map<String, Object> map) {
			entries = new ArrayList<>(map.size());
			
			for(Map.Entry<String, Object> mapEntry : map.entrySet()) {
				Entry entry;
				
				if(mapEntry.getValue() instanceof Map) {
					entry = new MapEntry(mapEntry);
				} else if(mapEntry.getValue() instanceof List) {
					entry = new ListEntry(mapEntry);
				} else {
					entry = new Entry(mapEntry);
				}
				
				entries.add(entry);
			}
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement
	private static class Entry {
		
		@XmlAttribute
		private String key;
		
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
		private Object value;
		
		private Entry() {
			// no-op
		}
		
		private Entry(Map.Entry<String, Object> mapEntry) {
			key = mapEntry.getKey();
			value = mapEntry.getValue();
		}
		
		String getKey() {
			return key;
		}
		
		Object getValue() {
			return value;
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement
	private static class ListEntry extends Entry {
		
		@XmlAttribute
		private String key;
		
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
			@XmlElement(name="item"),
		})
		private Object[] value;
		
		private ListEntry() {
			// no-op
		}
		
		private ListEntry(Map.Entry<String, Object> mapEntry) {
			key = mapEntry.getKey();
			value = ((List<?>) mapEntry.getValue()).toArray();
		}
		
		@Override
		Object getValue() {
			return CollectionUtils.asList(value);
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement
	private static class MapEntry extends Entry {
		
		@XmlAttribute
		private String key;
		
		@XmlElement
		@XmlJavaTypeAdapter(value=MapAdapter.class)
		private Map<?,?> value;
		
		private MapEntry() {
			// no-op
		}
		
		private MapEntry(Map.Entry<String, Object> mapEntry) {
			key = mapEntry.getKey();
			value = (Map<?,?>) mapEntry.getValue();
		}
		
		@Override
		Object getValue() {
			return (Map<?, ?>) value;
		}
	}
}
