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
package de.ims.icarus.language.coref.io;

import de.ims.icarus.io.Reader;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.DocumentSet;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.plugins.coref.io.CONLL12DocumentReader;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CONLL12AllocationReader implements AllocationReader {

	private DocumentSet documentSet;
	private Location location;
	private Options options;

	/**
	 * @see de.ims.icarus.language.coref.io.AllocationReader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options, de.ims.icarus.language.coref.DocumentSet)
	 */
	@Override
	public void init(Location location, Options options,
			DocumentSet documentSet) throws Exception {

		this.location = location;
		this.options = options;
		this.documentSet = documentSet;
	}

	/**
	 * @see de.ims.icarus.language.coref.io.AllocationReader#readAllocation(de.ims.icarus.language.coref.CoreferenceAllocation)
	 */
	@Override
	public void readAllocation(CoreferenceAllocation target) throws Exception {
		try(Reader<DocumentData> reader = new CONLL12DocumentReader()) {

			DocumentSet documentSet = CoreferenceUtils.loadDocumentSet(reader, location, options);
			CoreferenceAllocation defaultAllocation = documentSet.getDefaultAllocation();

			target.setProperties(defaultAllocation.getProperties());

			for(String documentId : defaultAllocation.getDocumentIds()) {
				target.setSpanSet(documentId, defaultAllocation.getSpanSet(documentId));
				target.setEdgeSet(documentId, defaultAllocation.getEdgeSet(documentId));
			}
		}
	}

}
