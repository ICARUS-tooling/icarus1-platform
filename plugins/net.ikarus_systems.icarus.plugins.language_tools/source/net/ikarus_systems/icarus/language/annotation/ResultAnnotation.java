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
	 * Returns the total number of hits the underlying result represents
	 * @return
	 */
	int getHitCount();
	
	/**
	 * Returns the internal hit pointer to a position
	 * right before the first hit
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
	boolean nextHit();
	
	/**
	 * Returns the current value of the internal index pointer
	 * @return
	 */
	int getIndex();
	
	/**
	 * Move the internal index pointer so that subsequent calls
	 * to getter methods refer to the given index
	 * @param hitIndex
	 */
	void moveToHit(int hitIndex);
}
