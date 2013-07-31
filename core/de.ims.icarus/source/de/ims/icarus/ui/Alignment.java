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
package de.ims.icarus.ui;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public enum Alignment {

	TOP,
	BOTTOM,
	LEFT,
	RIGHT,
	CENTER;
		
	public static Alignment parse(String s) {
		switch (s.toLowerCase()) {
		case "top": //$NON-NLS-1$
			return TOP;

		case "north": //$NON-NLS-1$
			return TOP;

		case "bottom": //$NON-NLS-1$
			return BOTTOM;

		case "south": //$NON-NLS-1$
			return BOTTOM;

		case "left": //$NON-NLS-1$
			return LEFT;

		case "west": //$NON-NLS-1$
			return LEFT;

		case "right": //$NON-NLS-1$
			return RIGHT;

		case "east": //$NON-NLS-1$
			return RIGHT;

		case "center": //$NON-NLS-1$
			return CENTER;

		default:
			throw new IllegalArgumentException("Unknown alignment: "+s); //$NON-NLS-1$
		}
	}
}
