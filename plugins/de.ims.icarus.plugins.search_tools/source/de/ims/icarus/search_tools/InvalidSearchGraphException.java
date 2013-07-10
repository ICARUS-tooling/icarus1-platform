/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class InvalidSearchGraphException extends RuntimeException {

	private static final long serialVersionUID = -1553229545679799102L;

	public InvalidSearchGraphException() {
		// no-op
	}

	public InvalidSearchGraphException(String message) {
		super(message);
	}
}
