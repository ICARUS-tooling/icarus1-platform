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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SingletonFileSet implements FileSet {

	private final Path file;
	private final SharedChecksumStorage storage;

	/**
	 * Creates a new {@code SingletonFileSet} that points to the given {@code file}
	 * and uses the provided {@link SharedChecksumStorage} to store the checksum.
	 *
	 * @param file
	 * @param storage
	 */
	public SingletonFileSet(Path file, SharedChecksumStorage storage) {
		if (file == null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$
		if (storage == null)
			throw new NullPointerException("Invalid storage"); //$NON-NLS-1$

		if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS))
			throw new IllegalArgumentException("File does not exis: "+file); //$NON-NLS-1$

		this.file = file;
		this.storage = storage;
	}

	/**
	 * Creates a new {@code SingletonFileSet} that points to the given {@code file}.
	 * The checksum storage is obtained by calling {@link SharedChecksumStorage#getStorage(Path)}
	 * with the {@code checksumFile} argument.
	 *
	 * @param file
	 * @param storage
	 */
	public SingletonFileSet(Path file, Path checksumFile) {
		if (file == null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$
		if (checksumFile == null)
			throw new NullPointerException("Invalid checksumFile"); //$NON-NLS-1$

		if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS))
			throw new IllegalArgumentException("File does not exis: "+file); //$NON-NLS-1$

		this.file = file;
		this.storage = SharedChecksumStorage.getStorage(checksumFile);
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#getFileCount()
	 */
	@Override
	public int getFileCount() {
		return 1;
	}

	private void checkIndex(int fileIndex) {
		if(fileIndex!=0)
			throw new IllegalArgumentException("Invalid file index: "+fileIndex+" - only legal value is 0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#getFileAt(int)
	 */
	@Override
	public Path getFileAt(int fileIndex) {
		checkIndex(fileIndex);

		return file;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#getChecksum(int)
	 */
	@Override
	public FileChecksum getChecksum(int fileIndex) throws ModelException {
		checkIndex(fileIndex);

		return storage.getChecksum(file);
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
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#verifyChecksum(int)
	 */
	@Override
	public boolean verifyChecksum(int fileIndex) throws IOException,
			ModelException {
		checkIndex(fileIndex);

		FileChecksum newValue = createChecksum(file);

		if(newValue==null)
			throw new ModelException(ModelError.DRIVER_CHECKSUM_FAIL,
					"Unable to refresh checksum for file: "+file); //$NON-NLS-1$

		return storage.compareAndSetChecksum(file, newValue);
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.FileSet#refreshChecksum(int)
	 */
	@Override
	public void refreshChecksum(int fileIndex) throws IOException,
			ModelException {
		checkIndex(fileIndex);

		refreshChecksum(file);
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
