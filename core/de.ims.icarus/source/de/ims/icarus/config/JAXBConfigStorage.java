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
package de.ims.icarus.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.xml.jaxb.JAXBUtils;
import de.ims.icarus.xml.jaxb.MapAdapter;


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

	protected Path file;

	protected Map<String, Object> map;

	public JAXBConfigStorage(String path, int strategy) {
		this(Paths.get(path), strategy);
	}

	public JAXBConfigStorage(String path) {
		this(Paths.get(path), MANUAL_SAVING);
	}

	public JAXBConfigStorage(Path file) {
		this(file, MANUAL_SAVING);
	}

	public JAXBConfigStorage(Path file, int strategy) {
		Exceptions.testNullArgument(file, "file"); //$NON-NLS-1$

		//System.out.println(file.getAbsolutePath());

		this.file = file;

		if(Files.notExists(file)) {
			try {
				Files.createFile(file);
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

		if(value==null || value instanceof Collection || !value.equals(map.get(path))) {
			map.put(path, value);
			return true;
		}
		return false;

	}

	@Override
	protected void read() throws Exception {
		if(Files.notExists(file) || Files.size(file)==0) {
			return;
		}

		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Buffer buffer = (Buffer) unmarshaller.unmarshal(Files.newInputStream(file));
		map = buffer.toMap();
	}

	@Override
	protected void write() throws Exception {
		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		Buffer buffer = new Buffer();
		buffer.fromMap(map);

		marshaller.marshal(buffer, Files.newOutputStream(file));
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
		private List<ConfigEntry> entries = new ArrayList<>();

		private Map<String, Object> toMap() {
			Map<String, Object> map = new HashMap<>(entries.size());

			for(ConfigEntry entry : entries) {
				map.put(entry.getKey(), entry.getValue());
			}

			return map;
		}

		private void fromMap(Map<String, Object> map) {
			entries = new ArrayList<>(map.size());

			for(Map.Entry<String, Object> mapEntry : map.entrySet()) {
				ConfigEntry entry;

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

	private interface ConfigEntry {

		String getKey();

		Object getValue();
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement
	private static class Entry implements ConfigEntry {

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

		@Override
		public
		String getKey() {
			return key;
		}

		@Override
		public
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
	private static class ListEntry implements ConfigEntry {

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
		public
		Object getValue() {
			return CollectionUtils.asList(value);
		}

		/**
		 * @see de.ims.icarus.config.JAXBConfigStorage.ConfigEntry#getKey()
		 */
		@Override
		public String getKey() {
			return key;
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
	private static class MapEntry implements ConfigEntry {

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
		public
		Object getValue() {
			return (Map<?, ?>) value;
		}

		/**
		 * @see de.ims.icarus.config.JAXBConfigStorage.ConfigEntry#getKey()
		 */
		@Override
		public String getKey() {
			return key;
		}
	}
}
