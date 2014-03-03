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
package de.ims.icarus.language.model.standard.markable;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.Fragment;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.util.CorpusUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractFragment extends AbstractMarkable implements Fragment {

	private final Markable markable;

	/**
	 * @param id
	 * @param container
	 */
	public AbstractFragment(long id, Container container, Markable markable) {
		super(id, container);

		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$
		if(markable.getMemberType()!=MemberType.MARKABLE)
			throw new IllegalArgumentException("Cannot fragment a non-markable member: "+markable); //$NON-NLS-1$

		this.markable = markable;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getBeginOffset()
	 */
	@Override
	public int getBeginOffset() {
		return markable.getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return markable.getEndOffset();
	}

	/**
	 * @see de.ims.icarus.language.model.api.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.FRAGMENT;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Fragment#getMarkable()
	 */
	@Override
	public Markable getMarkable() {
		return markable;
	}

	/**
	 * Helper method to check whether or not the enclosing corpus is editable
	 * and to forward an atomic change to the edit model.
	 *
	 * @param change
	 * @throws UnsupportedOperationException if the corpus is not editable
	 */
	protected void execute(AtomicChange change) {
		Corpus corpus = getCorpus();

		if(!corpus.getManifest().isEditable())
			throw new UnsupportedOperationException("Corpus does not support modifications"); //$NON-NLS-1$

		corpus.getEditModel().execute(change);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.markable.AbstractMarkable#compareTo(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public int compareTo(Markable o) {
		return o instanceof Fragment ? CorpusUtils.compare(this, (Fragment)o)
				: CorpusUtils.compare(this, o);
	}
}
