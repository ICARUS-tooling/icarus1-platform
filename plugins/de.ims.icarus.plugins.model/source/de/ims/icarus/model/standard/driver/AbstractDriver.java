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
package de.ims.icarus.model.standard.driver;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.driver.Candidates;
import de.ims.icarus.model.api.driver.ChunkStorage;
import de.ims.icarus.model.api.driver.Driver;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.api.driver.indexing.Index;
import de.ims.icarus.model.api.driver.indexing.IndexReader;
import de.ims.icarus.model.api.driver.indexing.IndexStorage;
import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.members.Markable;
import de.ims.icarus.model.api.meta.AnnotationValueDistribution;
import de.ims.icarus.model.api.meta.AnnotationValueSet;
import de.ims.icarus.model.api.seg.Scope;
import de.ims.icarus.model.api.seg.Segment;
import de.ims.icarus.model.standard.driver.cache.MemberCache;
import de.ims.icarus.model.standard.layer.CachedLayer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractDriver implements Driver {

	private final Context context;
	private final DriverManifest manifest;
	private final IndexStorage indices;

	protected AbstractDriver(ContextManifest manifest, Corpus corpus) throws ModelException {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.manifest = manifest.getDriverManifest();

		indices = new IndexStorage();
		context = createContext(manifest, corpus);
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#getContext()
	 */
	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#getManifest()
	 */
	@Override
	public DriverManifest getManifest() {
		return manifest;
	}

	protected Context createContext(ContextManifest manifest, Corpus corpus) throws ModelException {
		return new ContextFactory().createContext(corpus, manifest, this);
	}


	/**
	 * @see de.ims.icarus.model.api.driver.Driver#getIndices()
	 */
	@Override
	public IndexStorage getIndices() {
		return indices;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#getIndex(de.ims.icarus.model.api.layer.MarkableLayer, de.ims.icarus.model.api.layer.MarkableLayer)
	 */
	@Override
	public Index getIndex(MarkableLayer sourceLayer, MarkableLayer targetLayer) {
		return indices.getIndex(sourceLayer, targetLayer);
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#load(de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.seg.Scope, de.ims.icarus.model.api.driver.DriverListener)
	 */
	@Override
	public long load(IndexSet[] indices, Scope scope, ChunkStorage storage)
			throws ModelException, InterruptedException {
		if (indices == null)
			throw new NullPointerException("Invalid indices"); //$NON-NLS-1$
		if (scope == null)
			throw new NullPointerException("Invalid scope"); //$NON-NLS-1$
		if (storage == null)
			throw new NullPointerException("Invalid storage"); //$NON-NLS-1$

		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * This implementation casts the given layer to {@link CachedLayer} and retrieves
	 * its cache to forward the lookup.
	 *
	 * @see de.ims.icarus.model.api.driver.Driver#load(long, de.ims.icarus.model.api.layer.MarkableLayer)
	 */
	@Override
	public Markable load(long index, MarkableLayer layer)
			throws ModelException {
		//TODO check of layer is managed by this driver?
		MemberCache cache = ((CachedLayer)layer).getCache();
		return cache==null ? null : cache.lookupMember(index);
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#mapIndices(de.ims.icarus.model.api.layer.MarkableLayer, de.ims.icarus.model.api.layer.MarkableLayer, de.ims.icarus.model.api.driver.IndexSet[])
	 */
	@Override
	public Candidates mapIndices(MarkableLayer targetLayer,
			MarkableLayer sourceLayer, IndexSet[] indices)
			throws ModelException, InterruptedException {
		Index index = getIndex(sourceLayer, targetLayer);

		if(index==null)
			throw new IllegalStateException("No index available to map from layer " //$NON-NLS-1$
					+sourceLayer.getName()+" to target layer "+targetLayer.getName()); //$NON-NLS-1$

		IndexReader reader = index.newReader();

		IndexSet[] result = null;

		reader.begin();
		try {
			result = reader.lookup(indices);
		} finally {
			reader.end();
			reader.close();
		}

		return Candidates.wrap(result);
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#releaseContainer(de.ims.icarus.model.api.Container, de.ims.icarus.model.api.seg.Segment)
	 */
	@Override
	public void release(Segment segment) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#lookupValues(de.ims.icarus.model.api.layer.AnnotationLayer, java.lang.String)
	 */
	@Override
	public AnnotationValueSet lookupValues(AnnotationLayer layer, String key)
			throws ModelException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#lookupDistribution(de.ims.icarus.model.api.layer.AnnotationLayer, java.lang.String)
	 */
	@Override
	public AnnotationValueDistribution lookupDistribution(
			AnnotationLayer layer, String key) throws ModelException,
			InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
}
