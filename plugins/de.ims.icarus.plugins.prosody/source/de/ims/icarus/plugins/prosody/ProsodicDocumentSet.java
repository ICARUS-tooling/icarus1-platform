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

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodicDocumentSet extends CoreferenceDocumentSet {

	public void add(DefaultProsodicDocumentData data) {
		super.add(data);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentSet#add(de.ims.icarus.language.coref.CoreferenceDocumentData)
	 */
	@Override
	public void add(CoreferenceDocumentData data) {
		add((DefaultProsodicDocumentData)data);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentSet#newDocument(java.lang.String)
	 */
	@Override
	public DefaultProsodicDocumentData newDocument(String id) {
		DefaultProsodicDocumentData data = new DefaultProsodicDocumentData(this, size());
		data.setId(id);
		add(data);
		return data;
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceDocumentSet#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return ProsodyUtils.getProsodyDocumentContentType();
	}

}
