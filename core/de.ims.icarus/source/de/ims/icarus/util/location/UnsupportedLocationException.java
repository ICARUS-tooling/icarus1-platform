/*
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
