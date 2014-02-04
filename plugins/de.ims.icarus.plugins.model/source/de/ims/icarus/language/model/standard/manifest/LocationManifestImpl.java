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
package de.ims.icarus.language.model.standard.manifest;

import de.ims.icarus.language.model.io.LocationType;
import de.ims.icarus.language.model.manifest.LocationManifest;
import de.ims.icarus.language.model.manifest.PathResolverManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocationManifestImpl implements LocationManifest {

	private LocationType locationType = LocationType.FILE;
	private String path;
	private PathResolverManifest pathResolverManifest;

	/**
	 * @see de.ims.icarus.language.model.manifest.LocationManifest#getType()
	 */
	@Override
	public LocationType getType() {
		return locationType;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LocationManifest#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LocationManifest#getPathResolverManifest()
	 */
	@Override
	public PathResolverManifest getPathResolverManifest() {
		return pathResolverManifest;
	}

	/**
	 * @param locationType the locationType to set
	 */
	public void setType(LocationType locationType) {
		if (locationType == null)
			throw new NullPointerException("Invalid locationType"); //$NON-NLS-1$

		this.locationType = locationType;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		if(path.isEmpty())
			throw new IllegalArgumentException("Empty path"); //$NON-NLS-1$

		this.path = path;
	}

	/**
	 * @param pathResolverManifest the pathResolverManifest to set
	 */
	public void setPathResolverManifest(PathResolverManifest pathResolverManifest) {
		if (pathResolverManifest == null)
			throw new NullPointerException("Invalid pathResolverManifest"); //$NON-NLS-1$

		this.pathResolverManifest = pathResolverManifest;
	}
}
