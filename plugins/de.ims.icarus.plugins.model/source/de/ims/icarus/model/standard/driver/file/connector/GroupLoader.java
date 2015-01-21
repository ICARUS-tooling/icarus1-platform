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
package de.ims.icarus.model.standard.driver.file.connector;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.driver.ChunkStorage;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.members.Item;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class GroupLoader {

	protected final LayerGroup group;
	protected final FileConnector connector;

	protected GroupLoader(LayerGroup group, FileConnector connector) {
		if (group == null)
			throw new NullPointerException("Invalid group"); //$NON-NLS-1$
		if (connector == null)
			throw new NullPointerException("Invalid connector"); //$NON-NLS-1$

		this.group = group;
		this.connector = connector;
	}

	/**
	 * Loads the current available content of the given {@code channel} and converts it
	 * into an appropriate markable implementation that will be returned. If the group
	 * this loader is responsible for contains <i>sub</i>-groups, the members of those
	 * groups must be loaded, too (and in addition the supplied {@link ChunkStorage} is
	 * to be used in order to signal the loading of those hosted members).
	 *
	 * @param index
	 * @param channel
	 * @param storage
	 * @return
	 * @throws IOException
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	public abstract Item load(long index, SeekableByteChannel channel, ChunkStorage storage) throws IOException, ModelException, InterruptedException;

	/**
	 * @return the group
	 */
	public LayerGroup getGroup() {
		return group;
	}

	/**
	 * @return the connector
	 */
	public FileConnector getConnector() {
		return connector;
	}
}
