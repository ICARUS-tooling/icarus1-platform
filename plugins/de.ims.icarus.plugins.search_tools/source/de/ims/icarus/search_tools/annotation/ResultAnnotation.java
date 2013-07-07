/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.annotation;

import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.util.annotation.Annotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ResultAnnotation extends Annotation {

	ResultEntry getResultEntry();
}
