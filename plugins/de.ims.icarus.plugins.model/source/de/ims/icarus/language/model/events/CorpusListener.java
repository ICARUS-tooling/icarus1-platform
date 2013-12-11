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
package de.ims.icarus.language.model.events;

import de.ims.icarus.language.model.Context;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CorpusListener {

	/**
	 * A general change in the corpus occurred.
	 * 
	 * @param e
	 */
	void corpusChanged(CorpusEvent e);
	
	/**
	 * A new {@link Context} was added to the corpus.
	 * @param e
	 */
	void contextAdded(CorpusEvent e);
	
	/**
	 * A {@link Context} was removed from the corpus
	 * @param e
	 */
	void contextRemoved(CorpusEvent e);
	
	/**
	 * 
	 * @param e
	 */
	void memberAdded(CorpusEvent e);
	
	void memberRemoved(CorpusEvent e);
	
	/**
	 * Signals programmatic modifications to the state
	 * of a {@code CorpusMember}.
	 * <p>
	 * Note that changed originating from direct user actions
	 * are reported via {@link #memberMutated(CorpusEvent)}!
	 * 
	 * @param e
	 */
	void memberChanged(CorpusEvent e);
	
	/**
	 * Signals user modifications to the state of a {@code CorpusMember}.
	 * @param e
	 */
	void memberMutated(CorpusEvent e);
}
