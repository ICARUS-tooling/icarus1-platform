/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

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
