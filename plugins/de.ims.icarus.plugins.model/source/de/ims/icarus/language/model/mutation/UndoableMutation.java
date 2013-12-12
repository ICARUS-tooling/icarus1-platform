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
import java.util.Map;
import java.util.WeakHashMap;

import de.ims.icarus.language.model.Context;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.MutablePrimitives.MutableLong;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class UndoableMutation {

	private static final Map<Context, MutableLong> generations = new WeakHashMap<>();

	private static long createGenerationId(Mutator<?> source) {
		Context context = CorpusUtils.getContext(source.getSubject());
		if(context==null)
			throw new IllegalArgumentException("Unable to resolve context for mutator: "+source); //$NON-NLS-1$

		synchronized (generations) {
			MutableLong gen = generations.get(context);
			if(gen==null) {
				gen = new MutableLong();
				generations.put(context, gen);
			}

			return gen.increment();
		}
	}

	/**
	 * Describes an atomic change to the content of a corpus.
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 * 
	 */
	public interface AtomicMutation {
		void execute();
	}

	protected final long generationId;

	/**
	 * Holds the source of the mutation.
	 */
	protected final Mutator<?> source;

	/**
	 * Holds the list of atomic mutations that make up this undoable mutation.
	 */
	protected final List<AtomicMutation> mutations = new ArrayList<AtomicMutation>();

	/**
	 * Specifies this undoable mutation is significant. Default is true.
	 */
	protected final boolean significant;

	/**
	 * Specifies the state of the undoable mutation.
	 */
	protected boolean undone, redone;

	/**
	 * Constructs a new undoable mutation for the given source.
	 */
	public UndoableMutation(Mutator<?> source) {
		this(source, true);
	}

	/**
	 * Constructs a new undoable mutation for the given source.
	 */
	public UndoableMutation(Mutator<?> source, boolean significant) {
		if(source==null)
			throw new NullPointerException("Invalid mutator"); //$NON-NLS-1$

		this.source = source;
		this.generationId = createGenerationId(source);
		this.significant = significant;
	}

	/**
	 * Hook to notify any listeners of the changes after an undo or redo has
	 * been carried out. This implementation is empty.
	 */
	public void dispatch() {
		// empty
	}

	/**
	 * Hook to free resources after the edit has been removed from the command
	 * history. This implementation is empty.
	 */
	public void die() {
		// empty
	}

	/**
	 * @return the source
	 */
	public Mutator<?> getSource() {
		return source;
	}

	/**
	 * @return the generationId
	 */
	public long getGenerationId() {
		return generationId;
	}

	/**
	 * @return the changes
	 */
	public List<AtomicMutation> getMutations() {
		return mutations;
	}

	/**
	 * @return the significant
	 */
	public boolean isSignificant() {
		return significant;
	}

	/**
	 * @return the undone
	 */
	public boolean isUndone() {
		return undone;
	}

	/**
	 * @return the redone
	 */
	public boolean isRedone() {
		return redone;
	}

	/**
	 * Returns true if the mutation edit contains no changes.
	 */
	public boolean isEmpty() {
		return mutations.isEmpty();
	}

	/**
	 * Adds the specified change to this mutation. The change is an object that is
	 * expected to either have an undo and redo, or an execute function.
	 */
	public void add(AtomicMutation mutation) {
		mutations.add(mutation);
	}

	/**
	 * Executes all the atomic mutations in reverse order of their
	 * registration and dispatches a notification afterwards.
	 */
	public void undo() {
		if (!undone) {
			int count = mutations.size();

			for (int i = count - 1; i >= 0; i--) {
				AtomicMutation mutation = mutations.get(i);
				mutation.execute();
			}

			undone = true;
			redone = false;
		}

		dispatch();
	}

	/**
	 * Executes all the atomic mutations in the order of their
	 * registration and dispatches a notification afterwards.
	 */
	public void redo() {
		if (!redone) {
			int count = mutations.size();

			for (int i = 0; i < count; i++) {
				AtomicMutation mutation = mutations.get(i);
				mutation.execute();
			}

			undone = false;
			redone = true;
		}

		dispatch();
	}
}
