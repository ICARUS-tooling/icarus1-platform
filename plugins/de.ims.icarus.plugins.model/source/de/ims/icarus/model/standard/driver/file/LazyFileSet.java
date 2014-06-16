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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.io.PathResolver;
import de.ims.icarus.model.io.ResourcePath;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.xml.jaxb.JAXBGate;

/**
 * Implements a {@link FileSet} that is linked to a central storage file and
 * manages a collection of target files that are resolved with the help of a
 * {@link PathResolver}, provided at construction time. The implementation
 * resolves paths to target files lazily, as soon as they are requested. The
 * same policy holds true for the corresponding checksums.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LazyFileSet extends JAXBGate<LazyFileSet.StorageBuffer> implements FileSet {

	private final TIntObjectMap<Path> paths = new TIntObjectHashMap<>();
	private final Map<Path, FileChecksum> checksums = new HashMap<>();
	private final PathResolver pathResolver;

	public LazyFileSet(PathResolver pathResolver, Path storage) {
		super(storage);
		if (pathResolver == null)
			throw new NullPointerException("Invalid pathResolver"); //$NON-NLS-1$

		this.pathResolver = pathResolver;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#getFileCount()
	 */
	@Override
	public int getFileCount() {
		return pathResolver.getPathCount();
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#getFileAt(int)
	 */
	@Override
	public synchronized Path getFileAt(int fileIndex) {
		Path path = paths.get(fileIndex);
		if(path==null) {
			ResourcePath resourcePath = pathResolver.getPath(fileIndex);
			if(resourcePath==null)
				throw new IndexOutOfBoundsException("No file available for index: "+fileIndex); //$NON-NLS-1$
			if(resourcePath.getType()!=LocationType.FILE)
				throw new CorruptedStateException("Resolver returned unsupported path type: "+resourcePath);//TODO more info in exception //$NON-NLS-1$

			path = Paths.get(resourcePath.getPath());

//			if(!path.isAbsolute()) {
//				path = path.toAbsolutePath();
//			}

			paths.put(fileIndex, path);
		}

		return path;
	}

	/**
	 * Loads the checksum storage if necessary and returns {@code true} if at least one
	 * mapping was loaded.
	 */
	private boolean checkStorage() {
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

	private FileChecksum getChecksum(Path path) {
		checkStorage();

		return checksums.get(path);
	}

	private FileChecksum createChecksum(Path path) throws IOException {
		return FileChecksum.compute(path);
	}

	private FileChecksum refreshChecksum(Path path) throws IOException {
		checkStorage();

		FileChecksum checksum = null;

		try {
			checksum = createChecksum(path);
		} finally {
			if(checksum==null) {
				checksums.remove(path);
			} else {
				checksums.put(path, checksum);
			}
		}

		return checksum;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#getChecksum(int)
	 */
	@Override
	public synchronized FileChecksum getChecksum(int fileIndex) {
		return getChecksum(getFileAt(fileIndex));
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#verifyChecksum(int)
	 */
	@Override
	public synchronized boolean verifyChecksum(int fileIndex) throws IOException, ModelException {
		Path path = getFileAt(fileIndex);

		FileChecksum oldValue = getChecksum(path);

		FileChecksum newValue = refreshChecksum(path);

		//TODO maybe signal error if one of the checksum values is null?

		return oldValue==null || oldValue.equals(newValue);
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#refreshChecksum(int)
	 */
	@Override
	public synchronized void refreshChecksum(int fileIndex) throws IOException {
		refreshChecksum(getFileAt(fileIndex));
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

		int size = getFileCount();
		StorageEntry[] entries = new StorageEntry[size];

		for(int i=0; i<size; i++) {
			Path path = paths.get(i);
			FileChecksum checksum = checksums.get(path);

			entries[i] = new StorageEntry(path, checksum);
		}

		return new StorageBuffer(entries);
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#synchronize()
	 */
	@Override
	public void synchronize() {
		if(!checkStorage()) {
			try {
				saveBufferNow();
			} catch (Exception e) {
				LoggerFactory.error(this, "Failed to synchronize checksum storage to file", e); //$NON-NLS-1$
			}
		}
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
