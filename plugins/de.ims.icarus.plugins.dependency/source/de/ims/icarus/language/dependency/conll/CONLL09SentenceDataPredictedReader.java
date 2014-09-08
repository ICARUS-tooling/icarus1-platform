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
package de.ims.icarus.language.dependency.conll;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataReader;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;
import de.ims.icarus.util.strings.CharTableBuffer;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class CONLL09SentenceDataPredictedReader implements SentenceDataReader {

	protected CharTableBuffer buffer;
	protected int count;

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#init(de.ims.icarus.util.location.Location,
	 *      de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {

		Path file = location.getLocalPath();

		if (file == null)
			throw new IllegalArgumentException("Filelocation Undef"); //$NON-NLS-1$

		if (Files.notExists(file))
			throw new FileNotFoundException("Missing File: " //$NON-NLS-1$
					+ file);

		if (options == null) {
			options = Options.emptyOptions;
		}

		count = 0;

		try {
			buffer = new CharTableBuffer();

			buffer.startReading(IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options)));
		} catch (IllegalArgumentException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to start reading CoNLL file: "+location, e.getCause()); //$NON-NLS-1$
		}

	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {

		DependencyData resultdd = null;

		if (buffer.next()) {
			try {
				resultdd = CONLLUtils.readPredicted09(buffer, count++);
			} catch(Exception e) {
				// Cannot be IOException or UnsupportedFormatException

				throw new IOException(buffer.getErrorMessage("Failed to read predicted CoNLL09 data"), e); //$NON-NLS-1$
			}
		}

		return resultdd;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#close()
	 */
	@Override
	public void close() {
		try {
			buffer.close();
		} catch (IOException e) {
			LoggerFactory.error(this, "Failed to close buffer", e); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();
	}
}
