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
package de.ims.icarus.plugins.coref.io;

import java.io.IOException;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.io.Reader;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.plugins.coref.io.CONLL12Utils.BlockHandler;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;
import de.ims.icarus.util.strings.CharTableBuffer;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CONLL12DocumentReader implements Reader<CoreferenceDocumentData> {

	private CharTableBuffer buffer;
	private BlockHandler blockHandler;
	private CoreferenceDocumentSet documentSet;

	public CONLL12DocumentReader() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.io.Reader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
		if(location==null)
			throw new NullPointerException("Invalid location"); //$NON-NLS-1$

		documentSet = (CoreferenceDocumentSet) options.get("documentSet"); //$NON-NLS-1$

		blockHandler = new BlockHandler();

		buffer = new CharTableBuffer();
		buffer.startReading(IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options)));
		buffer.setRowFilter(blockHandler);
	}

	/**
	 * @see de.ims.icarus.io.Reader#next()
	 */
	@Override
	public CoreferenceDocumentData next() throws IOException, UnsupportedFormatException {
		return CONLL12Utils.readDocumentData(documentSet, buffer, blockHandler);
	}

	/**
	 * @see de.ims.icarus.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			buffer.close();
		} finally {
			buffer = null;
		}
	}

	/**
	 * @see de.ims.icarus.io.Reader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

}
