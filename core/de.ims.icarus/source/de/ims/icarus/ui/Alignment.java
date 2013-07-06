/*
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
