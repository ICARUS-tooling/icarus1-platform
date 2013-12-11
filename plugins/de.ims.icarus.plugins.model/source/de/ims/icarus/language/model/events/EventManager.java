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
package de.ims.icarus.language.model.events;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface EventManager {

	
	/**
	 * Registers the given listener to the internal list of registered
	 * listeners. Does nothing if the provided listener is {@code null}.
	 * Note that implementations should make sure that no listener is
	 * registered more than once. Typically this means doubling the cost
	 * of registration. Since it is not to be expected that registrations
	 * occur extremely frequent, this can be ignored.
	 * 
	 * @param l The listener to be registered, may be {@code null}
	 */
	void addCorpusListener(CorpusListener l);
	
	/**
	 * Unregisters the given listener from the internal list of registered
	 * listeners. Does nothing if the provided listener is {@code null}.
	 * @param l The listener to be unregistered, may be {@code null}
	 */
	void removeCorpusListener(CorpusListener l);
	
	void fireCorpusChanged(CorpusEvent e);
	
	// TODO add fireXXX methods for all methods in the CorpusListener interface!
}
