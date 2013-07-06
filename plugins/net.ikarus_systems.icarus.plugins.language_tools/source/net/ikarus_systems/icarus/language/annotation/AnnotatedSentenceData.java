/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.annotation;

import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.util.annotation.AnnotatedData;
import net.ikarus_systems.icarus.util.annotation.Annotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotatedSentenceData extends SentenceData, AnnotatedData {

	Annotation getAnnotation();
}
