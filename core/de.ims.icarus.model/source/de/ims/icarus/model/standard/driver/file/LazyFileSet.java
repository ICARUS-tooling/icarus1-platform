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

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.io.PathResolver;
import de.ims.icarus.model.io.ResourcePath;
import de.ims.icarus.util.CorruptedStateException;

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
public class LazyFileSet implements FileSet {

	private final TIntObjectMap<Path> paths = new TIntObjectHashMap<>();
	private final PathResolver pathResolver;
	private final SharedChecksumStorage storage;

	/**
	 * Creates a new {@code SingletonFileSet} that points to the given {@code file}
	 * and uses the provided {@link SharedChecksumStorage} to store the checksum.
	 *
	 * @param pathResolver
	 * @param storage
	 */
	public LazyFileSet(PathResolver pathResolver, SharedChecksumStorage storage) {
		if (pathResolver == null)
			throw new NullPointerException("Invalid pathResolver"); //$NON-NLS-1$
		if (storage == null)
			throw new NullPointerException("Invalid storage"); //$NON-NLS-1$

		this.storage = storage;

		this.pathResolver = pathResolver;
	}

	/**
	 * Creates a new {@code SingletonFileSet} that points to the given {@code file}.
	 * The checksum storage is obtained by calling {@link SharedChecksumStorage#getStorage(Path)}
	 * with the {@code checksumFile} argument.
	 *
	 * @param pathResolver
	 * @param checksumFile
	 */
	public LazyFileSet(PathResolver pathResolver, Path checksumFile) {
		if (pathResolver == null)
			throw new NullPointerException("Invalid pathResolver"); //$NON-NLS-1$
		if (checksumFile == null)
			throw new NullPointerException("Invalid checksumFile"); //$NON-NLS-1$

		this.storage = SharedChecksumStorage.getStorage(checksumFile);

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

	private FileChecksum createChecksum(Path path) throws IOException {
		return FileChecksum.compute(path);
	}

	private FileChecksum refreshChecksum(Path path) throws IOException {
		FileChecksum checksum = null;

		try {
			checksum = createChecksum(path);
		} finally {
			if(checksum==null) {
				storage.removeChecksum(path);
			} else {
				storage.setChecksum(path, checksum);
			}
		}

		return checksum;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#getChecksum(int)
	 */
	@Override
	public synchronized FileChecksum getChecksum(int fileIndex) {
		return storage.getChecksum(getFileAt(fileIndex));
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#verifyChecksum(int)
	 */
	@Override
	public synchronized boolean verifyChecksum(int fileIndex) throws IOException, ModelException {
		Path path = getFileAt(fileIndex);

		FileChecksum newValue = createChecksum(path);

		if(newValue==null)
			throw new ModelException(ModelError.DRIVER_CHECKSUM_FAIL,
					"Unable to refresh checksum for file: "+path); //$NON-NLS-1$

		return storage.compareAndSetChecksum(path, newValue);
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#refreshChecksum(int)
	 */
	@Override
	public synchronized void refreshChecksum(int fileIndex) throws IOException {
		refreshChecksum(getFileAt(fileIndex));
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#synchronize()
	 */
	@Override
	public void synchronize() {
		storage.synchronize();
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#close()
	 */
	@Override
	public void close() {
		storage.close();
	}
}
