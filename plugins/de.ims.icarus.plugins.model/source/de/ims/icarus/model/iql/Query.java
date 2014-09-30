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
package de.ims.icarus.model.iql;

import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Scope;
import de.ims.icarus.model.api.driver.IndexSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Query {

	/**
	 * Creates a new query object that holds all the constraints and
	 * information of this query instance, minus all the data, that
	 * has not been introduced by means of a {@link Context} definition.
	 * This includes (but is not limited to) for example {@link Function},
	 * {@link Predicate} or {link Relation} objects or types of <i>inter-layer</i>
	 * edges.
	 */
	Query simplify();

	/**
	 * If based on another (potentially more complex or more general) query object
	 * this method returns that source query.
	 *
	 * @return
	 */
	Query getSource();

	/**
	 * If the query is created as the result of a previous request to a corpus and intended
	 * for further processing, it might hold a suggested candidate list. Note that this method
	 * will only return a valid {@code IndexSet} in case the {@link #getSource()} method returns
	 * a non-null source query and a candidate list was provided to the creation of this query.
	 *
	 * @return
	 * @throws IllegalStateException if the query is not derived from another request and therefore
	 * cannot provide a list of pre-filtered candidates
	 */
	IndexSet[] getCandidates();

	Scope getResultScope();

	Query clone();

	@Override
	String toString();
}
