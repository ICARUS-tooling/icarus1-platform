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
public class WrapperException extends RuntimeException {

	private static final long serialVersionUID = -1897212092904456702L;

	/**
	 * @param cause
	 */
	public WrapperException(Throwable cause) {
		super(cause);
	}
	
	public Exception getWrappedException() {
		return (Exception) getCause();
	}
	
	public Error getWrappedError() {
		return (Error) getCause();
	}
}
