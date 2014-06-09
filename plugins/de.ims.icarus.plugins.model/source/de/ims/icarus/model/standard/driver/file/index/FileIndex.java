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
package de.ims.icarus.model.standard.driver.file.index;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import de.ims.icarus.model.standard.driver.file.FileChecksum;

/**
 * Mixin interface to indicate that an index manages
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface FileIndex {

	/**
	 * Returns the physical location of this index as a {@code Path} object.
	 * If this index was created programmatically in memory and does not map
	 * to any file based storage, this method returns {@code null}.
	 */
	Path getLocation();

	/**
	 * Returns the number of files this index covers (at least {@code 1}).
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
	 * Returns the 16 bytes shallow checksum for the data backing this index
	 * or {@code null} if the index was created in memory programmatically.
	 * Note that this is the stored checksum for the specified file, i.e. the
	 * last encountered state of the file. If the current checksum for a file
	 * differs from the value returned by this method than an inconsistency
	 * has been detected.
	 *
	 * @param fileIndex
     * @throws IndexOutOfBoundsException if the fileIndex is out of range
     *         (<tt>index &lt; 0 || index &gt;= getFileCount()</tt>)
     *
	 * @see FileChecksum
	 */
	FileChecksum getChecksum(int fileIndex);

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
	void refreshChecksum(int fileIndex) throws IOException;

	/**
	 * Connects to the data of this index by opening a channel which allows
	 * random access to the byte data. Note that it is the responsibility of the
	 * reader or writer that requests the channel, to actually close it once
	 * the desired operations are performed on it!
	 *
	 * @throws IOException
	 */
	SeekableByteChannel openChannel() throws IOException;
}
