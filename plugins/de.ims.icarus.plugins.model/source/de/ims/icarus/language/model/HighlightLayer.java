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
package de.ims.icarus.language.model;

import java.util.List;

import de.ims.icarus.language.model.highlight.HighlightIterator;
import de.ims.icarus.language.model.manifest.HighlightLayerManifest;
import de.ims.icarus.language.model.manifest.ManifestOwner;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface HighlightLayer extends Layer, ManifestOwner<HighlightLayerManifest> {
	
	List<Layer> getHighlightedLayers();

	/**
	 * Returns a {@code HighlightIterator} that can be used to navigate over
	 * top-level highlights on the underlying {@code MarkableLayer}
	 * 
	 * @return
	 */
	HighlightIterator getHighlighIterator();
	
	/**
	 * Returns a {@code HighlightIterator} that can be used to navigate over
	 * highlights of the referenced layers top-level members (top-level members
	 * are the markables in that layers root container).
	 * 
	 * @param markable
	 * @return
	 */
	HighlightIterator getHighlightIterator(Markable markable);
}
