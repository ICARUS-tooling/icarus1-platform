/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/Priority.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.util;

/**
 * @author Markus Gärtner 
 * @version $Id: Priority.java 7 2013-02-27 13:18:56Z mcgaerty $
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
