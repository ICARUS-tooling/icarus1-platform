/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.annotation;

import de.ims.icarus.util.data.ContentType;

/**
 * Marks an object that contains other annotated objects
 * and is aware of the annotation type used. This interface
 * plays an important role for determining the actual
 * implementation of an {@code AnnotationManager} to be used for
 * managing the annotated data. Therefore it serves as a bridging
 * point for framework tools that rely on valid information about
 * the type of annotation used for data they display but of which
 * they are not aware.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationContainer {

	/**
	 * Returns the {@code ContentType} that describes the annotations
	 * used for data within this container.
	 */
	ContentType getAnnotationType();
}
