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
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.SentenceDataReader;
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
public class DirndlSentenceReader implements SentenceDataReader {

	private DirndlDocumentReader reader;
	private DocumentSet documentSet;

	private SentenceDataList document;
	private int sentenceIndex;

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {

		reader = new DirndlDocumentReader();
		documentSet = (DocumentSet) reader.create();

		options.put("documentSet", documentSet); //$NON-NLS-1$
		reader.init(location, options);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {
		if(document==null || sentenceIndex>=document.size()) {
			document = reader.next();

			if(document==null) {
				return null;
			}

			sentenceIndex = 0;
		}

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
