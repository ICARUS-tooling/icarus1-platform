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
package de.ims.icarus.model.standard.manifest;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.model.api.manifest.LocationManifest;
import de.ims.icarus.model.api.manifest.PathResolverManifest;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocationManifestImpl implements LocationManifest {
	private String path;
	private PathResolverManifest pathResolverManifest;

	private final List<PathEntry> pathEntries = new ArrayList<>();

	/**
	 * @see de.ims.icarus.model.api.manifest.LocationManifest#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LocationManifest#getPathResolverManifest()
	 */
	@Override
	public PathResolverManifest getPathResolverManifest() {
		return pathResolverManifest;
	}

	/**
	 * @param path the path to set
	 */
	@Override
	public void setPath(String path) {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		if(path.isEmpty())
			throw new IllegalArgumentException("Empty path"); //$NON-NLS-1$

		this.path = path;
	}

	@Override
	public void setPathResolverManifest(PathResolverManifest pathResolverManifest) {
		if (pathResolverManifest == null)
			throw new NullPointerException("Invalid pathResolverManifest"); //$NON-NLS-1$

		this.pathResolverManifest = pathResolverManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LocationManifest#getPathEntries()
	 */
	@Override
	public List<PathEntry> getPathEntries() {
		return CollectionUtils.getListProxy(pathEntries);
	}

	@Override
	public void addPathEntry(PathEntry entry) {
		if (entry == null)
			throw new NullPointerException("Invalid entry"); //$NON-NLS-1$

		pathEntries.add(entry);
	}

	@Override
	public void removePathEntry(PathEntry entry) {
		if (entry == null)
			throw new NullPointerException("Invalid entry"); //$NON-NLS-1$

		pathEntries.remove(entry);
	}

	public static class PathEntryImpl implements PathEntry {

		private final PathType type;
		private final String value;

		public PathEntryImpl(PathType type, String value) {
			if (type == null)
				throw new NullPointerException("Invalid type"); //$NON-NLS-1$
			if (value == null)
				throw new NullPointerException("Invalid value"); //$NON-NLS-1$

			this.type = type;
			this.value = value;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LocationManifest.PathEntry#getType()
		 */
		@Override
		public PathType getType() {
			return type;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LocationManifest.PathEntry#getValue()
		 */
		@Override
		public String getValue() {
			return value;
		}

	}
}
