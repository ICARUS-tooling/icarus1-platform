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
import de.ims.icarus.language.model.Corpus;

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
	 * Indicates that the corpus and all it's context objects have
	 * been saved (i.e. saved to a permanent storage). This event
	 * only fires for corpora that are editable.
	 *
	 * @param e
	 */
	void corpusSaved(CorpusEvent e);

	/**
	 * A new {@link Context} was added to the corpus.
	 * <p>
	 * The {@code "context"} property holds the {@code Context}
	 * that was added.
	 *
	 * @param e
	 */
	void contextAdded(CorpusEvent e);

	/**
	 * A {@link Context} was removed from the corpus
	 * <p>
	 * The {@code "context"} property holds the {@code Context}
	 * that was removed.
	 *
	 * @param e
	 */
	void contextRemoved(CorpusEvent e);

	/**
	 * A new member was added to the corpus.
	 * <p>
	 * This can be anything from a bare atomic markable, over
	 * a container, structure, edge or layer.
	 * <p>
	 * The {@code "member"} property holds the {@code CorpusMember}
	 * that was added.
	 *
	 * @param e
	 */
	void memberAdded(CorpusEvent e);

	/**
	 * A new member was removed from the corpus.
	 * <p>
	 * This can be anything from a bare atomic markable, over
	 * a container, structure, edge or layer.
	 * <p>
	 * The {@code "member"} property holds the {@code CorpusMember}
	 * that was removed.
	 *
	 * @param e
	 */
	void memberRemoved(CorpusEvent e);

	/**
	 * A new member was changed.
	 * <p>
	 * This event only fires on descriptor-based changes as defined in the
	 * {@link Corpus} documentation.
	 * <p>
	 * The {@code "member"} property holds the {@code CorpusMember}
	 * that was changed.
	 *
	 * @param e
	 */
	void memberChanged(CorpusEvent e);

	/**
	 * A new {@code MetaData} object was added to the corpus
	 * <p>
	 * The {@code "metadata"} property holds the {@code MetaData}
	 * that was added.
	 * <br>
	 * The {@code "layer"} property holds the {@code Layer} the
	 * meta-data has been added for.
	 *
	 * @param e
	 */
	void metaDataAdded(CorpusEvent e);

	/**
	 * A new {@code MetaData} object was removed from the corpus
	 * <p>
	 * The {@code "metadata"} property holds the {@code MetaData}
	 * that was removed.
	 * <br>
	 * The {@code "layer"} property holds the {@code Layer} the
	 * meta-data has been removed from.
	 *
	 * @param e
	 */
	void metaDataRemoved(CorpusEvent e);
}
