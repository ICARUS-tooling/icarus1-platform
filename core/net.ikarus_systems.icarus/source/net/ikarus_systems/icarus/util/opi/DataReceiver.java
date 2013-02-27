/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.opi;

import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DataReceiver<O extends Object> {

	void receiveData(O sender, Object data, Options options);
}
