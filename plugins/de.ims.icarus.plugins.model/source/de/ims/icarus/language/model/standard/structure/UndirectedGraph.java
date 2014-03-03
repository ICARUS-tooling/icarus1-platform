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
package de.ims.icarus.language.model.standard.structure;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Edge;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.StructureType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UndirectedGraph extends DirectedGraph {

	public UndirectedGraph(long id, Container parent, Container boundary,
			boolean augment, boolean boundaryAsBase) {
		super(id, parent, boundary, augment, boundaryAsBase);
	}

	public UndirectedGraph(long id, Container parent) {
		super(id, parent);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.structure.EmptyStructure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.GRAPH;
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.structure.AbstractRootedStructure#createEdge(de.ims.icarus.language.model.api.Markable, de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	protected Edge createEdge(Markable source, Markable target) {
		long id = getCorpus().getGlobalIdDomain().nextId();
		return new UndirectedEdge(id, this, source, target);
	}
}
