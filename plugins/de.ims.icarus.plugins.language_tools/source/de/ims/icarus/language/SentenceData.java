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
package de.ims.icarus.language;

import java.io.Serializable;

import de.ims.icarus.ui.text.TextItem;



/**
 * Abstract representation of a single sentence
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SentenceData extends Serializable, TextItem {

	public static final int ROOT_INDEX = -1;

	int getIndex();

	/**
	 * Creates a copy of this {@code SentenceData} object
	 * @return a copy of this {@code SentenceData} object
	 */
	SentenceData clone();

	/**
	 * Returns the {@code form} tokens of the underlying sentence
	 * this {@code SentenceData} object represents (i.e. all
	 * the terminal symbols)
	 * @return the {@code form} tokens of the underlying sentence
	 */
	String getForm(int index);

	Object getProperty(int index, String key);

	/**
	 * Returns {@code true} if this {@code SentenceData} object
	 * represents the empty sentence. This is equivalent with
	 * {@link #getForms()} returning an empty array of {@code String} tokens.
	 * @return {@code true} if and only if the underlying sentence is empty
	 */
	boolean isEmpty();

	/**
	 * Returns the length of the underlying sentence structure, i.e.
	 * the number of terminal tokens in this {@code SentenceData} instance.
	 */
	int length();

	/**
	 * Returns the {@code Grammar} that created this {@code SentenceData}
	 * object or that encapsulates the grammatical rules and structural
	 * terms that define the basis of this {@code SentenceData} object.
	 *
	 * @return the {@code 'grammatical'} source of this {@code SentenceData}
	 * object
	 */
	Grammar getSourceGrammar();
}
