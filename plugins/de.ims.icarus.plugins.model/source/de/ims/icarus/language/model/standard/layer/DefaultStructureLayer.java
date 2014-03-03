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
package de.ims.icarus.language.model.standard.layer;

import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.StructureLayer;
import de.ims.icarus.language.model.api.manifest.StructureLayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultStructureLayer extends DefaultMarkableLayer implements StructureLayer {

	/**
	 * @param id
	 * @param context
	 * @param manifest
	 */
	public DefaultStructureLayer(long id, Context context,
			StructureLayerManifest manifest) {
		super(id, context, manifest);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.layer.AbstractLayer#getManifest()
	 */
	@Override
	public StructureLayerManifest getManifest() {
		return (StructureLayerManifest) super.getManifest();
	}
}
