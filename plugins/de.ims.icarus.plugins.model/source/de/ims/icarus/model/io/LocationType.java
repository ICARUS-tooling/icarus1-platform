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
package de.ims.icarus.model.io;

import java.net.URL;

import de.ims.icarus.model.api.manifest.LocationManifest;
import de.ims.icarus.model.xml.XmlResource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum LocationType implements XmlResource {

	/**
	 * Specifies that a certain location denotes a local file object
	 * accessible via a simple path string.
	 */
	FILE,

	/**
	 * Marks a location as remotely accessible via a dedicated {@link URL}
	 */
	NETWORK,

	/**
	 * Locations with this type denote a database of arbitrary implementation.
	 * It is up to the {@link ResourcePath} or {@link LocationManifest} to provide
	 * additional information to properly access the database.
	 */
	DATABASE;

	/**
	 * @see de.ims.icarus.model.api.xml.XmlResource#getValue()
	 */
	@Override
	public String getValue() {
		return name();
	}

	public static LocationType parseLocationType(String s) {
		return valueOf(s.toUpperCase());
	}
}
