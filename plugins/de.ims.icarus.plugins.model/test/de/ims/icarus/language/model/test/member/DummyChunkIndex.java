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
package de.ims.icarus.language.model.test.member;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import de.ims.icarus.model.standard.driver.file.FileChecksum;
import de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyChunkIndex implements ChunkIndex {

	private boolean largeIndex;

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#getLocation()
	 */
	@Override
	public Path getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#getLayerUid()
	 */
	@Override
	public String getLayerUid() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#getFileCount()
	 */
	@Override
	public int getFileCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#getFileAt(int)
	 */
	@Override
	public Path getFileAt(int fileIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#getChecksum(int)
	 */
	@Override
	public FileChecksum getChecksum(int fileIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#refreshChecksum(int)
	 */
	@Override
	public void refreshChecksum(int fileIndex) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#isLargeIndex()
	 */
	@Override
	public boolean isLargeIndex() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#isMappingElements()
	 */
	@Override
	public boolean isMappingElements() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.model.standard.driver.file.index.chunks.ChunkIndex#openChannel()
	 */
	@Override
	public SeekableByteChannel openChannel() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
