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
package de.ims.icarus.model.standard.elements;

import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.standard.elements.dummy.DummyContainer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class VirtualRootContainer extends DummyContainer {

	private final MarkableLayer layer;

	public VirtualRootContainer(MarkableLayer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		this.layer = layer;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.dummy.DummyContainer#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return layer.getManifest().getRootContainerManifest();
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.dummy.DummyMarkable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return layer;
	}
}
