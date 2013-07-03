/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.annotation;

import net.ikarus_systems.icarus.search_tools.result.ResultEntry;
import net.ikarus_systems.icarus.util.annotation.Annotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ResultAnnotation extends Annotation {

	ResultEntry getResultEntry();
}
