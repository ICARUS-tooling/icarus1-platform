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

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.driver.indexing.Index;
import de.ims.icarus.model.api.driver.indexing.IndexStorage;
import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.meta.AnnotationValueDistribution;
import de.ims.icarus.model.api.meta.AnnotationValueSet;
import de.ims.icarus.model.api.seg.Scope;
import de.ims.icarus.model.api.seg.Segment;
import de.ims.icarus.model.iql.Query;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Driver {

//	void createContext(ContextManifest manifest) throws ModelException;

	Context getContext();

	DriverManifest getManifest();

	/**
	 * Returns all the indices available for the context this driver manages.
	 * @return
	 */
	IndexStorage getIndices();

	/**
	 *
	 * @param sourceLayer
	 * @param targetLayer
	 * @return
	 */
	Index getIndex(MarkableLayer sourceLayer, MarkableLayer targetLayer);

	/**
	 * Attempts to contact whatever <i>content</i> indexing system the driver internally uses
	 * and asks it to narrow down the number of potential candidates for the given
	 * query. The query is provided in raw form since there is no a priori limitation
	 * on what parts of a query a driver can or cannot use for indexing/filtering.
	 * Potential ids of candidates are to be returned wrapped into an {@code IndexSet} array.
	 * If the information in the query is not sufficient to filter candidates, than
	 * this method should return {@code null} instead of collecting all available ids.
	 *
	 * @param query the query to use in order to narrow down potential candidates
	 * @param layer the primary layer of the vertical filter that index values of the returned
	 * 			{@code IndexSet} array refer to.
	 * @return A collection of candidates or {@code null} if the data in the query was insufficient
	 * 			for any sensible filtering.
	 * @throws ModelException if the driver encountered problems while contacting the index
	 * 			(note that incompatibility between constraints in the query and the capabilities
	 * 			of the driver should lead the latter to ignore said constraints instead of throwing
	 * 			an exception or aborting the call in some other way)
	 * @throws InterruptedException
	 * @throws NullPointerException if either one of the {@code query} or {@code scope} arguments is {@code null}
	 */
	Candidates lookup(Query query, MarkableLayer layer) throws ModelException, InterruptedException;

	/**
	 * Synchronously attempts to load the given set of indices referencing chunks in the given layer.
	 * The driver is responsible for translating the indices into indices
	 * of the respective layer group and load chunks of that group. Note that this method must automatically
	 * increment the reference counters of each of the affected primary layer members by exactly {@code 1}, no matter
	 * how many lower members are referenced via the original indices. For every resolved markable covered by the
	 * given indices the supplied {@code storage} implementation is used.
	 * <p>
	 * Note furthermore that in case a context contains data that relies on foreign data hosted in another context,
	 * the driver is responsible for performing the required index lookups and initiating
	 *
	 * @param indices
	 * @param layer
	 * @param storage
	 * @return
	 * @throws IllegalArgumentException if the
	 * @throws ModelException
	 * @throws InterruptedException
	 * @throws NullPointerException if any of the arguments is {@code null}
	 *
	 * @see DriverListener
	 * @see Scope
	 */
	long load(IndexSet[] indices, MarkableLayer layer, ChunkStorage storage) throws ModelException, InterruptedException;

	/**
	 * Attempts to fetch the number of elements stored in the top-level container for the given
	 * layer. The returned value is meant to be the total number of markables in that layer,
	 * unaffected by horizontal filtering. Driver implementations should cache these counts for all
	 * layers they are meant to manage. A return value of {@code -1} indicates that the driver has
	 * no information about the specified layer's member count.
	 *
	 * @param layer
	 * @return
	 * @throws ModelException
	 */
	long getMemberCount(MarkableLayer layer) throws ModelException;

	/**
	 * Accesses the internal cache for the specified layer and attempts to lookup the
	 * markable mapped to the given index value. If no markable is stored for that index
	 * this method returns {@code null}.
	 *
	 * @param index
	 * @param layer
	 * @return
	 * @throws ModelException
	 */
	Markable load(long index, MarkableLayer layer) throws ModelException;

	/**
	 * Performs a reverse lookup to return indices of markables in the designated target layer
	 * that contain the specified elements in the given {@code source} layer.
	 *
	 * @param targetLayer
	 * @param sourceLayer
	 * @param indices
	 * @return
	 * @throws NullPointerException if any of the arguments is {@code null}
	 * @throws IllegalArgumentException if {@code targetLayer} is neither directly nor indirectly
	 * 			depending on {@code sourceLayer} or if the {@code sourceLayer} is not a member of
	 * 			the context this driver manages.
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	Candidates getHostIndices(MarkableLayer targetLayer, MarkableLayer sourceLayer, IndexSet[] indices) throws ModelException, InterruptedException;

	/**
	 * Called by a {@link Segment} when it gets closed or it otherwise decided to discard its current
	 * content. The driver is responsible for collecting the layer groups affected by the segment which
	 * it is able handle and then release their content from its internal cache, potentially moving
	 * members it no longer requires to a markable pool.
	 *
	 * @param container
	 * @param segment
	 */
	void release(Segment segment) throws ModelException, InterruptedException;

	/**
	 * Called when a context is removed from a corpus or the entire model framework is shutting down.
	 * The driver implementation is meant to release all previously held resources and to disconnect
	 * from databases or other remote storages.
	 * <p>
	 * Note that the behavior of a driver is undefined once it has been closed! References to closed
	 * driver instances should be discarded immediately.
	 */
	void close() throws ModelException;
//
//	/**
//	 * Returns the cache instance that is used to store loaded markables for the
//	 * specified layer.
//	 *
//	 * @param layer
//	 * @return
//	 * @throws NullPointerException if the {@code layer} argument is {@code null}
//	 */
//	MemberCache<Markable> getCache(MarkableLayer layer);

	/**
	 * Accesses the driver's internal indexing system and tries to fetch all the occurring values for a given
	 * annotation layer. If the optional {@code key} argument is non-null it defines the <i>sub-level</i> to
	 * get annotation values for.
	 *
	 * @param layer
	 * @param key the annotation key to be used in order to narrow down the amount of annotations
	 * 			to be considered or {@code null} if the layer's default annotation should be used.
	 * @return
	 * @throws NullPointerException if the {@code layer} argument is {@code null}
	 * @throws IllegalArgumentException if the specified {@code key} does not represent a legal entry in the
	 * 			given {@code AnnotationLayer}
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	AnnotationValueSet lookupValues(AnnotationLayer layer, String key) throws ModelException, InterruptedException;

	/**
	 * Performs a lookup very similar to {@link #lookupValues(AnnotationLayer, String)} but in addition returns
	 * for each encountered value the total count of occurrences.
	 *
	 * @param layer
	 * @param key the annotation key to be used in order to narrow down the amount of annotations
	 * 			to be considered or {@code null} if the layer's default annotation should be used.
	 * @return
	 * @throws NullPointerException if the {@code layer} argument is {@code null}
	 * @throws IllegalArgumentException if the specified {@code key} does not represent a legal entry in the
	 * 			given {@code AnnotationLayer}
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	AnnotationValueDistribution lookupDistribution(AnnotationLayer layer, String key) throws ModelException, InterruptedException;
}
