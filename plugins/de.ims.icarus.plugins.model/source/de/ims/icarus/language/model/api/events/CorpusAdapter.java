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
package de.ims.icarus.language.model.api.events;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusAdapter implements CorpusListener {

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#corpusChanged(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void corpusChanged(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#contextAdded(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void contextAdded(CorpusEvent e) {
		// no-op

	}

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#contextRemoved(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void contextRemoved(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#memberAdded(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void memberAdded(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#memberRemoved(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void memberRemoved(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#metaDataAdded(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void metaDataAdded(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#metaDataRemoved(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void metaDataRemoved(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#corpusSaved(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void corpusSaved(CorpusEvent e) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.api.events.CorpusListener#memberChanged(de.ims.icarus.language.model.api.events.CorpusEvent)
	 */
	@Override
	public void memberChanged(CorpusEvent e) {
		// no-op
	}

}
