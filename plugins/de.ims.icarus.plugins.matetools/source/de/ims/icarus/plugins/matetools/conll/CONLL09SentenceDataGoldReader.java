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
package de.ims.icarus.plugins.matetools.conll;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataReader;
import de.ims.icarus.language.dependency.CompoundDependencyData;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.location.DefaultFileLocation;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class CONLL09SentenceDataGoldReader implements SentenceDataReader {

	protected CONLLReader09 reader;
	protected boolean normalize;
	protected int inputFormat; // 0 (default) or 1
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

		normalize = true;
		inputFormat = 0;
		count = 0;

		try {
			reader = new CONLLReader09(normalize);
			reader.startReading(location.openInputStream());
		} catch (IllegalArgumentException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"CoNLL State Exception", e.getCause()); //$NON-NLS-1$
		}

	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {

		SentenceData09 input;

		DependencyData resultdd = null;

		// more sentences left?
		if ((input = reader.getNext()) != null) {
			resultdd = MatetoolsCONLLUtils.readGold(input, count++, true, true);
		}

		// catch illegal state getcause -> originale
		return resultdd;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();
	}

	protected String ensureValid(String input) {
		return input == null ? "" : input; //$NON-NLS-1$
	}

	protected String ensureDummy(String input, String dummy) {
		return input == null ? dummy : input;
	}

	public static void main(String[] args) throws UnsupportedFormatException {

		Path file = Paths.get("E:\\test_small.txt"); //$NON-NLS-1$

		DefaultFileLocation dloc = new DefaultFileLocation(file);

		Options o = null;

		CONLL09SentenceDataGoldReader cr = new CONLL09SentenceDataGoldReader();
		try {
			cr.init(dloc, o);

			while (cr.next() != null) {
				SentenceData sd = cr.next();
				if (sd instanceof CompoundDependencyData) {
					CompoundDependencyData cdd = (CompoundDependencyData) sd;
					System.out.println("Gold: " + cdd.getData(DataType.GOLD)); //$NON-NLS-1$
					System.out
							.println("System: " + cdd.getData(DataType.SYSTEM)); //$NON-NLS-1$
				} else {
					System.out.println(sd);
				}
			}
			System.out.println("Finished reading"); //$NON-NLS-1$
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
