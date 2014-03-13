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
package de.ims.icarus.language.model.api.layer;

import de.ims.icarus.language.model.api.manifest.StructureLayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface StructureLayer extends MarkableLayer {

	/**
	 * Returns the shared {@code StructureLayerManifest} that holds
	 * information about markable composition and possible structures
	 * in this layer.
	 *
	 * @return The manifest that describes this collection of markables
	 */
	@Override
	StructureLayerManifest getManifest();

	int getMixinLayerCount();

	MarkableLayer getMixinLayerAt(int index);

	/**
	 * Translates an index currently used to access edges in this layer's
	 * root container into a value that can be used to reference elements in
	 * the entirety of the backing data.
	 * The advise from the {@link #translateMarkableIndex(int)} applies
	 * here, too.
	 *
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException if the given index is negative or
	 * 			exceeds the values allowed by the root structure
	 */
	long translateEdgeIndex(int index);
}
