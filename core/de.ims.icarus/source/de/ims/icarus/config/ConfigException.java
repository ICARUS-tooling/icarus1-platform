/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.config;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConfigException extends RuntimeException {

	private static final long serialVersionUID = -4451401320112231471L;
	
	private final String path;

	/**
	 * @param message
	 */
	public ConfigException(String message, String path) {
		super(message);
		this.path = path;
	}
	
	@Override
	public String getMessage() {
		return String.format("Config exception on path '%s': %s", path, super.getMessage()); //$NON-NLS-1$
	}
}
