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
package de.ims.icarus.language.model.api.manifest;

import de.ims.icarus.language.model.api.manifest.LayerManifest.TargetLayerManifest;

/**
 * Models the description of an indexable mapping in the form of either a
 * direct dependency or a bounding relation. The index maps from elements in
 * the source layer to elements in the target layer.
 *
 * Depending on the layer types of both the source and target layer, very specific
 * data structures are needed to model the actual index.
 *
 * TODO mention fragment layers
 *
 * Note that the indices described by this manifest are only about directly related
 * markable layers, not the contents of annotation layers!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface IndexManifest {

	ContextManifest getContextManifest();

	TargetLayerManifest getSourceLayerManifest();

	TargetLayerManifest getTargetLayerManifest();

	Relation getRelation();

	boolean includeReverse();

	/**
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public enum Relation {

		/**
		 * Elements from the source and target layer are mapped by identical index values.
		 * This means there is no real lookup structure required and the element at index <i>i</i>
		 * in the source layer is mapped to the element at the same index <i>i</i> in the target
		 * layer (and therefore the same holds true for the inverse index).
		 */
		ONE_TO_ONE,

		/**
		 * A single element in the source layer may hold an arbitrary number of elements from
		 * the target layer. Typical examples are all kinds of aggregating markable layers that
		 * feature containers as top level elements. Possible lookup structures include span lists
		 * (begin- and end-index for each source element) for source layers that host span elements
		 * and complete content lists (a list of exact target indices) for non-continuous source
		 * layer members. While span lists are fairly easy to map to memory chunks or arrays, content
		 * lists pose some serious drawbacks, potentially requiring an additional layer of indices to
		 * map source elements to their respective sublist in a data block.
		 */
		ONE_TO_MANY,

		/**
		 * An arbitrary number of (not necessarily continuous) elements in the source layer map to
		 * a common member of the target layer.
		 *
		 * If the target elements are spans, than an efficient
		 * lookup can be created by dividing the source layer into chunks of markables and then save
		 * for each such chunk the first and last element in the target layer that is truly contained
		 * in this chunk (with respect to its begin- and end-offset). To lookup a target the algorithm
		 * then first determines the correct chunk according to the source elements index and then
		 * performs a binary search on the spans in that chunk to find the target element.
		 * Performance can be controlled by adjusting chunk size to a value that provides a good tradeoff
		 * between required memory space to store the index information and the speed incurred by the
		 * binary search (which serves as a constant cost factor in the performance formula).
		 *
		 * In the case of non-continuous elements in the target layer (e.g. clusters of source markables)
		 * the above technique fails and it might be required to store a dedicated target index value for
		 * each source element.
		 */
		MANY_TO_ONE,

		/**
		 * As the most complex relation version, this one maps an arbitrary number of source elements to
		 * an again arbitrary number of target elements/containers. As an example imagine entities in the
		 * source layer being grouped into category containers in the target layer, allowing each entity to
		 * be assigned many different categories at once.
		 *
		 * Depending on the container type of the target elements, this version gets easy or very expensive.
		 *
		 * If the target elements are spans, than it is possible to use the strategy proposed for the
		 * {@link #MANY_TO_ONE} relation with a slight addition: When the first target container is found
		 * using binary search within the chunk, then neighbors to both sides are added to the result collection,
		 * until containers are encountered for both ends, that do not contain the source element. The complexity
		 * in this case is limited by the maximum "nesting depth" of spans in the target layer, which remains
		 * to be evaluated as a proper upper bound. Looking in the neighborhood of the first successful match
		 * is possible due to the sorted nature of top-level layer elements and the sorting rules for spans
		 * (span locality).
		 *
		 * For non-continuous target elements the rules for the {@link #ONE_TO_MANY} relation apply.
		 */
		MANY_TO_MANY;

		/**
		 * Returns the inverse version of the current relation. This method is used to obtain the required
		 * information when an {@code IndexManifest}'s {@link IndexManifest#includeReverse()} method returns
		 * {@code true} and indicates the necessity of an index for the inverse relation.
		 * <p>
		 * Note that {@link #ONE_TO_ONE} and {@link #MANY_TO_MANY} relations do not change when inverted!
		 * @return
		 */
		public Relation invert() {
			switch (this) {
			case MANY_TO_ONE:
				return ONE_TO_MANY;
			case ONE_TO_MANY:
				return MANY_TO_ONE;

			default:
				return this;
			}
		}
	}
}
