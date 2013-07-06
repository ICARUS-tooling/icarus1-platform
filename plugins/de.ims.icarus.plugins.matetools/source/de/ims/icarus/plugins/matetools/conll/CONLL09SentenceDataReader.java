/*
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class CONLL09SentenceDataReader implements SentenceDataReader {

	protected CONLLReader09 reader;
	protected boolean normalize;
	protected boolean gold;
	protected boolean system;
	protected int inputFormat; // 0 (default) or 1

	/**
	 * 
	 */
	public CONLL09SentenceDataReader() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#init(de.ims.icarus.util.location.Location,
	 *      de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {

		File file = location.getFile();

		if (file == null)
			throw new IllegalArgumentException("Filelocation Undef"); //$NON-NLS-1$	

		if (!file.exists())
			throw new FileNotFoundException("Missing File: " //$NON-NLS-1$
					+ file.getAbsolutePath());

		if (options == null) {
			options = Options.emptyOptions;
		}

		normalize = true;
		inputFormat = 0;

		gold = options.get(INCLUDE_GOLD_OPTION, false);
		system = options.get(INCLUDE_SYSTEM_OPTION, false);

		if (!gold && !system) {
			system = true;
		}
		inputFormat = 0;

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

			// read system
			if (system) {
				resultdd = CONLLUtils.readPredicted(input, true, true);
			}

			// read gold
			if (gold) {

				DependencyData dd_gold = CONLLUtils.readGold(input, true, true);
				// check if system is read before, and generate compund data
				if (resultdd != null) {
					CompoundDependencyData cdd = new CompoundDependencyData();
					cdd.setData(DataType.SYSTEM, resultdd);
					cdd.setData(DataType.GOLD, dd_gold);
					resultdd = cdd;
				} else {
					resultdd = dd_gold;

				}
			}

		}

		// catch illegal state getcause -> originale
		return (SentenceData) resultdd;
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

		File file = new File("E:\\test_small.txt"); //$NON-NLS-1$

		DefaultFileLocation dloc = new DefaultFileLocation(file);

		Options o = null;

		CONLL09SentenceDataReader cr = new CONLL09SentenceDataReader();
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
