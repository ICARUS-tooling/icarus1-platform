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
package de.ims.icarus.plugins.prosody;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.DefaultCoreferenceDocumentData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultProsodicDocumentData extends DefaultCoreferenceDocumentData implements ProsodicDocumentData {

	/**
	 * @param documentSet
	 * @param documentIndex
	 */
	public DefaultProsodicDocumentData(CoreferenceDocumentSet documentSet, int documentIndex) {
		super(documentSet, documentIndex);
	}

	/**
	 * @see de.ims.icarus.language.coref.DefaultCoreferenceDocumentData#get(int)
	 */
	@Override
	public ProsodicSentenceData get(int index) {
		return (ProsodicSentenceData) super.get(index);
	}

	/**
	 * @see de.ims.icarus.language.coref.DefaultCoreferenceDocumentData#get(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public ProsodicSentenceData get(int index, DataType type) {
		return (ProsodicSentenceData) super.get(index, type);
	}

	/**
	 * @see de.ims.icarus.language.coref.DefaultCoreferenceDocumentData#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public ProsodicSentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return (ProsodicSentenceData) super.get(index, type, observer);
	}

}
