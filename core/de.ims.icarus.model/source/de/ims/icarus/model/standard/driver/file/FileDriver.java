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
package de.ims.icarus.model.standard.driver.file;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.driver.Candidates;
import de.ims.icarus.model.api.driver.ChunkStorage;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.iql.Query;
import de.ims.icarus.model.standard.driver.AbstractDriver;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FileDriver extends AbstractDriver {

	//TODO when generating a new driver, make the framework assign its manifest a random UID string for the name suffix of the folder the driver uses for metadata and/or indices

	/**
	 * @param manifest
	 * @param corpus
	 * @throws ModelException
	 */
	protected FileDriver(ContextManifest manifest, Corpus corpus)
			throws ModelException {
		super(manifest, corpus);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#lookup(de.ims.icarus.model.iql.Query, de.ims.icarus.model.api.layer.MarkableLayer)
	 */
	@Override
	public Candidates lookup(Query query, MarkableLayer layer)
			throws ModelException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#load(de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.layer.MarkableLayer, de.ims.icarus.model.api.driver.ChunkStorage)
	 */
	@Override
	public long load(IndexSet[] indices, MarkableLayer layer,
			ChunkStorage storage) throws ModelException, InterruptedException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#getMemberCount(de.ims.icarus.model.api.layer.MarkableLayer)
	 */
	@Override
	public long getMemberCount(MarkableLayer layer) throws ModelException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.Driver#close()
	 */
	@Override
	public void close() throws ModelException {
		// TODO Auto-generated method stub

	}

}
