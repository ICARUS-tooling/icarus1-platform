/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/util/mpi/DataReceiver.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.util.mpi;

import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: DataReceiver.java 23 2013-04-17 12:39:04Z mcgaerty $
 *
 */
public interface DataReceiver<O extends Object> {

	void receiveData(O sender, Object data, Options options);
}
