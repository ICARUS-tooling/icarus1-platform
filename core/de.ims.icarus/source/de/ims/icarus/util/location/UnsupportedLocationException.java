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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.location;


/**
 * Exception to indicate that a certain {@code Location} is not
 * supported by some method or framework. For example a reader
 * class might be able to only handle local files and would fail
 * to access remotely located data. This exception can also be used
 * when remote resources require some unsupported protocol or
 * are not accessible due to firewall or other technical means.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class UnsupportedLocationException extends Exception {

	private static final long serialVersionUID = 1648257446268571576L;
	
	private final Location location;

	/**
	 * 
	 */
	public UnsupportedLocationException(Location location) {
		this(null, location, null);
	}

	/**
	 * @param message
	 */
	public UnsupportedLocationException(String message, Location location, Throwable cause) {
		super(message, cause);
		this.location = location;
	}

	/**
	 * @param cause
	 */
	public UnsupportedLocationException(Throwable cause) {
		this(null, null, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedLocationException(String message, Throwable cause) {
		this(message, null, cause);
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
}
