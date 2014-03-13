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

import java.util.List;

import de.ims.icarus.language.model.api.layer.Layer;

/**
 * A {@code LayerManifest} describes a single {@link Layer} in a corpus and
 * defines an optional set of prerequisites that have to be met for the layer
 * to work properly. In addition it defines whether or not a layer can be
 * accessed for searching and if so, whether it can be indexed to speed up a
 * search operation. Note that those two flags are fixed properties of the
 * layer manifest and therefore not modifiable by the user. Not being able to
 * search a layer does however {@code not} imply it can't be used by the user at
 * all. It simply means the possible interactions besides looking at the visualized
 * form are restricted to manual operations like annotating or exploring it without
 * the help of the search engine.
 * <p>
 * Side note on indexing:
 * <br>
 * For an actual index to be constructed for a given layer, itself and <b>all</b> the layers
 * it depends on (even indirectly) have to be indexable. For the simple case of indexing
 * an annotation layer this is trivial, since most annotations will refer to basic
 * markable or structure layers which should be indexable. Therefore the annotation layer
 * makes the choice regarding the option of indexing being available.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LayerManifest extends MemberManifest {

	ContextManifest getContextManifest();

	MarkableLayerManifest getBaseLayerManifest();

	/**
	 * Returns a list of prerequisites describing other layers a corpus
	 * has to host in order for the new layer to be operational. If this
	 * layer does not depend on other layers the returned list is empty.
	 *
	 * @return
	 */
	List<Prerequisite> getPrerequisites();

	/**
	 * Defines if it is possible to build an index for the content of a layer.
	 * This is of course only of importance if the layer in question actually
	 * supports search operations as defined via the {@link #isSearchable()}
	 * method.
	 *
	 * @return
	 */
	boolean isIndexable();

	/**
	 * Returns whether or not search operations on this layer are supported.
	 * For most layers this method will returns {@code true} but there are types
	 * of data for which searching is a non-trivial task and not easily implemented.
	 * For example an annotation layer containing web links to wikipedia articles or
	 * audio recordings of human speakers would most likely decide to not support
	 * searches.
	 *
	 * @return
	 */
	boolean isSearchable();
}
