/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import java.util.concurrent.atomic.AtomicBoolean;

import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.util.PropertyChangeSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class Search extends PropertyChangeSource {
	
	private SearchState state = SearchState.BLANK;
	
	private Object lock = new Object();
	
	private final SearchQuery query;
	
	private AtomicBoolean cancelled = new AtomicBoolean();
	
	protected Search(SearchQuery query) {
		if(query==null)
			throw new IllegalArgumentException("Invalid query"); //$NON-NLS-1$
		
		this.query = query;
	}

	final void setState(SearchState state) {
		SearchState oldValue;
		synchronized (lock) {
			oldValue = this.state;
			this.state = state;
		}
		
		firePropertyChange("state", oldValue, state); //$NON-NLS-1$
	}
	
	public final SearchState getState() {
		synchronized (lock) {
			return state;
		}
	}
	
	public final SearchQuery getQuery() {
		return query;
	}
	
	public final boolean isCancelled() {
		return cancelled.get();
	}
	
	/**
	 * Attempts to cancel this search by setting the internal {@code cancelled}
	 * flag to {@code true}.
	 * This method will throw an {@link IllegalArgumentException} if the
	 * search is not yet running or has already been finished or cancelled.
	 */
	public final void cancel() {
		synchronized (lock) {
			SearchState state = getState();
			if(state==SearchState.BLANK)
				throw new IllegalStateException("Search not started yet!"); //$NON-NLS-1$
			if(state!=SearchState.RUNNING)
				throw new IllegalStateException("Search not running!"); //$NON-NLS-1$
			if(!cancelled.compareAndSet(false, true))
				throw new IllegalStateException("Search already cancelled!"); //$NON-NLS-1$
			
			setState(SearchState.CANCELLED);
		}
	}
	
	/**
	 * Runs the search and constructs the internal {@code SearchResult} object.
	 * Note that an implementation should regularly check for user originated
	 * cancellation by invoking {@link #isCancelled()}.
	 */
	public abstract void doSearch() throws Exception;
	
	/**
	 * Returns the (estimated) progress of the search in the range
	 * 0 to 100.
	 */
	public abstract int getProgress();
	
	/**
	 * Returns the result of this search operation.
	 * Note that not all implementations are required to support live
	 * previews of the final result. In this case they may decide to
	 * return {@code null} until the search has finished or an empty result.
	 */
	public abstract SearchResult getResult();
}
