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
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ItemLayerManifest;
import de.ims.icarus.model.standard.driver.cache.MemberCache;
import de.ims.icarus.model.standard.driver.cache.MemberPool;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultMarkableLayer extends AbstractLayer<ItemLayerManifest> implements MarkableLayer, CachedLayer {

	private MarkableLayer boundaryLayer;
	private MemberCache cache;
	private MemberPool pool;

	/**
	 * @param context
	 * @param manifest
	 */
	public DefaultMarkableLayer(ItemLayerManifest manifest, LayerGroup group) {
		super(manifest, group);
	}

	/**
	 * @param boundaryLayer the boundaryLayer to set
	 */
	public void setBoundaryLayer(MarkableLayer boundaryLayer) {
		if (boundaryLayer == null)
			throw new NullPointerException("Invalid boundaryLayer"); //$NON-NLS-1$

		this.boundaryLayer = boundaryLayer;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.StructureLayer#getBoundaryLayer()
	 */
	@Override
	public MarkableLayer getBoundaryLayer() {
		return boundaryLayer;
	}

	/**
	 * @see de.ims.icarus.model.standard.layer.CachedLayer#getCache()
	 */
	@Override
	public MemberCache getCache() {
		return cache;
	}

	/**
	 * @see de.ims.icarus.model.standard.layer.CachedLayer#getPool()
	 */
	@Override
	public MemberPool getPool() {
		return pool;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(MemberCache cache) {
		if (cache == null)
			throw new NullPointerException("Invalid cache");  //$NON-NLS-1$

		this.cache = cache;
	}

	/**
	 * @param pool the pool to set
	 */
	public void setPool(MemberPool pool) {
		if (pool == null)
			throw new NullPointerException("Invalid pool");  //$NON-NLS-1$

		this.pool = pool;
	}
}
