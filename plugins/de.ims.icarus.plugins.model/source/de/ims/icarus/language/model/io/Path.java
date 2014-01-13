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
package de.ims.icarus.language.model.io;

import java.io.File;
import java.net.URL;

import de.ims.icarus.util.Options;
import de.ims.icarus.util.location.Location;

/**
 * Helper class to describe an abstract physical data location in the context
 * of some {@link LocationType}. Depending on the type of the source data the
 * meaning of the returned {@code path} string may vary. While for {@value LocationType#FILE}
 * or {@value LocationType#NETWORK} the {@code path} itself is sufficient for accessing
 * the data by translating it into a {@link File} or {@link URL} object and then opening
 * the respective input stream, the matter becomes more complicated when data is stored within
 * a database system. In this case the {@code path} denotes the address of the database and
 * additional information (like the row index of a table to start from, etc...) can be
 * obtained via property values set on the {@code Path} object.
 * <p>
 * Note that unlike the {@link Location} interface, the {@code Path} model does not provide
 * translation of the abstract path to the data into a readable stream. All {@code LocationType}
 * specific behavior is to be implemented by the objects that use a {@code Path} instance to
 * load data from!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Path extends Options {

	private static final long serialVersionUID = 4608518181833150521L;

	private final String path;
	private final LocationType type;

	public Path(String path, LocationType type) {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		if (type == null)
			throw new NullPointerException("Invalid type");  //$NON-NLS-1$

		this.path = path;
		this.type = type;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the type
	 */
	public LocationType getType() {
		return type;
	}

}
