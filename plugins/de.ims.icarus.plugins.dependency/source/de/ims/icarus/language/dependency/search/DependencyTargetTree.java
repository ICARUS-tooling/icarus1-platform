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
package de.ims.icarus.language.dependency.search;

import de.ims.icarus.language.dependency.DependencySentenceData;
import de.ims.icarus.search_tools.tree.AbstractSentenceTargetTree;
import de.ims.icarus.util.CorruptedStateException;


/**
 * Rooted tree view on dependency data structures.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyTargetTree extends AbstractSentenceTargetTree<DependencySentenceData> {

	public DependencyTargetTree() {
		super();
	}

	@Override
	protected boolean supports(Object data) {
		return data instanceof DependencySentenceData;
	}

	@Override
	protected int fetchSize() {
		return data.length();
	}

	@Override
	protected int fetchHead(int index) {
		return data.getHead(index);
	}



	// NODE METHODS

	@Override
	public String getForm() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.getForm(nodePointer);
	}

	@Override
	public String getPos() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.getPos(nodePointer);
	}

	@Override
	public String getLemma() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.getLemma(nodePointer);
	}

	/**
	 * Returns an always non-null array of feature expressions
	 */
	@Override
	public String getFeatures() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.getFeatures(nodePointer);
	}

	// EDGE METHODS

	public String getRelation() {
		/*if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$*/
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		return data.getRelation(nodePointer);
	}

	// GENERAL METHODS

	@Override
	public boolean isFlagSet(long flag) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.isFlagSet(nodePointer, flag);
	}

	@Override
	public Object getProperty(String key) {
		return getSource().getProperty(nodePointer, key);
	}

	// LOCKING METHODS

}
