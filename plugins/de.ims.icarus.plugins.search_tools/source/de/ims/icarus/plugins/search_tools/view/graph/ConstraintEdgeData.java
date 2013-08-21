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
package de.ims.icarus.plugins.search_tools.view.graph;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.util.CollectionUtils;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="edgeConstraints")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintEdgeData extends ConstraintCellData<ConstraintEdgeData> {

	private static final long serialVersionUID = 3694619821568182574L;

	@XmlAttribute
	private EdgeType edgeType = EdgeType.DOMINANCE;

	public ConstraintEdgeData() {
		// no-op
	}

	public ConstraintEdgeData(ConstraintEdgeData source) {
		copyFrom(source);
	}

	public ConstraintEdgeData(SearchEdge source) {
		copyFrom(source);
	}

	public ConstraintEdgeData(int size) {
		constraints = new ArrayList<>(size);
	}
	
	/**
	 * 
	 * @see de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData#copyFrom(de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData)
	 */
	@Override
	public void copyFrom(ConstraintEdgeData source) {
		negated = source.negated;
		edgeType = source.edgeType;
		id = source.id;
		constraints = SearchUtils.cloneConstraints(source.constraints);
	}

	public void copyFrom(SearchEdge source) {
		negated = source.isNegated();
		edgeType = source.getEdgeType();
		id = source.getId();
		constraints = CollectionUtils.asList(SearchUtils.cloneConstraints(source.getConstraints()));
	}

	/**
	 * 
	 * @see de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData#clone()
	 */
	@Override
	public ConstraintEdgeData clone() {
		return new ConstraintEdgeData(this);
	}

	public EdgeType getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(EdgeType edgeType) {
		this.edgeType = edgeType;
	}
}
