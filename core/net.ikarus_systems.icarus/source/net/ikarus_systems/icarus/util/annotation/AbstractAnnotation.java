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

import java.nio.channels.IllegalSelectorException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractAnnotation implements Annotation {

	private int pointer = BEFORE_FIRST;

	/**
	 * @see net.ikarus_systems.icarus.util.annotation.Annotation#reset()
	 */
	@Override
	public void reset() {
		pointer = BEFORE_FIRST;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.annotation.Annotation#isBeforeFirst()
	 */
	@Override
	public boolean isBeforeFirst() {
		return pointer==BEFORE_FIRST;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.annotation.Annotation#isAfterLast()
	 */
	@Override
	public boolean isAfterLast() {
		return pointer>=getAnnotationCount();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.annotation.Annotation#nextAnnotation()
	 */
	@Override
	public boolean nextAnnotation() {
		if(isAfterLast())
			throw new IllegalSelectorException();
		
		pointer++;
		return !isAfterLast();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.annotation.Annotation#getIndex()
	 */
	@Override
	public int getIndex() {
		return pointer;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.annotation.Annotation#moveToAnnotation(int)
	 */
	@Override
	public void moveToAnnotation(int index) {
		if(index<0 || index>=getAnnotationCount())
			throw new IndexOutOfBoundsException();
		
		pointer = index;
	}

}
