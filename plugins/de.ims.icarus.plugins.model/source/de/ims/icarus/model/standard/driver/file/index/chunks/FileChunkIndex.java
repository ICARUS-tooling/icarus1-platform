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
package de.ims.icarus.model.standard.driver.file.index.chunks;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FileChunkIndex extends DefaultChunkIndex {

	private final Path path;
	private final boolean largeIndex;

	public FileChunkIndex(String layerUid, boolean mapElements,
			List<Path> files, Path path, boolean largeIndex) {
		super(layerUid, mapElements, files);

		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$

		this.path = path;
		this.largeIndex = largeIndex;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#getLocation()
	 */
	@Override
	public Path getLocation() {
		return path;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#isLargeIndex()
	 */
	@Override
	public boolean isLargeIndex() {
		return largeIndex;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#openChannel()
	 */
	@Override
	public SeekableByteChannel openChannel() throws IOException {
		return Files.newByteChannel(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
	}
}
