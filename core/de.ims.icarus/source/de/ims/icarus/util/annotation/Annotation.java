/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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
