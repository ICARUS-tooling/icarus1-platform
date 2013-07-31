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
package de.ims.icarus.util;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public enum Priority {

	LOWEST,
	LOW,
	STANDARD,
	HIGH,
	HIGHEST;
	
	public static Priority parse(String s) {
		switch (s.toLowerCase()) {
		case "highest": //$NON-NLS-1$
			return HIGHEST;

		case "high": //$NON-NLS-1$
			return HIGH;

		case "standard": //$NON-NLS-1$
			return STANDARD;

		case "default": //$NON-NLS-1$
			return STANDARD;

		case "": //$NON-NLS-1$
			return STANDARD;

		case "low": //$NON-NLS-1$
			return LOW;

		case "lowest": //$NON-NLS-1$
			return LOWEST;

		case "none": //$NON-NLS-1$
			return LOWEST;

		default:
			throw new IllegalArgumentException("Unknown priority: "+s); //$NON-NLS-1$
		}
	}
}
