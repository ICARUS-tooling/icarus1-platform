/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.coref.io;

import java.io.BufferedReader;
import java.io.IOException;

import net.ikarus_systems.icarus.io.IOUtil;
import net.ikarus_systems.icarus.io.Reader;
import net.ikarus_systems.icarus.language.coref.CoreferenceDocumentData;
import net.ikarus_systems.icarus.language.coref.CoreferenceUtils;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.UnsupportedLocationException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CONLL12DocumentReader implements Reader<CoreferenceDocumentData> {

	private BufferedReader reader;
	private int documentId = 0;
	
	public CONLL12DocumentReader() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.io.Reader#init(net.ikarus_systems.icarus.util.location.Location, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
		if(location==null)
			throw new IllegalArgumentException("Invalid location"); //$NON-NLS-1$
		
		reader = IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options));
	}

	/**
	 * @see net.ikarus_systems.icarus.io.Reader#next()
	 */
	@Override
	public CoreferenceDocumentData next() throws IOException, UnsupportedFormatException {
		return CONLL12Utils.readDocumentData(reader, documentId++);
	}

	/**
	 * @see net.ikarus_systems.icarus.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			reader.close();
		} finally {
			reader = null;
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.io.Reader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

}
