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
package de.ims.icarus.language.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.ui.events.ChangeSource;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class WrappedSentenceDataList extends ChangeSource implements SentenceDataList {

	private final List<SentenceData> buffer;
	private final ContentType contentType;

	public WrappedSentenceDataList(ContentType contentType) {
		this(contentType, 200);
	}

	public WrappedSentenceDataList(ContentType contentType, int capacity) {
		if (contentType == null)
			throw new NullPointerException("Invalid contentType"); //$NON-NLS-1$

		this.contentType = contentType;
		buffer = new ArrayList<>(capacity);
	}

	public void add(SentenceData item) {
		buffer.add(item);

		fireStateChanged();
	}

	public void add(Collection<? extends SentenceData> items) {
		buffer.addAll(items);

		fireStateChanged();
	}

	protected void clearUnnoticed() {
		buffer.clear();
	}

	public void clear() {
		buffer.clear();
		fireStateChanged();
	}

	public void add(SentenceDataList list) {
		if (list == null)
			throw new NullPointerException("Invalid list"); //$NON-NLS-1$

		if(!ContentTypeRegistry.isCompatible(contentType, list.getContentType()))
			throw new IllegalArgumentException("Incompatible content type. Expected "+contentType+" - got "+list.getContentType()); //$NON-NLS-1$ //$NON-NLS-2$

		for(int i=0; i<list.size(); i++) {
			buffer.add(list.get(i));
		}

		fireStateChanged();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return buffer.size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		return buffer.get(index);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return contentType;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return type==DataType.SYSTEM;
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

}
