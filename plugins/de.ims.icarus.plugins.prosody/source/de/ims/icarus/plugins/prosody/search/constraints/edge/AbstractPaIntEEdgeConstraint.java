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
package de.ims.icarus.plugins.prosody.search.constraints.edge;

import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.painte.AggregationMode;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultConstraint;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractPaIntEEdgeConstraint extends DefaultConstraint {

	private static final long serialVersionUID = -2399416566414278372L;

	// Buffer to be used while aggregating
	protected transient final PaIntEConstraintParams valueParams = new PaIntEConstraintParams();

	// Aggregated parameter set for the source node
	protected transient final PaIntEConstraintParams sourceParams = new PaIntEConstraintParams();

	// Aggregated parameter set for the target node
	protected transient final PaIntEConstraintParams targetParams = new PaIntEConstraintParams();

	protected AggregationMode aggregationMode = AggregationMode.maxValue;

	protected AbstractPaIntEEdgeConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object value) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		int target = tree.getNodeIndex();
		int source = tree.getParentIndex();

		// Target data
		if(tree.hasSyllables()) {
			getAggregate(tree, targetParams);
		} else {
			targetParams.clear();
		}

		// Source data
		tree.viewNode(source);
		if(tree.hasSyllables()) {
			getAggregate(tree, sourceParams);
		} else {
			sourceParams.clear();
		}

		// Move cursor back to target node!!!
		tree.viewNode(target);

		// Now do the real matching

		return matches(sourceParams, targetParams);
	}

	protected abstract boolean matches(PaIntEConstraintParams sourceParams, PaIntEConstraintParams targetParams);

	/**
	 * Creates an aggregated version of the data in the currently inspected node of {@code tree}
	 * and saves it in the specified {@code params}.
	 */
	protected abstract void getAggregate(ProsodyTargetTree tree, PaIntEConstraintParams params);
}
