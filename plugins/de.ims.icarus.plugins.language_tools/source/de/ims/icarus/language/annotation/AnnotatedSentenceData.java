/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.annotation;

import de.ims.icarus.language.SentenceData;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.Annotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotatedSentenceData extends SentenceData, AnnotatedData {

	Annotation getAnnotation();
}
