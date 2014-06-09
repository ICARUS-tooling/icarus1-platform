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
package de.ims.icarus.language.model.api.manifest;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LocationManifest {

//	LocationType getType();

	/**
	 * Returns the "root" path to the location described by this manifest.
	 * Depending on the exact location type, the meaning of this root path
	 * may vary. It can denote a single corpus file, an entire folder or the
	 * identifier of a database, for example.
	 *
	 * @return
	 */
	String getPath();

	/**
	 * If the data source is distributed then this method returns the manifest
	 * that describes the resolver to use when accessing different chunks of data.
	 * If the data is not of distributed nature this method returns {@code null}.
	 *
	 * @return
	 */
	PathResolverManifest getPathResolverManifest();

	void setPathResolverManifest(PathResolverManifest manifest);
}
