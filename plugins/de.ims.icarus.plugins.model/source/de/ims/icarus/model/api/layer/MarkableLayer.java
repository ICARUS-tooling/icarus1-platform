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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.model.api.layer;

import de.ims.icarus.model.api.manifest.ManifestOwner;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;

/**
 * A {@code MarkableLayer} defines a collection of markables. If it is
 * the <i>base layer</i> of a corpus it describes the basic collection
 * of available markables for that corpus. In any other case it serves
 * as a sort of aggregated view, grouping markables of the underlying
 * layers in its container.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface MarkableLayer extends Layer, ManifestOwner<MarkableLayerManifest> {

	/**
	 * Returns the shared {@code MarkableLayerManifest} that holds
	 * information about markable composition and possible structures
	 * in this layer.
	 *
	 * @return The manifest that describes this collection of markables
	 */
	@AccessRestriction(AccessMode.ALL)
	@Override
	MarkableLayerManifest getManifest();

//	/**
//	 * Returns the container holding all the {@code Markable} objects this
//	 * layer defines.
//	 * @return The root container of this layer
//	 */
//	Container getContainer();

//	/**
//	 * Returns the number of members in this layer. This is the total number
//	 * of members as present in the corpus data. Legal indices used for member
//	 * lookups are {@code 0} to <tt>getMemberCount()-1</tt>. A return value of
//	 * {@code -1} indicates that the layer has not yet obtained cached information
//	 * about the number of elements it hosts.
//	 * @return
//	 */
//	@AccessRestriction(AccessMode.ALL)
//	long getMemberCount();
//
//	/**
//	 * Returns the number of members currently present in the layer's cache.
//	 * @return
//	 */
//	@AccessRestriction(AccessMode.ALL)
//	int getCachedMemberCount();

//	/**
//	 * Returns the member cached for the specified {@code index} or {@code null} if
//	 * no member is cached for that index.
//	 * @param index
//	 * @return
//	 */
//	Markable getCachedMember(long index);

//	/**
//	 *
//	 * @param index
//	 * @return
//	 */
//	int getReferenceCount(long index);
//
//	/**
//	 * Registers the given member, incrementing its reference count if already present in
//	 * the cache or creating a new cache entry otherwise.
//	 *
//	 * @param member
//	 * @return {@code true} iff the given {@code member} has not been present in the cache
//	 * 			and a new entry was created
//	 */
//	boolean registerMember(Markable member);
//
//	/**
//	 * Decrements the reference count for the given {@code member}, removing it from the
//	 * cache if the reference count reaches zero.
//	 * @param member
//	 * @return {@code true} iff unregistering the given {@code member} caused the reference
//	 * 			count to reach zero and the associated cache entry has been removed
//	 */
//	boolean unregisterMember(Markable member);

	/**
	 * Returns the {@code MarkableLayer} that holds the bounding
	 * containers the elements in this layer correspond to. For
	 * example if a structural layer represents syntax trees for
	 * another layer that holds word tokens then this layer would
	 * be referenced via {@link Layer#getBaseLayer()} and the
	 * layer representing sentences would be accessed by
	 * this method. Note that for containers that do not correspond
	 * to the groups defined by other {@code MarkableLayer}s this
	 * method is allowed to return {@code null}. A {@code non-null}
	 * return value is a hint for visualization facilities on
	 * how to link certain layers.
	 * <p>
	 * The main difference between the containers (C) of this layer and
	 * the ones returned by this method (B) are as follows:
	 * <ul>
	 * <li>Containers C do not have to hold all the elements in their
	 * <i>boundary container</i> B</li>
	 * <li>Containers C can define <i>virtual</i> markables outside of
	 * the ones provided by B</li>
	 * <li>Containers B therefore define the <i>base</i> collection
	 * of markables that is available for containers C to build upon</li>
	 * <li>For each C there has to be exactly one matching B</li>
	 * <li>Not every B is required to have a container C referencing it!</li>
	 * </ul>
	 * If the markables in this layer are mere containers then the members
	 * of the boundary layer define borders that those containers are not allowed
	 * to span across.
	 * <p>
	 * This is an optional method.
	 *
	 * @return the {@code MarkableLayer} holding boundary containers for
	 * the structures in this layer or {@code null} if the structures this
	 * layer defines are not mapped to existing layer boundaries.
	 */
	@AccessRestriction(AccessMode.ALL)
	MarkableLayer getBoundaryLayer();
}
