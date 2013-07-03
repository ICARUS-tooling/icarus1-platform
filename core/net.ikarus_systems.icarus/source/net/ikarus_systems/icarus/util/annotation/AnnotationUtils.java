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

import javax.swing.JComponent;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class AnnotationUtils {

	private AnnotationUtils() {
		// no-op
	}

	public static void setAnnotationManager(JComponent comp, AnnotationManager annotationManager) {
		comp.putClientProperty(AnnotationController.ANNOTATION_MANAGER_PROPERTY, annotationManager);
	}
	
	public static AnnotationManager getAnnotationManager(JComponent comp) {
		return (AnnotationManager) comp.getClientProperty(AnnotationController.ANNOTATION_MANAGER_PROPERTY);
	}
}
