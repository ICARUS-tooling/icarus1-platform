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
package de.ims.icarus.language.model.api.driver;

import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.CorpusException;
import de.ims.icarus.language.model.api.layer.AnnotationLayer;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.api.meta.AnnotationValueDistribution;
import de.ims.icarus.language.model.api.meta.AnnotationValueSet;
import de.ims.icarus.language.model.api.seg.Scope;
import de.ims.icarus.language.model.iql.Query;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Driver {

	Context getContext();

	/**
	 * Attempts to contact whatever indexing system the driver internally uses
	 * and asks it to narrow down the number of potential candidates for the given
	 * query. The query is provided in raw form since there is no a priori limitation
	 * on what parts of a query a driver can or cannot use for indexing/filtering.
	 * Potential ids of candidates are to be returned wrapped into an {@code IndexSet}.
	 * If the information in the query is not sufficient to filter candidates, than
	 * this method should return {@code null} instead of collecting all available ids.
	 *
	 * @param query the query to use in order to narrow down potential candidates
	 * @param scope the vertical filter that defines the layer members of the returned
	 * 			{@code IndexSet} refer to.
	 * @return
	 * @throws CorpusException if the driver encountered problems while contacting the index
	 * 			(note that incompatibility between constraints in the query and the capabilities
	 * 			of the driver should lead the latter to ignore said constraints instead of throwing
	 * 			an exception or aborting the call in some other way)
	 * @throws InterruptedException
	 * @throws NullPointerException if either one of the {@code query} or {@code scope} arguments is {@code null}
	 */
	IndexSet lookup(Query query, Scope scope) throws CorpusException, InterruptedException;

	/**
	 * Synchronously attempts to load the given set of indices referencing chunks in the primary layer of the
	 * supplied {@code Scope}. To lookup and register chunks the given {@link ChunkManager} should be used.
	 * The returned value signals the total number of chunks that have been loaded successfully.
	 *
	 * @param indices
	 * @param scope
	 * @param manager
	 * @return
	 * @throws CorpusException
	 * @throws InterruptedException
	 * @throws NullPointerException if any of the arguments is {@code null}
	 *
	 * @see ChunkManager
	 * @see Scope
	 */
	long load(IndexSet indices, Scope scope, ChunkManager manager) throws CorpusException, InterruptedException;

	/**
	 * Attempts to fetch the number of elements stored in the top-level container for the given
	 * layer. The returned value is meant to be the total number of markables in that container,
	 * unaffected by horizontal filtering. Driver implementations should cache these counts for all
	 * layers they are meant to manage.
	 *
	 * @param layer
	 * @return
	 * @throws CorpusException
	 */
	long getMemberCount(MarkableLayer layer) throws CorpusException;

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
	 * @throws CorpusException
	 * @throws InterruptedException
	 */
	AnnotationValueSet lookupValues(AnnotationLayer layer, String key) throws CorpusException, InterruptedException;

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
	 * @throws CorpusException
	 * @throws InterruptedException
	 */
	AnnotationValueDistribution lookupDistribution(AnnotationLayer layer, String key) throws CorpusException, InterruptedException;
}
