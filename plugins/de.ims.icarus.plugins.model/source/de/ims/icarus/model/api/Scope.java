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
package de.ims.icarus.model.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * Models a compact description of a subset of contexts and layers
 * used for a specific purpose. Each {@code scope} is defined by its
 * <i>primary layer</i> as returned by {@link #getPrimaryLayer()}. This
 * layer is used as the measure of granularity (Note that this layer
 * is bound to be a {@code MarkableLayer} or {@code StructureLayer}).
 * For every element in this layer that was declared loaded by the host
 * corpus, it is guaranteed that all required data of underlying layers that are
 * within this scope (as can be checked by {@link #containsLayer(Layer)}
 * will also be available.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Scope {

	private final Corpus corpus;
	private final List<Context> contexts;
	private Set<Context> contextsLut;
	private final List<Layer> layers;
	private Set<Layer> layersLut;
	private final MarkableLayer primaryLayer;

	public Scope(Corpus corpus, List<Context> contexts,
			MarkableLayer primaryLayer, List<Layer> layers) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$
		if (contexts == null)
			throw new NullPointerException("Invalid contexts"); //$NON-NLS-1$
		if (primaryLayer == null)
			throw new NullPointerException("Invalid primaryLayer"); //$NON-NLS-1$
		if (layers == null)
			throw new NullPointerException("Invalid layers"); //$NON-NLS-1$

		if(contexts.isEmpty())
			throw new IllegalArgumentException("List of contexts is empty"); //$NON-NLS-1$

		// Note that layers list is allowed to be empty!

		this.corpus = corpus;
		this.contexts = new ArrayList<>(contexts);
		this.primaryLayer = primaryLayer;
		this.layers = new ArrayList<>(layers);
	}

	public Corpus getCorpus() {
		return corpus;
	}

	public List<Context> getContexts() {
		return CollectionUtils.getListProxy(contexts);
	}

	public boolean containsContext(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(contextsLut==null) {
			contextsLut = new HashSet<>(contexts);
		}

		return contextsLut.contains(context);
	}

	/**
	 * Returns the layer that defines the granularity of this scope.
	 * The members of that layer are intended to represent atomic units
	 * when loading and/or caching is performed for this scope. Atomicity
	 * in the context of a scope means that once a member of that layer is
	 * loaded, the corpus guarantees that all underlying data referenced by
	 * that member will be fully available, too. It is therefore vital to
	 * follow the simple rule when defining a layer as primary:
	 * <p>
	 * <i>As fine-grained as possible, as coarse-grained as necessary!</i>
	 */
	public MarkableLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * Returns the list of additional layers available through this scope.
	 * This can be an arbitrary subset of the combined collection of layers
	 * hosted by all the {@code Context} instances as returned by {@link #getContexts()}.
	 * <p>
	 * Note however, that there are some things to keep in mind:
	 * <ul>
	 * <li><b>{@link AnnotationLayer}</b>s are only allowed in a scope if their respective
	 * base-layer is also a part of that scope.</li>
	 * <li><b>{@link MarkableLayer}</b>s below the primary layer will be guaranteed to get
	 * their content loaded in blocks that are covered by the members in the primary layer</li>
	 * <li><b>{@link MarkableLayer}</b>s above the primary layer will only be loaded partially unlike
	 * specifically requested</li>
	 * <li>The behavior of <b>{@link MarkableLayer}</b>s that are not linked to the primary
	 * layer in any way is not specified.</li>
	 * </ul>
	 */
	public List<Layer> getSecondaryLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	/**
	 * Returns {@code true} if the given layer is either the
	 * primary layer of this scope or contained in the list
	 * of secondary layers.
	 *
	 * @throws NullPointerException if the {@code layer} argument
	 * is {@code null}.
	 */
	public boolean containsLayer(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		if(layersLut==null) {
			layersLut = new HashSet<>(layers);
		}

		return layersLut.contains(layers);
	}
}
