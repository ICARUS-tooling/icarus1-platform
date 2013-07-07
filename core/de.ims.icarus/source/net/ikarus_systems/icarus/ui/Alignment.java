/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/Alignment.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.ui;

/**
 * @author Markus Gärtner 
 * @version $Id: Alignment.java 7 2013-02-27 13:18:56Z mcgaerty $
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
