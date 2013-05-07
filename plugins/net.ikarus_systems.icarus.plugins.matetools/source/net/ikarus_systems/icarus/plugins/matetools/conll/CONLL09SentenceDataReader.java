/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.matetools.conll;

import java.io.IOException;

import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataReader;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.UnsupportedLocationException;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class CONLL09SentenceDataReader implements SentenceDataReader {

	/**
	 * 
	 */
	public CONLL09SentenceDataReader() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#init(net.ikarus_systems.icarus.util.location.Location, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#getDataType()
	 */
	@Override
	public ContentType getDataType() {
		return DependencyUtils.getDependencyContentType();
	}

}
