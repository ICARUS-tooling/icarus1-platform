/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;

/**
 * 
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Grammar {
	
	/**
	 * Returns an identifier for this {@code Grammar}
	 * @return
	 */
	String getIdentifier();

	/**
	 * 
	 * @return an empty {@code SentenceData} object
	 */
	SentenceData createEmptySentenceData();

	
	SentenceData createExampleSentenceData();
	
	/**
	 * Returns the used super class for {@code SentenceData}
	 * objects returned from this {@code Grammar}. Typically
	 * this will be an {@code interface}.
	 * @return the super class of {@code SentenceData} objects
	 * returned by this {@code Grammar}
	 */
	Class<? extends SentenceData> getBaseClass();
}
