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

import javax.swing.undo.UndoManager;

import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.events.CorpusAdapter;
import de.ims.icarus.language.model.events.CorpusEvent;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusUndoManager extends UndoManager {

	private static final long serialVersionUID = -1207749889681029406L;

	private final Corpus corpus;

	private long savedGeneration = 0L;

	public CorpusUndoManager(Corpus corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus");  //$NON-NLS-1$

		corpus.getEventManager().addCorpusListener(new CorpusAdapter(){

			/**
			 * @see de.ims.icarus.language.model.events.CorpusAdapter#corpusSaved(de.ims.icarus.language.model.events.CorpusEvent)
			 */
			@Override
			public void corpusSaved(CorpusEvent e) {
				markSaved();
			}

		});

		this.corpus = corpus;
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	protected void markSaved() {
		UndoableCorpusEdit edit = editToBeUndone();
		savedGeneration = edit==null ? 0L : edit.getId();
	}

	public boolean isSavedState() {
		UndoableCorpusEdit edit = editToBeUndone();
		long id = edit==null ? 0L : edit.getId();

		return savedGeneration==id;
	}

	/**
	 * @see javax.swing.undo.UndoManager#editToBeUndone()
	 */
	@Override
	protected UndoableCorpusEdit editToBeUndone() {
		return (UndoableCorpusEdit) super.editToBeUndone();
	}

	/**
	 * @see javax.swing.undo.UndoManager#editToBeRedone()
	 */
	@Override
	protected UndoableCorpusEdit editToBeRedone() {
		return (UndoableCorpusEdit) super.editToBeRedone();
	}

	/**
	 * @see javax.swing.undo.UndoManager#discardAllEdits()
	 */
	@Override
	public synchronized void discardAllEdits() {
		super.discardAllEdits();
	}

	/**
	 * @see javax.swing.undo.UndoManager#end()
	 */
	@Override
	public synchronized void end() {
		// TODO Auto-generated method stub
		super.end();
	}
}
