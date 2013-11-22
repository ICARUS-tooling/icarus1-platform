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

import de.ims.icarus.search_tools.NodeType;
import de.ims.icarus.search_tools.SearchNode;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.util.collections.CollectionUtils;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="nodeConstraints")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintNodeData extends ConstraintCellData<ConstraintNodeData> {

	private static final long serialVersionUID = -5561783573729079886L;
	
	@XmlAttribute
	private NodeType nodeType = NodeType.GENERAL;

	public ConstraintNodeData() {
		// no-op
	}

	public ConstraintNodeData(ConstraintNodeData source) {
		copyFrom(source);
	}

	public ConstraintNodeData(SearchNode source) {
		copyFrom(source);
	}

	public ConstraintNodeData(int size) {
		constraints = new ArrayList<>(size);
	}
	
	/**
	 * 
	 * @see de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData#copyFrom(de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData)
	 */
	@Override
	public void copyFrom(ConstraintNodeData source) {
		negated = source.negated;
		nodeType = source.nodeType;
		id = source.id;
		constraints = SearchUtils.cloneConstraints(source.constraints);
	}
	
	public void copyFrom(SearchNode source) {
		negated = source.isNegated();
		nodeType = source.getNodeType();
		id = source.getId();
		constraints = CollectionUtils.asList(SearchUtils.cloneConstraints(source.getConstraints()));
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * 
	 * @see de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData#clone()
	 */
	@Override
	public ConstraintNodeData clone() {
		return new ConstraintNodeData(this);
	}
}
