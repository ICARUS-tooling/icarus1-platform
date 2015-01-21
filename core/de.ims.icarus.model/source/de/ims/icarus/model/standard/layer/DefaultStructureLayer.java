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
package de.ims.icarus.model.standard.layer;

import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.layer.StructureLayer;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultStructureLayer extends DefaultMarkableLayer implements StructureLayer {

	/**
	 *
	 * @param manifest
	 * @param group
	 */
	public DefaultStructureLayer(StructureLayerManifest manifest, LayerGroup group) {
		super(manifest, group);
	}

	/**
	 * @see de.ims.icarus.model.api.standard.layer.AbstractLayer#getManifest()
	 */
	@Override
	public StructureLayerManifest getManifest() {
		return (StructureLayerManifest) super.getManifest();
	}
}
