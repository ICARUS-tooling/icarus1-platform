/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus;

import java.util.Map;

import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.UnsupportedSentenceDataException;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.util.Location;

/**
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Corpus {
	
	boolean isEditable();
	
	void setName(String name);
	
	String getName();
	
	boolean hasGold();

	/**
	 * 
	 * @param item
	 * @throws UnsupportedSentenceDataException
	 * @throws UnsupportedOperationException
	 */
	void add(SentenceData item);
	
	/**
	 * 
	 * @param item
	 * @param index
	 * @throws UnsupportedSentenceDataException
	 * @throws UnsupportedOperationException
	 */
	void add(SentenceData item, int index);
	
	/**
	 * 
	 * @param item
	 * @throws UnsupportedSentenceDataException
	 * @throws UnsupportedOperationException
	 */
	void remove(SentenceData item);
	
	/**
	 * 
	 * @param index
	 * @throws UnsupportedOperationException
	 */
	void remove(int index);

	/**
	 * 
	 * @return
	 */
	boolean isLoaded();

	/**
	 * Loads this {@code Corpus}. In a typical synchronous
	 * implementation this method will cover the entire 
	 * loading process and return once all data is loaded.
	 * For asynchronous implementations this method returns
	 * immediately and actual loading is done in a background
	 * thread.
	 * @throws Exception if the loading failed
	 */
	void load() throws Exception;

	/**
	 * Sets the new {@code Location} to be used for this 
	 * {@code Corpus}. Subsequent calls to {@link #isLoaded()}
	 * will return {@code false} at least until the first call
	 * to {@link #load()} is performed 
	 * @param location the new {@code Location} to be used
	 */
	void setLocation(Location location);

	/**
	 * Returns the {@code Location} this {@code Corpus} is
	 * loading data from
	 * @return the {@code source} of this {@code Corpus}
	 */
	Location getLocation();

	/**
	 * Returns the number of {@code SentenceData} objects that
	 * this {@code Corpus} currently contains. 
	 * @return the number of elements in this {@code Corpus}
	 */
	int size();
	
	/**
	 * Releases all data within this {@code Corpus} so that 
	 * later calls to {@link #isLoaded()} return {@code false}.
	 */
	void free();
	
	void saveState(CorpusDescriptor descriptor);
	
	void loadState(CorpusDescriptor descriptor);

	/**
	 * Synchronously fetches the {@link SentenceData} object
	 * for the specified index. If this {@code Corpus} supports
	 * asynchronous loading of elements then this method may
	 * return {@code null} if the desired {@code index} is not 
	 * available.
	 * 
	 * @param index the index of interest
	 * @return the {@code SentenceData} object at position {@code index}
	 * within this {@code Corpus}
	 */
	SentenceData get(int index);
	
	SentenceData getGold(int index);


	/**
	 * Asynchronously fetches the {@link SentenceData} object
	 * for the specified index.<p>
	 * This method might return {@code null} and use the optional
	 * {@link CorpusObserver} to notify about a successful loading
	 * of the desired data later. Implementations should only
	 * store a weak reference to the provided {@code observer} since
	 * the calling code might decide to release the {@code CorpusObserver}
	 * object and its associated resources while the loading is 
	 * still in progress. Therefore exclusive ownership of this object 
	 * should be left to the original code that created it.
	 * 
	 * @param index the index of interest
	 * @param observer the {@code CorpusObserver} to be notified
	 * when the asynchronous loading of the desired {@code SentenceData}
	 * is finished
	 * @return
	 */
	SentenceData get(int index, CorpusObserver observer);

	SentenceData getGold(int index, CorpusObserver observer);
	
	/**
	 * Returns the {@code Class} used for {@code SentenceData} objects
	 * in this {@code Corpus}. 
	 * @return the {@code Class} used to represent sentence data within
	 * this {@code Corpus}
	 */
	Class<? extends SentenceData> getEntryClass();
	
	CorpusMetaData getMetaData();

	Object getProperty(String key);
	
	void setProperty(String key, Object value);
	
	Map<String, Object> getProperties();
	
	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#addListener(java.lang.String, net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	void addListener(String eventName, EventListener listener);

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	void removeListener(EventListener listener);

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener, java.lang.String)
	 */
	void removeListener(EventListener listener, String eventName);
}
