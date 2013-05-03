/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank.annotation;

/**
 * Definitions on how to display annotations.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum AnnotationDisplayMode {

	/**
	 * All available annotations should be displayed.
	 */
	ALL_ANNOTATIONS,
	
	/**
	 * No annotation is to be displayed whether or not
	 * any annotations exist.
	 */
	NO_ANNOTATIONS,
	
	/**
	 * Only the first annotation is to be displayed.
	 * When the number of available annotations is below
	 * or equal to 1 then this is effectively equivalent
	 * to {@value #ALL_ANNOTATIONS}.
	 */
	FIRST_ANNOTATION_ONLY,
	
	/**
	 * Only the last annotation is to be displayed.
	 * When the number of available annotations is below
	 * or equal to 1 then this is effectively equivalent
	 * to {@value #ALL_ANNOTATIONS}.
	 */
	LAST_ANNOTATION_ONLY,
	
	/**
	 * The choice of which annotation to display is made 
	 * by the user.
	 */
	SELECTED_ANNOTATION,
}
