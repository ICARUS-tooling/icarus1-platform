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
package de.ims.icarus.language.coref.annotation;

import javax.swing.event.ChangeListener;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.util.Wrapper;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotatedCoreferenceDocumentData implements AnnotatedData, CoreferenceDocumentData, Wrapper<CoreferenceDocumentData> {

	private final CoreferenceDocumentData source;
	private Annotation annotation;

	public AnnotatedCoreferenceDocumentData(CoreferenceDocumentData source, Annotation annotation) {
		if(source==null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		this.source = source;
		this.annotation = annotation;
	}

	public AnnotatedCoreferenceDocumentData(CoreferenceDocumentData source) {
		this(source, null);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return source.supportsType(type);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		return source.get(index, type);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return source.get(index, type, observer);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return source.size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return source.getContentType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		source.addChangeListener(listener);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		source.removeChangeListener(listener);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#get(int)
	 */
	@Override
	public CoreferenceData get(int index) {
		return source.get(index);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#add(de.ims.icarus.language.coref.CoreferenceData)
	 */
	@Override
	public void add(CoreferenceData data) {
		source.add(data);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#getDocumentSet()
	 */
	@Override
	public CoreferenceDocumentSet getDocumentSet() {
		return source.getDocumentSet();
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#getSpanSet()
	 */
	@Override
	public SpanSet getSpanSet() {
		return source.getSpanSet();
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#getEdgeSet()
	 */
	@Override
	public EdgeSet getEdgeSet() {
		return source.getEdgeSet();
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#getDefaultSpanSet()
	 */
	@Override
	public SpanSet getDefaultSpanSet() {
		return source.getDefaultSpanSet();
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#getDefaultEdgeSet()
	 */
	@Override
	public EdgeSet getDefaultEdgeSet() {
		return source.getDefaultEdgeSet();
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#getDocumentIndex()
	 */
	@Override
	public int getDocumentIndex() {
		return source.getDocumentIndex();
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#getId()
	 */
	@Override
	public String getId() {
		return source.getId();
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentData#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return source.getProperty(key);
	}

	/**
	 * @see de.ims.icarus.util.annotation.AnnotatedData#getAnnotation()
	 */
	@Override
	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	/**
	 * @see de.ims.icarus.util.Wrapper#get()
	 */
	@Override
	public CoreferenceDocumentData get() {
		return source;
	}

}
