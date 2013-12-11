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
package de.ims.icarus.language.model.mutation;

import de.ims.icarus.language.model.CorpusMember;

/**
 * A {@code Mutator} is the external entry point for user
 * originated modifications of members in a corpus.
 * It allows other client code to request changes to the underlying
 * corpus members. The mutator has to evaluate the conditions of
 * a given request, execute it if possible and notify listeners
 * in the corpus about the changes.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Mutator<C extends CorpusMember> {

	/**
	 * Returns the {@code CorpusMember} this mutator is 
	 * allowed to perform mutations on.
	 * 
	 * @return
	 */
	C getSubject();
}
