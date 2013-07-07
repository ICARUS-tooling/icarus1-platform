/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/config/ConfigListener.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.config;

/**
 * 
 * @author Markus Gärtner
 * @version $Id: ConfigListener.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public interface ConfigListener {

	void invoke(ConfigRegistry sender, ConfigEvent event);
}
