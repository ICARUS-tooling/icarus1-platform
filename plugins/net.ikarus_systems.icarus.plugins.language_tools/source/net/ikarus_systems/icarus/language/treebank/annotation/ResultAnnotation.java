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
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ResultAnnotation {
	
	public static final int BEFORE_FIRST = -1;
	public static final int AFTER_LAST = -2;

	/**
	 * Returns the total number of annotations the underlying result represents
	 */
	int getAnnotationCount();
	
	/**
	 * Returns the internal annotation pointer to a position
	 * right before the first hit
	 */
	void reset();
	
	boolean isBeforeFirst();
	
	boolean isAfterLast();
	
	/**
	 * Moves the internal annotation pointer one step forward and returns
	 * true if it now points to a valid annotation
	 * @return
	 * @throws IllegalStateException if the internal annotation pointer is 
	 * currently after the last hit index 
	 */
	boolean nextAnnotation();
	
	/**
	 * Returns the current value of the internal index pointer
	 * @return
	 */
	int getIndex();
	
	/**
	 * Move the internal annotation pointer so that subsequent calls
	 * to getter methods refer to the given index
	 * @param hitIndex
	 */
	void moveToAnnotation(int annotationIndex);
}
