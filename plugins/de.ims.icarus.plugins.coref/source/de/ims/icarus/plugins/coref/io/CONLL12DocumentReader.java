/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.io;

import java.io.BufferedReader;
import java.io.IOException;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.io.Reader;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.CoreferenceUtils;
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
public class CONLL12DocumentReader implements Reader<CoreferenceDocumentData> {

	private BufferedReader reader;
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
			throw new IllegalArgumentException("Invalid location"); //$NON-NLS-1$
		
		documentSet = (CoreferenceDocumentSet) options.get("documentSet"); //$NON-NLS-1$
		reader = IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options));
	}

	/**
	 * @see de.ims.icarus.io.Reader#next()
	 */
	@Override
	public CoreferenceDocumentData next() throws IOException, UnsupportedFormatException {
		return CONLL12Utils.readDocumentData(documentSet, reader);
	}

	/**
	 * @see de.ims.icarus.io.Reader#close()
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
	 * @see de.ims.icarus.io.Reader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

}
