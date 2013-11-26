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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.errormining.annotation;

import java.nio.channels.IllegalSelectorException;

import de.ims.icarus.language.dependency.annotation.DependencyAnnotation;
import de.ims.icarus.plugins.errormining.ngram_search.DefaultNGramHighlight;

public class NGramAnnotation implements DependencyAnnotation {
	
	protected DefaultNGramHighlight highlight;		
	private int pointer = BEFORE_FIRST;			
	
	public NGramAnnotation (DefaultNGramHighlight highlight){
		this.highlight = highlight;
	}
	
	
	public NGramHighlighting getHighlighting() {
		return NGramHighlighting.getInstance();
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#isHighlighted(int)
	 */
	@Override
	public boolean isHighlighted(int index) {
		return getHighlight(index)!=0L;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#getGroupId(int)
	 */
	@Override
	public int getGroupId(int index) {
		return -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#getGroupId(int, java.lang.String)
	 */
	@Override
	public int getGroupId(int index, String token) {
		return -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#isNodeHighlighted(int)
	 */
	@Override
	public boolean isNodeHighlighted(int index) {
		return true;
		//return getHighlighting().isNodeHighlighted(getHighlight(index));
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#isEdgeHighlighted(int)
	 */
	@Override
	public boolean isEdgeHighlighted(int index) {
		return false;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#isTransitiveHighlighted(int)
	 */
	@Override
	public boolean isTransitiveHighlighted(int index) {
		return false;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#isTokenHighlighted(int, java.lang.String)
	 */
	@Override
	public boolean isTokenHighlighted(int index, String token) {
		return true;
		//return getHighlighting().isTokenHighlighted(getHighlight(index), token);
	}

	/**
	 * @see de.ims.icarus.util.annotation.Annotation#getAnnotationCount()
	 */
	@Override
	public int getAnnotationCount() {
		return 1;
	}

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

	/**
	 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#getHighlight(int)
	 */
	@Override
	public long getHighlight(int index) {
		if(isBeforeFirst() || isAfterLast())
			throw new IllegalStateException();			
		return highlight.getHighlight(index);
	}
}