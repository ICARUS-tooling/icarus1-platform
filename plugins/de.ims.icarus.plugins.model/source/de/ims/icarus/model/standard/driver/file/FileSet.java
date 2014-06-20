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
import java.nio.file.Path;

import de.ims.icarus.model.ModelException;

/**
 * Stores a collection of files together with their respective checksums.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface FileSet {

	/**
	 * Returns the number of files this storage covers (at least {@code 1}).
	 */
	int getFileCount();

	/**
	 * Returns the physical location of the file specified by the {@code fileIndex}
	 * argument.
	 *
	 * @param fileIndex
	 * @return
	 */
	Path getFileAt(int fileIndex);

	/**
	 * Returns the 16 bytes shallow checksum stored for the specified file, i.e. the
	 * last encountered state of the file. If the current checksum for a file
	 * differs from the value returned by this method than an inconsistency
	 * has been detected.
	 * <p>
	 * If no checksum has been computed for the specified file as of now, this method
	 * returns {@code null}.
	 *
	 * @param fileIndex
     * @return
     * @throws IndexOutOfBoundsException if the fileIndex is out of range
     *         (<tt>index &lt; 0 || index &gt;= getFileCount()</tt>)
     *
	 * @see FileChecksum
	 */
	FileChecksum getChecksum(int fileIndex) throws ModelException;

	/**
	 * Recomputes the checksum for the specified file and compares it with the
	 * stored value. Returns {@code true} iff the stored checksum is still correct.
	 *
	 * @param fileIndex
	 * @return
     * @throws IndexOutOfBoundsException if the fileIndex is out of range
     *         (<tt>index &lt; 0 || index &gt;= getFileCount()</tt>)
	 */
	boolean verifyChecksum(int fileIndex) throws IOException, ModelException;

	/**
	 * Forces the index to recompute the checksum for a specified file and
	 * refresh its storage so that further calls to {@link #getChecksum(int)}
	 * with the same index will yield the new checksum.
	 *
	 * @param fileIndex
     * @throws IndexOutOfBoundsException if the fileIndex is out of range
     *         (<tt>index &lt; 0 || index &gt;= getFileCount()</tt>)
	 *
	 * @see FileChecksum
	 */
	void refreshChecksum(int fileIndex) throws IOException, ModelException;

	void synchronize();

	void close();
}
