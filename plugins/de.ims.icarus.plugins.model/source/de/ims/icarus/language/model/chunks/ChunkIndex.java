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
package de.ims.icarus.language.model.chunks;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

/**
 * A simple lookup structure to map from chunk index values to actual
 * physical file locations and byte offsets within those files. To
 * actually access the data within an index or in order to modify it,
 * one has to use appropriate implementations of the {@link ChunkIndexReader}
 * and {@link ChunkIndexWriter} interfaces. Note that there are in principal
 * 3 different types (or levels) of chunk indices possible:
 *
 * <ul>
 * <li><b>Small</b> indices using integer representations for both element index
 * values and byte offsets. This type of index is recommended for small corpora that
 * neither host too great a number of elements, nor reside in multi-gigabyte files.</li>
 * <li><b>Medium</b> indices that still only host a number of elements that can be
 * addressed with integer values, but map to one or more files which exceed the
 * {@link Integer#MAX_VALUE} limit in terms of total size. This type of index differs from the <i>small</i>
 * version only in the value range available for byte offsets, which in this case is {@code long}.</li>
 * <li><b>Large</b> indices finally provide the means to map extremely big corpora that by far exceed
 * the value range of {@link Integer#MAX_VALUE} for both element index and byte offset. Unlike the
 * aforementioned two types, this one no longer can be represented as one big primitive array holding
 * all the index data. Quite the opposite, it requires another level of caching to account for the fact,
 * that an index this large would itself be too memory consuming to be loaded as a whole. Implementations
 * for this type will try to keep the footprint of loaded chunks as small as possible while still
 * providing fast response times for read/write operations.</li>
 * </ul>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ChunkIndex {

	/**
	 * Returns the physical location of this index as a {@code Path} object.
	 * If this index was created programmatically in memory and does not map
	 * to any file based storage, this method returns {@code null}.
	 */
	Path getLocation();

	/**
	 * Returns the unique id of the layer this index was created for.
	 */
	String getLayerUid();

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
	 * Signals whether or not the underlying index data is meant to address
	 * very large file data (that is data exceeding {@link Integer#MAX_VALUE}
	 * in size). This info is meant as a hint what type of implementation to
	 * use for {@link ChunkIndexReader} and {@link ChunkIndexWriter}.
	 *
	 * @return
	 */
	boolean isLargeIndex();

	/**
	 *
	 * @return
	 */
	boolean isMappingElements();

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
