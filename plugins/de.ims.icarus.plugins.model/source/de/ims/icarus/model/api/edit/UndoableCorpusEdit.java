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
package de.ims.icarus.model.api.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.members.CorpusMember;
import de.ims.icarus.model.api.members.Markable;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UndoableCorpusEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 3374451587305862185L;

	private static final AtomicLong idGenerator = new AtomicLong();

	/**
	 * Describes an atomic change to the content of a corpus. As a general
	 * rule a change should check its preconditions and fail without any
	 * modifications when they are not entirely met.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface AtomicChange {

		/**
		 * Executes the change and modifies it internal information
		 * so that the next call to this method reverts the result.
		 * If a change requires a specific set of preconditions to be
		 * carried out, it should check them first or at least try to
		 * fail before making any permanent changes to the model, should
		 * any of those conditions be unfulfilled.
		 *
		 * @throws CorruptedStateException if the preconditions of this
		 * change are not met
		 */
		void execute();

		/**
		 * Returns the {@code CorpusMember} that this change affected.
		 * This is used to decide whether or not changes or entire edits
		 * should be purged from the undo history in the event of higher order
		 * changes like the removal of an entire context.
		 * @return
		 */
		CorpusMember getAffectedMember();
	}

	private final long id;

	/**
	 * Holds the corpus of the edit.
	 */
	private final Corpus corpus;

	/**
	 * Holds the list of atomic changes that make up this undoable edit.
	 */
	private final List<AtomicChange> changes = new ArrayList<>();

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
	 * Notifies listeners of the execution of this edit
	 */
	public void dispatch() {
//		getCorpus().getEditModel().fireEvent(
//				new EventObject(CorpusEditEvents.CHANGE, "edit", this)); //$NON-NLS-1$
		// for subclasses
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
	 * Adds the specified change to this edit.
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

		Lock lock = getCorpus().getLock();

		lock.lock();
		try {
			for (int i = count - 1; i >= 0; i--) {
				AtomicChange change = changes.get(i);
				change.execute();
			}
		} finally {
			lock.unlock();
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

		Lock lock = getCorpus().getLock();

		lock.lock();
		try {
			for (int i = 0; i < count; i++) {
				AtomicChange change = changes.get(i);
				change.execute();
			}
		} finally {
			lock.unlock();
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

	private Context getContextForChange(AtomicChange change){
		CorpusMember member = change.getAffectedMember();
		if(CorpusUtils.isMarkableMember(member)) {
			return ((Markable)member).getLayer().getContext();
		} else if(CorpusUtils.isLayerMember(member)) {
			return ((Layer)member).getContext();
		} else {
			return null;
		}
	}

	private Layer getLayerForChange(AtomicChange change){
		CorpusMember member = change.getAffectedMember();
		if(CorpusUtils.isMarkableMember(member)) {
			return ((Markable)member).getLayer();
		} else if(CorpusUtils.isLayerMember(member)) {
			return (Layer)member;
		} else {
			return null;
		}
	}

	/**
	 * Checks whether this edit affects the given {@code Context}.
	 * This is done by traversing all the atomic changes stored in
	 * this edit and comparing their context (determined via calling
	 * {@link AtomicChange#getAffectedMember()} and resolving that
	 * member's context) to the given one.
	 */
	public boolean isAffected(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		for(AtomicChange change : changes) {
			Context affected = getContextForChange(change);
			if(affected!=null && affected.equals(context)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Collects all the {@code Context} objects affected by this edit and
	 * returns them as a {@code Set}. If no contexts could be resolved using
	 * the {@link AtomicChange#getAffectedMember()} method of stored changes
	 * than the empty set will be returned.
	 * <p>
	 * Note that the order of appearance in the returned set does <b>not</b>
	 * reflect the order of changes as stored in this edit!
	 * The returned set is not guaranteed to be modifiable, so client code should
	 * treat it as read-only.
	 */
	public Set<Context> getAffectedContexts() {
		Set<Context> result = null;

		for(AtomicChange change : changes) {
			Context affected = getContextForChange(change);
			if(affected!=null) {
				if(result==null) {
					result = new HashSet<>();
				}
				result.add(affected);
			}
		}

		if(result==null) {
			result = Collections.emptySet();
		}

		return result;
	}

	/**
	 * Collects all the {@code Layer} objects affected by this edit and
	 * returns them as a {@code Set}. If no contexts could be resolved using
	 * the {@link AtomicChange#getAffectedMember()} method of stored changes
	 * than the empty set will be returned.
	 * <p>
	 * Note that the order of appearance in the returned set does <b>not</b>
	 * reflect the order of changes as stored in this edit!
	 * The returned set is not guaranteed to be modifiable, so client code should
	 * treat it as read-only.
	 */
	public Set<Layer> getAffectedLayers() {
		Set<Layer> result = null;

		for(AtomicChange change : changes) {
			Layer affected = getLayerForChange(change);
			if(affected!=null) {
				if(result==null) {
					result = new HashSet<>();
				}
				result.add(affected);
			}
		}

		if(result==null) {
			result = Collections.emptySet();
		}

		return result;
	}
}
