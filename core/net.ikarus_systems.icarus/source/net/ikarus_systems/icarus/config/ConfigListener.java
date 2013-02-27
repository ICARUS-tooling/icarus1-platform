/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.config;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ConfigListener {

	void invoke(ConfigRegistry sender, ConfigEvent event);
}
