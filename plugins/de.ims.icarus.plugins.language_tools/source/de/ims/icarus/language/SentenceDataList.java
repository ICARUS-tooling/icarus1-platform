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

import de.ims.icarus.util.data.DataList;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SentenceDataList extends DataList<SentenceData> {
	
	boolean supportsType(DataType type);

	/**
	 * Synchronously fetches the {@link SentenceData} object
	 * for the specified index. If this {@code list} supports
	 * asynchronous loading of elements then this method may
	 * return {@code null} if the desired {@code index} is not 
	 * available.
	 * 
	 * @param index the index of interest
	 * @return the {@code SentenceData} object at position {@code index}
	 * within this {@code list}
	 */
	SentenceData get(int index, DataType type);


	/**
	 * Asynchronously fetches the {@link SentenceData} object
	 * for the specified index.<p>
	 * This method might return {@code null} and use the optional
	 * {@link AvailabilityObserver} to notify about a successful loading
	 * of the desired data later. Implementations should only
	 * store a weak reference to the provided {@code observer} since
	 * the calling code might decide to release the {@code AvailabilityObserver}
	 * object and its associated resources while the loading is 
	 * still in progress. Therefore exclusive ownership of this object 
	 * should be left to the original code that created it.
	 * 
	 * @param index the index of interest
	 * @param type the type to fetch an entry for
	 * @param observer the {@code AvailabilityObserver} to be notified
	 * when the asynchronous loading of the desired {@code SentenceData}
	 * is finished
	 * @return
	 */
	SentenceData get(int index, DataType type, AvailabilityObserver observer);
}
