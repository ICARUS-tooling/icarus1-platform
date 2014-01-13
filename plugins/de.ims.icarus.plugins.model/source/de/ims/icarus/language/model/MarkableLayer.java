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
package de.ims.icarus.language.model;

import de.ims.icarus.language.model.manifest.ManifestOwner;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;

/**
 * A {@code MarkableLayer} defines a collection of markables. If it is
 * the <i>base layer</> of a corpus it describes the basic collection
 * of available markables for that corpus. In any other case it serves
 * as a sort of aggregated view, grouping markables of the underlying
 * layers in its container.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface MarkableLayer extends Layer, ManifestOwner<MarkableLayerManifest> {

	/**
	 * Returns the shared {@code MarkableLayerManifest} that holds
	 * information about markable composition and possible structures
	 * in this layer.
	 *
	 * @return The manifest that describes this collection of markables
	 */
	@Override
	MarkableLayerManifest getManifest();

	/**
	 * Returns the container holding all the {@code Markable} objects this
	 * layer defines.
	 * @return The root container of this layer
	 */
	Container getContainer();

//	IdDomain getIdDomain();
}
