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
package de.ims.icarus.language.model.standard.driver.file;

import de.ims.icarus.language.model.standard.driver.file.ManagedFileResource.Block;
import de.ims.icarus.language.model.standard.driver.file.ManagedFileResource.BlockCache;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RUBlockCache implements BlockCache {

	private Entry[] table;

	private static class Entry {

		// Block id used as hash key
		int key;

		// Data block
		Block block;

		// Link to the next entry in the hash table
		Entry next;

		// Links for the linked list
		Entry _next, _previous;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.driver.file.ManagedFileResource.BlockCache#getBlock(int)
	 */
	@Override
	public Block getBlock(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.driver.file.ManagedFileResource.BlockCache#addBlock(de.ims.icarus.language.model.standard.driver.file.ManagedFileResource.Block, int)
	 */
	@Override
	public Block addBlock(Block block, int id) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.driver.file.ManagedFileResource.BlockCache#open(int)
	 */
	@Override
	public void open(int capacity) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.standard.driver.file.ManagedFileResource.BlockCache#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
}
