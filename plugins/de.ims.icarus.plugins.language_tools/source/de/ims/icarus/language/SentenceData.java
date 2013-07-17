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

import java.io.Serializable;

import de.ims.icarus.ui.helper.TextItem;



/**
 * Abstract representation of a single sentence
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface SentenceData extends Serializable, TextItem {
	
	public static final int ROOT_INDEX = -1;

	/**
	 * Creates a copy of this {@code SentenceData} object
	 * @return a copy of this {@code SentenceData} object
	 */
	SentenceData clone();

	/**
	 * Returns the {@code form} tokens of the underlying sentence
	 * this {@code SentenceData} object represents (i.e. all
	 * the terminal symbols) 
	 * @return the {@code form} tokens of the underlying sentence
	 */
	String getForm(int index);

	/**
	 * Returns {@code true} if this {@code SentenceData} object
	 * represents the empty sentence. This is equivalent with
	 * {@link #getForms()} returning an empty array of {@code String} tokens.
	 * @return {@code true} if and only if the underlying sentence is empty
	 */
	boolean isEmpty();
	
	/**
	 * Returns the length of the underlying sentence structure, i.e.
	 * the number of terminal tokens in this {@code SentenceData} instance.
	 */
	int length();
	
	/**
	 * Returns the {@code Grammar} that created this {@code SentenceData} 
	 * object or that encapsulates the grammatical rules and structural
	 * terms that define the basis of this {@code SentenceData} object.
	 * 
	 * @return the {@code 'grammatical'} source of this {@code SentenceData}
	 * object
	 */
	Grammar getSourceGrammar();
}
