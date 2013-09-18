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

import java.nio.channels.IllegalSelectorException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractAnnotation implements Annotation {

	private int pointer = BEFORE_FIRST;

	/**
	 * @see de.ims.icarus.util.annotation.Annotation#reset()
	 */
	@Override
	public void reset() {
		pointer = BEFORE_FIRST;
	}

	/**
	 * @see de.ims.icarus.util.annotation.Annotation#isBeforeFirst()
	 */
	@Override
	public boolean isBeforeFirst() {
		return pointer==BEFORE_FIRST;
	}

	/**
	 * @see de.ims.icarus.util.annotation.Annotation#isAfterLast()
	 */
	@Override
	public boolean isAfterLast() {
		return pointer>=getAnnotationCount();
	}

	/**
	 * @see de.ims.icarus.util.annotation.Annotation#nextAnnotation()
	 */
	@Override
	public boolean nextAnnotation() {
		if(isAfterLast())
			throw new IllegalSelectorException();
		
		pointer++;
		return !isAfterLast();
	}

	/**
	 * @see de.ims.icarus.util.annotation.Annotation#getIndex()
	 */
	@Override
	public int getIndex() {
		return pointer;
	}

	/**
	 * @see de.ims.icarus.util.annotation.Annotation#moveToAnnotation(int)
	 */
	@Override
	public void moveToAnnotation(int index) {
		if(index!=BEFORE_FIRST && index!=AFTER_LAST
				&& (index<0 || index>=getAnnotationCount()))
			throw new IndexOutOfBoundsException("Invalid annotation index: "+index); //$NON-NLS-1$
		
		pointer = index;
	}

}
