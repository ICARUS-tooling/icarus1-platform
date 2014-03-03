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
package de.ims.icarus.language.model.api;

import java.util.List;

import de.ims.icarus.language.model.api.highlight.Highlight;
import de.ims.icarus.language.model.api.highlight.HighlightCursor;
import de.ims.icarus.language.model.api.manifest.HighlightLayerManifest;
import de.ims.icarus.language.model.api.manifest.ManifestOwner;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface HighlightLayer extends Layer, ManifestOwner<HighlightLayerManifest> {

	/**
	 * Returns the {@code Layer}s that highlightings within this layer refer to.
	 * Note that the order of {@code Layer} instances in the returned list corresponds
	 * to the indices used in the {@link Highlight} interface.
	 * @see Highlight
	 */
	List<Layer> getHighlightedLayers();

	/**
	 * Returns a {@code HighlightCursor} that can be used to navigate over
	 * top-level highlights on the underlying {@code MarkableLayer}. If there are no
	 * top-level highlights available, this method should return {@code null}.
	 * <p>
	 * Note that this method is equivalent to calling {@link #getHighlightCursor(Markable)}
	 * with the root container of this layers <i>base-layer</i>.
	 *
	 * @return
	 */
	HighlightCursor getHighlightCursor();

	/**
	 * Returns a {@code HighlightCursor} that can be used to navigate over
	 * highlights of the referenced layers top-level members (top-level members
	 * are the markables in that layers root container). If the container in
	 * question is not highlighted at all, this method returns {@code null}.
	 * Note that this method is intended for fetching highlights on nested containers
	 * and therefore will only be available if the <i>base-layer</i> is indeed
	 * built as a hierarchy of containers. If provided with the <i>base-layers</i>
	 * root container this method is essentially equal to calling {@link #getHighlightCursor()}.
	 *
	 * @param container The {@code Markable} to fetch highlight information about
	 *
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code Markable} is not a member of this
	 * layers <i>base-layer</i> as defined by {@link #getBaseLayer()} or if it is not a
	 * {@code Container}
	 */
	HighlightCursor getHighlightCursor(Markable markable);
}
