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
package de.ims.icarus.language.model.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.resources.ResourceManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UndoableCorpusEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 3374451587305862185L;

	private static final AtomicLong idGenerator = new AtomicLong();

	/**
	 * Describes an atomic change to the content of a corpus.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface AtomicChange {
		void execute();
	}

	private final long id;

	/**
	 * Holds the corpus of the mutation.
	 */
	private final Corpus corpus;

	/**
	 * Holds the list of atomic changes that make up this undoable mutation.
	 */
	private final List<AtomicChange> changes = new ArrayList<AtomicChange>();

	/**
	 * Specifies this undoable edit is significant. Default is true.
	 */
	private final boolean significant;

	private final String nameKey;

	/**
	 * Constructs a new undoable edit for the given corpus.
	 */
	public UndoableCorpusEdit(Corpus corpus) {
		this(corpus, true, null);
	}

	/**
	 * Constructs a new undoable edit for the given corpus.
	 */
	public UndoableCorpusEdit(Corpus corpus, String nameKey) {
		this(corpus, true, nameKey);
	}

	/**
	 * Constructs a new undoable edit for the given corpus.
	 */
	public UndoableCorpusEdit(Corpus corpus, boolean significant, String nameKey) {
		if(corpus==null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.corpus = corpus;
		this.significant = significant;
		this.nameKey = nameKey;

		id = idGenerator.incrementAndGet();
	}

	/**
	 * Hook to notify any listeners of the changes after an undo or redo has
	 * been carried out. This implementation is empty.
	 */
	public void dispatch() {
		// empty
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @return the changes
	 */
	public List<AtomicChange> getChanges() {
		return changes;
	}

	/**
	 * @return the significant
	 */
	@Override
	public boolean isSignificant() {
		return significant;
	}

	/**
	 * Returns true if this edit contains no changes.
	 */
	public boolean isEmpty() {
		return changes.isEmpty();
	}

	/**
	 * Adds the specified change to this edit. The change is an object that is
	 * expected to either have an undo and redo, or an execute function.
	 */
	public void add(AtomicChange change) {
		changes.add(change);
	}

	/**
	 * Executes all the atomic changes in reverse order of their
	 * registration and dispatches a notification afterwards.
	 */
	@Override
	public void undo() throws CannotUndoException {
		super.undo();

		int count = changes.size();

		for (int i = count - 1; i >= 0; i--) {
			AtomicChange change = changes.get(i);
			change.execute();
		}

		dispatch();
	}

	/**
	 * Executes all the atomic changes in the order of their
	 * registration and dispatches a notification afterwards.
	 */
	@Override
	public void redo() throws CannotRedoException {
		super.redo();

		int count = changes.size();

		for (int i = 0; i < count; i++) {
			AtomicChange change = changes.get(i);
			change.execute();
		}

		dispatch();
	}

	/**
	 * @see javax.swing.undo.AbstractUndoableEdit#getPresentationName()
	 */
	@Override
	public String getPresentationName() {
		return nameKey==null ? ResourceManager.getInstance().get(nameKey)
				: super.getPresentationName();
	}

	/**
	 * @see javax.swing.undo.AbstractUndoableEdit#getUndoPresentationName()
	 */
	@Override
	public String getUndoPresentationName() {
		String name = getPresentationName();
		String prefix = ResourceManager.getInstance().get("undo"); //$NON-NLS-1$

		return "".equals(name) ? prefix : prefix+" "+name; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see javax.swing.undo.AbstractUndoableEdit#getRedoPresentationName()
	 */
	@Override
	public String getRedoPresentationName() {
		String name = getPresentationName();
		String prefix = ResourceManager.getInstance().get("redo"); //$NON-NLS-1$

		return "".equals(name) ? prefix : prefix+" "+name; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
}
