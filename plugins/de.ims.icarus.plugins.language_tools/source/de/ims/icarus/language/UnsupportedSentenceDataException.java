/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language;

/**
 * Signals that a given {@code SentenceData} object did not match
 * the requirements of a certain method.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class UnsupportedSentenceDataException extends RuntimeException {

	private static final long serialVersionUID = 548715288726730673L;

	/**
	 * 
	 */
	public UnsupportedSentenceDataException() {
	}

	/**
	 * @param message
	 */
	public UnsupportedSentenceDataException(String message) {
		super(message);
	}
}
