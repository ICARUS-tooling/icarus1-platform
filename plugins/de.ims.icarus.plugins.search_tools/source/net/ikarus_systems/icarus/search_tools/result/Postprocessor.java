/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.result;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Postprocessor {

	Object postprocess(Object data, ResultEntry resultEntry);
}
