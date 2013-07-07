/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/config/ConfigException.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.config;

/**
 * 
 * @author Markus Gärtner
 * @version $Id: ConfigException.java 7 2013-02-27 13:18:56Z mcgaerty $
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
