/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DataConversionException extends Exception {

	private static final long serialVersionUID = 235262909724603039L;

	public DataConversionException() {
		// no-op
	}

	public DataConversionException(String message) {
		super(message);
	}
	
	public DataConversionException(String message, Throwable cause) {
		super(message, cause);
	}
}
