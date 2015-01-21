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
package de.ims.icarus.model.api.driver.indexing;

import de.ims.icarus.model.api.driver.Driver;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.IndexManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Index {

	/**
	 * Returns the driver that created and manages this index.
	 *
	 * @return
	 */
	Driver getDriver();

	/**
	 * Returns the {@code source} layer for the mapping this index represents.
	 * Note that the index must accept each element in this source layer as a
	 * legal input to methods of its {@link IndexReader} instances!
	 *
	 * @return
	 */
	MarkableLayer getSourceLayer();

	/**
	 * Returns the {@code target} layer for the mapping this index represents.
	 *
	 * @return
	 */
	MarkableLayer getTargetLayer();

	/**
	 * Returns the manifest this index is based upon.
	 *
	 * @return
	 */
	IndexManifest getManifest();

	/**
	 * Creates a new reader instance to access the data in this index.
	 *
	 * @return
	 */
	IndexReader newReader();

	void close();
}
