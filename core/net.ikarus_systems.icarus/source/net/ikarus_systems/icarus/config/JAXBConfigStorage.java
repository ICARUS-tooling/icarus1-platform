/**
 * 
 */
package net.ikarus_systems.icarus.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class JAXBConfigStorage extends AbstractConfigStorage {
	
	protected File file;
	
	protected Buffer buffer;
	
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
		return buffer==null ? null : buffer.getValue(path);
	}

	@Override
	protected boolean setValue0(String path, Object value) {
		if(buffer==null) {
			buffer = new Buffer();
		}
		
		return buffer.setValue(path, value);
		
	}

	@Override
	protected void read() throws Exception {
		if(!file.exists() || file.length()==0) {
			return;
		}

		JAXBContext context = JAXBContext.newInstance(Buffer.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		buffer = (Buffer) unmarshaller.unmarshal(file);
	}

	@Override
	protected void write() throws Exception {
		JAXBContext context = JAXBContext.newInstance(Buffer.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(buffer, file);
	}

	@XmlRootElement(name="properties")
	private static class Buffer {
		
		@XmlElement(name="entries",nillable=true)
		private Map<String, Object> entries = new HashMap<>();
		
		public Object getValue(String path) {
			return entries.get(path);
		}

		public boolean setValue(String path, Object value) {
			if(value==null || !value.equals(entries.get(path))) {
				entries.put(path, value);
				return true;
			}
			return false;
		}
	}
}
