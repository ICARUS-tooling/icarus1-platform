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

import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.FragmentLayer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.raster.Rasterizer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultFragmentLayer extends DefaultMarkableLayer implements FragmentLayer {

	private Rasterizer rasterizer;
	private AnnotationLayer valueLayer;

	/**
	 * @param manifest
	 * @param group
	 */
	public DefaultFragmentLayer(FragmentLayerManifest manifest, LayerGroup group) {
		super(manifest, group);
	}

	/**
	 * @see de.ims.icarus.model.api.layer.FragmentLayer#getValueLayer()
	 */
	@Override
	public AnnotationLayer getValueLayer() {
		return valueLayer;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.FragmentLayer#getRasterizer()
	 */
	@Override
	public Rasterizer getRasterizer() {
		return rasterizer;
	}

	/**
	 * @param rasterizer the rasterizer to set
	 */
	public void setRasterizer(Rasterizer rasterizer) {
		if (rasterizer == null)
			throw new NullPointerException("Invalid rasterizer"); //$NON-NLS-1$

		this.rasterizer = rasterizer;
	}

	/**
	 * @param valueLayer the valueLayer to set
	 */
	public void setValueLayer(AnnotationLayer valueLayer) {
		if (valueLayer == null)
			throw new NullPointerException("Invalid valueLayer"); //$NON-NLS-1$

		this.valueLayer = valueLayer;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.FragmentLayer#getRasterSize(de.ims.icarus.model.api.members.Item, int)
	 */
	@Override
	public long getRasterSize(Item item, int axis) {
		String key = getManifest().getAnnotationKey();
		Object value = valueLayer.getValue(item, key);
		return rasterizer.getRasterSize(item, this, value, axis);
	}

	/**
	 * @see de.ims.icarus.model.standard.layer.AbstractLayer#getManifest()
	 */
	@Override
	public FragmentLayerManifest getManifest() {
		return (FragmentLayerManifest) super.getManifest();
	}
}