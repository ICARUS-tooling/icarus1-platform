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
import de.ims.icarus.language.SentenceDataReader;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;


/**
 * 
 * CONLL 2012 shared task data format:
 * <p>
 * Column 	Type 	Description
 * 1 	Document ID 	This is a variation on the document filename
 * 2 	Part number 	Some files are divided into multiple parts numbered as 000, 001, 002, ... etc.
 * 3 	Word number 	
 * 4 	Word itself 	This is the token as segmented/tokenized in the Treebank. Initially the *_skel file contain the placeholder [WORD] which gets replaced by the actual token from the Treebank which is part of the OntoNotes release.
 * 5 	Part-of-Speech 	
 * 6 	Parse bit 	This is the bracketed structure broken before the first open parenthesis in the parse, and the word/part-of-speech leaf replaced with a *. The full parse can be created by substituting the asterix with the "([pos] [word])" string (or leaf) and concatenating the items in the rows of that column.
 * 7 	Predicate lemma 	The predicate lemma is mentioned for the rows for which we have semantic role information. All other rows are marked with a "-"
 * 8 	Predicate Frameset ID 	This is the PropBank frameset ID of the predicate in Column 7.
 * 9 	Word sense 	This is the word sense of the word in Column 3.
 * 10 	Speaker/Author 	This is the speaker or author name where available. Mostly in Broadcast Conversation and Web Log data.
 * 11 	Named Entities 	These columns identifies the spans representing various named entities.
 * 12:N 	Predicate Arguments 	There is one column each of predicate argument structure information for the predicate mentioned in Column 7.
 * N 	Coreference 	Coreference chain information encoded in a parenthesis structure.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CONLL12Reader implements SentenceDataReader {
	
	private BufferedReader reader;
	private CoreferenceDocumentData document;

	public CONLL12Reader() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
		if(location==null)
			throw new IllegalArgumentException("Invalid location"); //$NON-NLS-1$
		
		document = (CoreferenceDocumentData) options.firstSet("documentData"); //$NON-NLS-1$
		reader = IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options));
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public CoreferenceData next() throws IOException, UnsupportedFormatException {
		return CONLL12Utils.readData(document, reader);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#close()
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
	 * @see de.ims.icarus.language.SentenceDataReader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceContentType();
	}

}
