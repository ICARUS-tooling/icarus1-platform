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
package de.ims.icarus.model.api.manifest;

import java.util.List;

import de.ims.icarus.model.io.LocationType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DriverManifest extends MemberManifest {

	/**
	 * Returns the manifest that specifies the actual driver implementation.
	 * @return
	 */
	ImplementationManifest getImplementationManifest();

	/**
	 * Returns manifests describing all the indices that should be created for this
	 * context.
	 * @return
	 */
	List<IndexManifest> getIndexManifests();

	/**
	 * Returns a hint on which type of resources the driver is depending to access
	 * corpus data.
	 *
	 * @return
	 */
	LocationType getLocationType();
}
