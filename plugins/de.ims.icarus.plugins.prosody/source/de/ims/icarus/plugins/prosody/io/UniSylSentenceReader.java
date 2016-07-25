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
package de.ims.icarus.plugins.prosody.io;

import java.io.IOException;

import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataReader;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.DocumentSet;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UniSylSentenceReader implements SentenceDataReader {

	private UniSylDocumentReader reader;
	private DocumentSet documentSet;
	private DocumentData document;

	private boolean doRead;

	private int documentIndex;
	private int sentenceIndex;

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {

		reader = new UniSylDocumentReader();
		documentSet = (DocumentSet) reader.create();

		documentIndex = 0;
		sentenceIndex = 0;
		doRead = true;
		document = null;

		options.put("documentSet", documentSet); //$NON-NLS-1$
		reader.init(location, options);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {
		if(doRead) {
			reader.next();
			doRead = false;
		}

		if(document!=null && sentenceIndex>=document.size()) {
			document = null;
		}

		if(document==null && documentIndex<documentSet.size()) {
			document = documentSet.get(documentIndex++);
			sentenceIndex = 0;
		}

		if(document==null) {
			return null;
		}

//		System.out.println("returning sentence "+sentenceIndex+" of "+document.size()); //$NON-NLS-1$

		return document.get(sentenceIndex++);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#close()
	 */
	@Override
	public void close() throws IOException {
		reader.close();
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return ProsodyUtils.getProsodySentenceContentType();
	}

}
