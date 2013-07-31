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

/**
 * 
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Grammar {
	
	/**
	 * Returns an identifier for this {@code Grammar}
	 * @return
	 */
	String getIdentifier();

	/**
	 * 
	 * @return an empty {@code SentenceData} object
	 */
	SentenceData createEmptySentenceData();

	
	SentenceData createExampleSentenceData();
	
	/**
	 * Returns the used super class for {@code SentenceData}
	 * objects returned from this {@code Grammar}. Typically
	 * this will be an {@code interface}.
	 * @return the super class of {@code SentenceData} objects
	 * returned by this {@code Grammar}
	 */
	Class<? extends SentenceData> getBaseClass();
}
