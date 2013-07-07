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

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Annotation {
	
	public static final int BEFORE_FIRST = -1;
	public static final int AFTER_LAST = -2;

	/**
	 * Returns the total number of annotations this object represents
	 */
	int getAnnotationCount();
	
	/**
	 * Returns the internal annotation pointer to a position
	 * right before the first annotation
	 */
	void reset();
	
	boolean isBeforeFirst();
	
	boolean isAfterLast();
	
	/**
	 * Moves the internal hit pointer one step forward and returns
	 * true if it now points to a valid hit
	 * @return
	 * @throws IllegalStateException if the internal hit pointer is 
	 * currently after the last hit index 
	 */
	boolean nextAnnotation();
	
	/**
	 * Returns the current value of the internal index pointer
	 * @return
	 */
	int getIndex();
	
	/**
	 * Move the internal index pointer so that subsequent calls
	 * to getter methods refer to the given index
	 * @param index
	 */
	void moveToAnnotation(int index);
}
