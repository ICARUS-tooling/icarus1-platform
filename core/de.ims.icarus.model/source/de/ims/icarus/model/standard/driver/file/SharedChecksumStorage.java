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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.model.standard.driver.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.xml.jaxb.JAXBGate;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SharedChecksumStorage extends JAXBGate<SharedChecksumStorage.StorageBuffer> {

	private final Map<Path, FileChecksum> checksums = new HashMap<>();

	private int useCount = 0;

	private static final Map<Path, SharedChecksumStorage> instances = new WeakHashMap<>();

	public static SharedChecksumStorage getStorage(Path file) {
		if (file == null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$

		synchronized (instances) {
			SharedChecksumStorage storage = instances.get(file);

			if(storage==null) {
				storage = new SharedChecksumStorage(file);
				instances.put(file, storage);
			}

			storage.use();

			return storage;
		}
	}

	private static void destroy(SharedChecksumStorage storage) {
		synchronized (instances) {
			try {
				// Ensure the data gets saved properly
				storage.synchronize();
			} finally {
				// Finally discard storage
				instances.remove(storage.getFile());
			}
		}
	}

	private SharedChecksumStorage(Path file) {
		super(file);
	}

	private synchronized void use() {
		useCount++;
	}

	/**
	 * Decrements the internal usage counter and in the case it reaches
	 * zero, destroys this storage object and removes it from the internal
	 * shared cache.
	 */
	public synchronized void close() {
		useCount--;

		if(useCount<=0) {
			destroy(this);
		}
	}

	/**
	 * @see de.ims.icarus.xml.jaxb.JAXBGate#readBuffer(java.lang.Object)
	 */
	@Override
	protected synchronized void readBuffer(StorageBuffer buffer)
			throws Exception {
		checksums.clear();

		for(StorageEntry entry : buffer.items) {
			Path path = entry.getPath();
			FileChecksum checksum = entry.getChecksum();

			//TODO resolve path relative to storage?

			checksums.put(path, checksum);
		}
	}

	/**
	 * @see de.ims.icarus.xml.jaxb.JAXBGate#createBuffer()
	 */
	@Override
	protected synchronized StorageBuffer createBuffer()
			throws Exception {
		if(checksums.isEmpty()) {
			return null;
		}

		int size = checksums.size();
		StorageEntry[] entries = new StorageEntry[size];

		int index = 0;
		for(Entry<Path, FileChecksum> entry : checksums.entrySet()) {
			entries[index++] = new StorageEntry(entry.getKey(), entry.getValue());
		}

		return new StorageBuffer(entries);
	}

	public synchronized void synchronize() {
		// Only save to disc when actual new entries exist
		if(!checkStorage()) {
			try {
				saveBufferNow();
			} catch (Exception e) {
				LoggerFactory.error(this, "Failed to synchronize checksum storage to file", e); //$NON-NLS-1$
			}
		}
	}

	public synchronized void setChecksum(Path path, FileChecksum checksum) {
		checkStorage();

		checksums.put(path, checksum);
	}

	public synchronized void removeChecksum(Path path) {
		checkStorage();

		checksums.remove(path);
	}

	public synchronized FileChecksum getChecksum(Path path) {
		checkStorage();

		return checksums.get(path);
	}

	public synchronized boolean compareAndSetChecksum(Path path, FileChecksum checksum) {
		checkStorage();

		FileChecksum old = checksums.get(path);

		checksums.put(path, checksum);

		return old==null || !old.equals(checksum);
	}

	/**
	 * Loads the checksum storage if necessary and returns {@code true} if at least one
	 * mapping was loaded.
	 */
	private synchronized boolean checkStorage() {
		if(checksums.isEmpty()) {
			try {
				loadBuffer();
			} catch (Exception e) {
				LoggerFactory.error(this, "Failed to load checksum storage: "+getFile(), e); //$NON-NLS-1$
			}

			return !checksums.isEmpty();
		}

		return false;
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlRootElement(name="checksums")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StorageBuffer {
		@XmlElement(name="file")
		private StorageEntry[] items;

		private StorageBuffer() {
			// no-op
		}

		private StorageBuffer(StorageEntry[] items) {
			this.items = items;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlRootElement(name="file")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StorageEntry {
		@XmlAttribute(name="path")
		private String path;
		@XmlAttribute(name="checksum")
		private String checksum;

		private StorageEntry() {
			// no-op
		}

		private StorageEntry(Path path, FileChecksum checksum) {
			this.path = path.toString();
			this.checksum = checksum.toString();
		}

		public Path getPath() {
			return Paths.get(path);
		}

		public FileChecksum getChecksum() {
			return FileChecksum.parse(checksum);
		}
	}
}
