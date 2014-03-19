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
import java.util.List;

import de.ims.icarus.io.MemoryByteBuffer;

/**
 * Implements an in-memory storage for chunk index data.
 * The implementation uses an byte array based buffer structure
 * that creates channels to read or write through to that internal
 * byte array.
 * <p>
 * Since the capacity for arrays is limited to {@link Integer#MAX_VALUE},
 * the {@link #isLargeIndex()} method will always return {@code false}.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MemoryChunkIndex extends AbstractChunkIndex {

	private final MemoryByteBuffer buffer;

	public MemoryChunkIndex(String layerUid, boolean mapElements,
			List<Path> files) {
		this(layerUid, mapElements, files, new MemoryByteBuffer());
	}

	/**
	 * @param layerUid
	 * @param mapElements
	 * @param files
	 */
	public MemoryChunkIndex(String layerUid, boolean mapElements,
			List<Path> files, MemoryByteBuffer buffer) {
		super(layerUid, mapElements, files);

		if (buffer == null)
			throw new NullPointerException("Invalid buffer"); //$NON-NLS-1$

		this.buffer = buffer;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#getLocation()
	 */
	@Override
	public Path getLocation() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#isLargeIndex()
	 */
	@Override
	public boolean isLargeIndex() {
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#openChannel()
	 */
	@Override
	public SeekableByteChannel openChannel() throws IOException {
		return buffer.newChannel();
	}

}
