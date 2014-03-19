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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractChunkIndex implements ChunkIndex {

	private final String layerUid;
	private final boolean mapElements;
	private final List<Path> files;
	private final Map<Path, FileChecksum> checksums = new HashMap<>();

	public AbstractChunkIndex(String layerUid, boolean mapElements, List<Path> files) {
		if (layerUid == null)
			throw new NullPointerException("Invalid layerUid"); //$NON-NLS-1$
		if (files == null)
			throw new NullPointerException("Invalid files"); //$NON-NLS-1$
		if(files.isEmpty())
			throw new IllegalArgumentException("Empty file list for index"); //$NON-NLS-1$

		this.layerUid = layerUid;
		this.mapElements = mapElements;
		this.files = new ArrayList<>(files);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#getLayerUid()
	 */
	@Override
	public String getLayerUid() {
		return layerUid;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#getFileCount()
	 */
	@Override
	public int getFileCount() {
		return files.size();
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#getFileAt(int)
	 */
	@Override
	public Path getFileAt(int fileIndex) {
		if(files.isEmpty())
			throw new IllegalStateException("No files defined"); //$NON-NLS-1$

		return files.get(fileIndex);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#getChecksum(int)
	 */
	@Override
	public FileChecksum getChecksum(int fileIndex) {
		Path path = getFileAt(fileIndex);

		FileChecksum checksum = checksums.get(path);
		if(checksum==null)
			throw new IllegalStateException("No checksum stored for path: "+path); //$NON-NLS-1$

		return checksum;
	}

	/**
	 * Refreshes the checksum for the specified file using the
	 * {@link FileChecksum#compute(Path)} method with the {@code Path} object
	 * stored for the given {@code fileIndex}. If the computation of a new
	 * checksum fails, then the previously stored checksum will be deleted.
	 *
	 * @throws IOException
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#refreshChecksum(int)
	 */
	@Override
	public void refreshChecksum(int fileIndex) throws IOException {
		Path path = getFileAt(fileIndex);

		FileChecksum checksum = null;

		try {
			checksum = computeChecksum(path);
		} finally {
			if(checksum==null) {
				checksums.remove(path);
			} else {
				checksums.put(path, checksum);
			}
		}
	}

	protected FileChecksum computeChecksum(Path path) throws IOException {
		return FileChecksum.compute(path);
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndex#isMappingElements()
	 */
	@Override
	public boolean isMappingElements() {
		return mapElements;
	}
}
