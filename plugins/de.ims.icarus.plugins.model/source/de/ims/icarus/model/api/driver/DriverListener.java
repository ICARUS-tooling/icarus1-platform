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
package de.ims.icarus.model.api.driver;

import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.Markable;

/**
 * Allows intercepting loaded or skipped chunks during load operations of a driver implementation.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DriverListener {

//	Corpus getCorpus();

	/**
	 * Callback to signal the successful loading of a new data chunk
	 * (typically a {@link Container} implementation) by a driver.
	 * Note that this method will only be called for top-level members!
	 *
	 *
	 * @param id
	 * @param markable
	 */
	void chunkLoaded(long index, LayerGroup layerGroup, Markable markable);

	/**
	 * Signals that a certain data chunk could not be loaded. The reason is typically
	 * one of the following:
	 * <ol>
	 * <li>The data chunk has already been loaded before and the driver only added data
	 * to additional layers</li>
	 * <li>The driver detected an inconsistency and considers the data chunk for the given
	 * index invalid. This only happens when there is a markable object already existing for this
	 * index. As a result to this method call the {@code DriverListener} should discard all stored data
	 * for the specified chunk and later attempt to load it again.</li>
	 * </ol>
	 * The latter case is signaled with the {@code corrupted} argument being {@code true}.
	 * Note that the {@code Markable} associated with the given index will still be available
	 * through the layer's cache as long as the {@code corrupted} argument is {@code false}.
	 * <p>
	 * Note that this method will only be called for top-level members!
	 *
	 * @param index
	 * @param corrupted
	 */
	void chunkSkipped(long index, LayerGroup layerGroup, ChunkStatus status);

//	/**
//	 * Lookup an existing chunk in the specified layer. If no markable could be found in the data
//	 * storage for the given {@code index} then this method should return {@code null}.
//	 * <p>
//	 * Note that there is no dedicated method for the lookup of {@link Edge}s, since it is not possible
//	 * to partially load the edges of a {@link Structure}. Unlike regular {@link Container}s they would lose an
//	 * important aspect of their content in discarding edges.
//	 *
//	 * @param layer
//	 * @param index the global index of the {@code Markable} to be fetched, unaffected by horizontal
//	 * filtering and without index translation.
//	 * @return
//	 */
//	Markable getChunk(MarkableLayer layer, long index);
}