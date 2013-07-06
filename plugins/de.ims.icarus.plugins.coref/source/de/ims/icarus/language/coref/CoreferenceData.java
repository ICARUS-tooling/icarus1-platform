/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref;

import de.ims.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CoreferenceData extends SentenceData {
	
	public static final String SPEAKER_PROPERTY = "speaker"; //$NON-NLS-1$
	public static final String PHRASE_TREE_PROPERTY = "phraseTree"; //$NON-NLS-1$
	public static final String SENTENCE_ID_PROPERTY = "sentenceId"; //$NON-NLS-1$
	public static final String DOCUMENT_ID_PROPERTY = "documentId"; //$NON-NLS-1$
	public static final String PART_ID_PROPERTY = "partId"; //$NON-NLS-1$
	
	Object getProperty(String key);
	
	// NOTE: spans should always be sorted descending in order of size!
	Span[] getSpans();
}
