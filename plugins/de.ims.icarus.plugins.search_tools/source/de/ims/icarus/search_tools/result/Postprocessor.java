/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.result;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Postprocessor {

	Object postprocess(Object data, ResultEntry resultEntry);
}
