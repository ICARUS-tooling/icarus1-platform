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

import de.ims.icarus.model.api.members.Markable;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CandidateFilter {

	/**
	 * Returns an <i>estimated</i> strength of this filter implementation,
	 * taking into account the aspects used to filter candidates and the
	 * properties of the target corpus/segment.
	 * <p>
	 * The returned value is to be located in the closed interval <tt>0.0 ... 1.0</tt>
	 * with {@code 0.0} meaning the filter is insufficient to be used as an early
	 * discard mechanism or it it very expensive to run. Increased values up
	 * to the maximum of {@code 1.0} signal better suitability for singling out candidates
	 * and therefore will result in a higher hierarchical position for the filter. The
	 * engine sorts filters according to their strength in descending order, so that strong
	 * filters, that are more likely to discard candidates or cheap to use, get applied first.
	 * <p>
	 * Note that when an implementation is unable to estimate or guess its own filter quality,
	 * it should return the general default value of {@code 0.5}.
	 *
	 * @return
	 */
	double getFilterStrength(QueryContext context);

	boolean accepts(Markable candidate);
}
