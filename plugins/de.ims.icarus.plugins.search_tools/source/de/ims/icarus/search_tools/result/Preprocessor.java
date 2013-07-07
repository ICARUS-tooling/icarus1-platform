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
public interface Preprocessor {

	Object preprocess(Object data);
}
