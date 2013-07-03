/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.annotation;


/**
 * Marks an object as the owner of an {@code AnnotationManager}.
 * Generally this dedicated object is solely in charge of controlling
 * the current state of the manager.
 * <p>
 * External sources that use the manager returned by {@link #getAnnotationManager()}
 * should respect that fact and only treat it as <i>read-only</i> to get
 * the annotation information they require and not change its state.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationController {
	
	public static final String ANNOTATION_MANAGER_PROPERTY = "annotationManager"; //$NON-NLS-1$

	AnnotationManager getAnnotationManager();
}
