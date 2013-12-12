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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.model.mutation;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.model.Context;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.events.CorpusAdapter;
import de.ims.icarus.language.model.events.CorpusEvent;
import de.ims.icarus.language.model.events.CorpusListener;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;
import de.ims.icarus.util.collections.LongValueHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class CorpusUndoManager extends WeakEventSource {

	/**
	 * Maximum command history size. 0 means unlimited history. Default is 100.
	 */
	protected int size;

	/**
	 * List that contains the steps of the command history.
	 */
	protected List<UndoableMutation> history;

	/**
	 * Index of the element to be added next.
	 */
	protected int indexOfNextAdd;

	protected LongValueHashMap contextGenerations = new LongValueHashMap();

	protected CorpusListener contextTracker = new CorpusAdapter(){

		/**
		 * @see de.ims.icarus.language.model.events.CorpusAdapter#contextRemoved(de.ims.icarus.language.model.events.CorpusEvent)
		 */
		@Override
		public void contextRemoved(CorpusEvent e) {
			invalidateGeneration(e.getContext());
		}

	};

	/**
	 * Constructs a new undo manager with a default history size.
	 */
	public CorpusUndoManager() {
		this(100);
	}

	/**
	 * Constructs a new undo manager for the specified size.
	 */
	public CorpusUndoManager(int size) {
		this.size = size;
		clear();
	}

	public void install(Corpus corpus) {
		if(corpus==null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		corpus.getEventManager().addCorpusListener(contextTracker);
	}

	public void uninstall(Corpus corpus) {
		if(corpus==null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		corpus.getEventManager().removeCorpusListener(contextTracker);
	}

	/**
	 * 
	 */
	public boolean isEmpty() {
		return history.isEmpty();
	}

	/**
	 * Clears the command history.
	 */
	public void clear() {
		history = new ArrayList<>(size);
		indexOfNextAdd = 0;
		fireEvent(new EventObject(Events.CLEAR));
	}

	protected void invalidateGeneration(Context context) {
		contextGenerations.remove(context);
	}

	protected void refreshGeneration(UndoableMutation mutation) {
		Context context = CorpusUtils.getContext(mutation.getSource().getSubject());
		if(context==null)
			throw new IllegalArgumentException("Unable to resolve context for mutator: "+mutation.getSource()); //$NON-NLS-1$

		synchronized (contextGenerations) {
			contextGenerations.put(context, mutation.getGenerationId());
		}
	}

	/**
	 * Returns true if an undo is possible.
	 */
	public boolean canUndo() {
		return indexOfNextAdd > 0;
	}

	/**
	 * Undoes the last change.
	 */
	public void undo() {
		while (indexOfNextAdd > 0) {
			UndoableMutation mutation = history.get(--indexOfNextAdd);
			mutation.undo();

			if (mutation.isSignificant()) {
				fireEvent(new EventObject(Events.UNDO, "mutation", mutation)); //$NON-NLS-1$
				break;
			}
		}
	}

	/**
	 * Returns true if a redo is possible.
	 */
	public boolean canRedo() {
		return indexOfNextAdd < history.size();
	}

	/**
	 * Redoes the last change.
	 */
	public void redo() {
		int n = history.size();

		while (indexOfNextAdd < n) {
			UndoableMutation mutation = history.get(indexOfNextAdd++);
			mutation.redo();

			if (mutation.isSignificant()) {
				fireEvent(new EventObject(Events.REDO, "mutation", mutation)); //$NON-NLS-1$
				break;
			}
		}
	}

	/**
	 * Method to be called to add new undoable edits to the history.
	 */
	public void undoableMutationHappened(UndoableMutation mutation) {
		trim();

		if (size > 0 && size == history.size()) {
			history.remove(0);
		}

		history.add(mutation);
		indexOfNextAdd = history.size();
		fireEvent(new EventObject(Events.ADD, "mutation", mutation)); //$NON-NLS-1$
	}

	/**
	 * Removes all pending steps after indexOfNextAdd from the history, invoking
	 * die on each edit. This is called from {@link #undoableMutationHappened(UndoableMutation)}.
	 */
	protected void trim() {
		while (history.size() > indexOfNextAdd) {
			UndoableMutation mutation = history.remove(indexOfNextAdd);
			mutation.die();
		}
	}
}
