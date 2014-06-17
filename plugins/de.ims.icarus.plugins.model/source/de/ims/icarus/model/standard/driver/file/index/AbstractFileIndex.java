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
package de.ims.icarus.model.standard.driver.file.index;

import java.io.IOException;
import java.nio.file.Path;

import de.ims.icarus.model.api.driver.Driver;
import de.ims.icarus.model.api.driver.indexing.Index;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.standard.driver.file.ManagedFileResource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractFileIndex extends ManagedFileResource implements Index {

	private Driver driver;
	private IndexManifest manifest;
	private MarkableLayer sourceLayer;
	private MarkableLayer targetLayer;

	protected AbstractFileIndex(Path file, BlockCache cache, int cacheSize) {
		super(file, cache, cacheSize);
	}

	protected static void checkInterrupted() throws InterruptedException {
		if(Thread.interrupted())
			throw new InterruptedException();
	}

	/**
	 * @param driver the driver to set
	 */
	void setDriver(Driver driver) {
		this.driver = driver;
	}

	/**
	 * @param manifest the manifest to set
	 */
	void setManifest(IndexManifest manifest) {
		this.manifest = manifest;
	}

	/**
	 * @param sourceLayer the sourceLayer to set
	 */
	void setSourceLayer(MarkableLayer sourceLayer) {
		this.sourceLayer = sourceLayer;
	}

	/**
	 * @param targetLayer the targetLayer to set
	 */
	void setTargetLayer(MarkableLayer targetLayer) {
		this.targetLayer = targetLayer;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.indexing.Index#getDriver()
	 */
	@Override
	public Driver getDriver() {
		return driver;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.indexing.Index#getSourceLayer()
	 */
	@Override
	public MarkableLayer getSourceLayer() {
		return sourceLayer;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.indexing.Index#getTargetLayer()
	 */
	@Override
	public MarkableLayer getTargetLayer() {
		return targetLayer;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.indexing.Index#getManifest()
	 */
	@Override
	public IndexManifest getManifest() {
		return manifest;
	}

	/**
	 * Returns an accessor for write operations on this index.
	 *
	 * @return
	 */
	public abstract IndexWriter newWriter();

	/**
	 * The default implementation does nothing.
	 *
	 * @see de.ims.icarus.model.api.driver.indexing.Index#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * Allows subclasses to perform compression or other means of
	 * storage optimization. This method should only called once
	 * an index has been completely filled with mapping data!
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void optimize() throws IOException, InterruptedException {
		// for subclasses
	}
}
