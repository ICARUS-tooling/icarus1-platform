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
 * An extension of the {@code SentenceData} interface that supports
 * modifications and the ability to notify registered {@code SentenceDataListener}
 * objects about changes to a certain {@code MutableSentenceData} object.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface MutableSentenceData extends SentenceData {

	/**
	 * Returns a copy of this {@code MutableSentenceData} that
	 * represents the exact same state the original object was in
	 * at the time this method is called. Does not copy listeners!
	 * In contrast to the original method {@link SentenceData#clone()}
	 * the returned object has to be mutable!
	 * @return a new {@code MutableSentenceData} object that is a
	 * copy of the current state of this object
	 * @see de.ims.icarus.language.SentenceData#clone() 
	 */
	MutableSentenceData clone();
	
	/**
	 * Returns the current state of this mutable object as an
	 * immutable {@code SentenceData} instance
	 * @return an immutable version of the current state of this
	 * {@code MutableSentenceData} object
	 */
	SentenceData snapshot();

	/**
	 * Copies data from the provided {@code SentenceData} object.
	 * Implementations should throw {@code UnsupportedSentenceDataException}
	 * if the operation is not possible due to incompatibility.
	 * @param source the {@code SentenceData} object to copy data from
	 * @throws UnsupportedSentenceDataException if the {@code source}
	 * parameter is of an unknown {@code SentenceData} type or the content
	 * of this object is not compatible
	 */
	void copyFrom(SentenceData source);

	/**
	 * Erases all data contained within this {@code MutableSentenceData} so
	 * that subsequent calls to {@link #isEmpty()} return {@code true}
	 */
	void clear();

	/**
	 * Adds the given {@code SentenceDataListener} to the list
	 * of registered listeners. Implementations should make sure
	 * that no listener exists more than once in the internal list.
	 * @param listener the {@code SentenceDataListener} to be added
	 */
	void addSentenceDataListener(SentenceDataListener listener);

	/**
	 * Removes the given {@code SentenceDataListener} from the list
	 * of registered listeners. If the {@code listener} is not present
	 * no special actions should be taken
	 * @param listener the {@code SentenceDataListener} to be removed
	 */
	void removeSentenceDataListener(SentenceDataListener listener);
	
	/**
	 * Stores a mapping from {@code key} to a value of arbitrary type.
	 * It is up to an implementation how to handle {@code null} values
	 * but the recommended way is to use {@code null} as hint for removing
	 * a certain mapping entirely.
	 */
	void setProperty(String key, Object value);
	
	/**
	 * Returns the value of a previously stored mapping
	 */
	Object getProperty(String key);
}
