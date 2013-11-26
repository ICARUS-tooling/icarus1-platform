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
package de.ims.icarus.plugins.errormining;

import java.util.List;

import javax.swing.event.ChangeListener;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.util.annotation.AnnotationContainer;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class DetailedNGramSentenceDataList implements SentenceDataList, AnnotationContainer{

	protected List<SentenceData> sdl;
	
	/**
	 * @param sentenceDataDetailedList
	 */
	public DetailedNGramSentenceDataList(
			List<SentenceData> sdl) {
		this.sdl = sdl;
	}
	
	public DetailedNGramSentenceDataList() {

	}

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return sdl.size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		return sdl.get(index);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		return get(index);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return get(index);
	}

	/**
	 * @see de.ims.icarus.util.annotation.AnnotationContainer#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		return DependencyUtils.getDependencyAnnotationType();
	}
	
}
