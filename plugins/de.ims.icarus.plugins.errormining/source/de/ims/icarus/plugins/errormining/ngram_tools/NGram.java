/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining.ngram_tools;

import java.util.concurrent.atomic.AtomicBoolean;

import de.ims.icarus.util.PropertyChangeSource;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public abstract class NGram extends PropertyChangeSource {
	
	private NGramState state = NGramState.BLANK;
	
	private Object lock = new Object();
	
	private final NGramDescriptor descriptor;
	
	private AtomicBoolean cancelled = new AtomicBoolean();
	
	protected NGram(NGramDescriptor descriptor) {
		if(descriptor==null)
			throw new NullPointerException("Invalid descriptor"); //$NON-NLS-1$
		
		this.descriptor = descriptor;
	}

	final void setState(NGramState state) {
		NGramState oldValue;
		synchronized (lock) {
			oldValue = this.state;
			this.state = state;
		}
		
		firePropertyChange("state", oldValue, state); //$NON-NLS-1$
	}
	
	public final NGramState getState() {
		synchronized (lock) {
			return state;
		}
	}
	
	public final NGramDescriptor getDescriptor() {
		return descriptor;
	}
	
	public final NGramQuery getQuery() {
		return descriptor.getQuery();
	}
	
	public final boolean isCancelled() {
		return cancelled.get();
	}
	
	public final boolean isDone() {
		synchronized (lock) {
			return state==NGramState.FINISHED || state==NGramState.CANCELLED;
		}
	}
	
	/**
	 * Attempts to cancel this ngram by setting the internal {@code cancelled}
	 * flag to {@code true}.
	 * This method will throw an {@link IllegalArgumentException} if the
	 * ngram is not yet running or has already been finished or cancelled.
	 */
	public final void cancel() {
		synchronized (lock) {
			NGramState state = getState();
			if(state==NGramState.BLANK)
				throw new IllegalStateException("NGram generation not started yet!"); //$NON-NLS-1$
			if(state!=NGramState.RUNNING)
				throw new IllegalStateException("NGram generator not running!"); //$NON-NLS-1$
			if(!cancelled.compareAndSet(false, true))
				throw new IllegalStateException("NGram generator already cancelled!"); //$NON-NLS-1$
			
			setState(NGramState.CANCELLED);
		}
	}
	
	/**
	 * Runs the ngram and constructs the internal {@code NGramResult} object.
	 * Note that an implementation should regularly check for user originated
	 * cancellation by invoking {@link #isCancelled()}.
	 */
	public abstract void execute() throws Exception;
	
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
	public abstract NGramResult getResult();
}
