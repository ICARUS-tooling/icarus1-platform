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

import java.io.Closeable;
import java.io.IOException;

/**
 * Implements a reader for {@code ChunkIndex} data.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ChunkIndexReader extends Closeable {

	/**
	 * Returns the data source of this reader.
	 */
	ChunkIndex getChunkIndex();

	boolean open() throws IOException;

	/**
	 * Returns the number of chunks in the underlying
	 * {@code ChunkIndex}.
	 *
	 * @return
	 */
	long getChunkCount();

	/**
	 * Returns the position at the file level a chunk specified
	 * via the {@code index} parameter is located at. Note that for
	 * chunk indices that only cover a single corpus file this method
	 * will always return {@code 0}
	 *
	 * @param index
	 * @return
	 */
	int getFileId(long index);

	/**
	 * Points to the exact byte offset within a file obtained via
	 * {@link #getFileId(long)} (with the same {@code index} argument!)
	 * that marks the <i>begin</i> of the specified data chunk.
	 *
	 * @param index
	 * @return
	 */
	long getBeginOffset(long index);

	/**
	 * Points to the exact byte offset within a file obtained via
	 * {@link #getFileId(long)} (with the same {@code index} argument!)
	 * that marks the <i>end</i> of the specified data chunk.
	 *
	 * @param index
	 * @return
	 */
	long getEndOffset(long index);

	/**
	 * When the underlying index stores mappings for the bounds of containers
	 * (signaled by {@link ChunkIndex#isMappingElements()} than this method
	 * will return the index of the first element in the specified container
	 * relative to the underlying layer.
	 *
	 * @param index
	 * @return
	 * @throws UnsupportedOperationException if the underlying index does not
	 * support mapping of members
	 */
	long getBeginIndex(long index);


	/**
	 * When the underlying index stores mappings for the bounds of containers
	 * (signaled by {@link ChunkIndex#isMappingElements()} than this method
	 * will return the index of the last element in the specified container
	 * relative to the underlying layer.
	 *
	 * @param index
	 * @return
	 * @throws UnsupportedOperationException if the underlying index does not
	 * support mapping of members
	 */
	long getEndIndex(long index);
}
