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
package de.ims.icarus.search_tools.standard;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchNode;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="searchGraph")
public class DefaultSearchGraph implements SearchGraph, Cloneable {

	@XmlAttribute
	private int rootOperator = OPERATOR_CONJUNCTION;

	@XmlElement
	@XmlJavaTypeAdapter(value=NodeAdapter.class)
	private SearchNode[] nodes;

	@XmlElement
	@XmlJavaTypeAdapter(value=EdgeAdapter.class)
	private SearchEdge[] edges;

	@XmlElement
	@XmlIDREF
	@XmlJavaTypeAdapter(value=NodeAdapter.class)
	private SearchNode[] rootNodes;

	public DefaultSearchGraph() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchGraph#getNodes()
	 */
	@Override
	public SearchNode[] getNodes() {
		return nodes;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchGraph#getEdges()
	 */
	@Override
	public SearchEdge[] getEdges() {
		return edges;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchGraph#getRootNodes()
	 */
	@Override
	public SearchNode[] getRootNodes() {
		return rootNodes;
	}

	public void setNodes(SearchNode[] nodes) {
		this.nodes = nodes;
	}

	public void setEdges(SearchEdge[] edges) {
		this.edges = edges;
	}

	public void setRootNodes(SearchNode[] rootNodes) {
		this.rootNodes = rootNodes;
	}

	@Override
	public int getRootOperator() {
		return rootOperator;
	}

	public void setRootOperator(int rootOperator) {
		this.rootOperator = rootOperator;
	}

	@Override
	public SearchGraph clone() {
		DefaultSearchGraph graph = new DefaultSearchGraph();
		graph.rootOperator = rootOperator;
		graph.nodes = nodes==null ? null : nodes.clone();
		graph.edges = edges==null ? null : edges.clone();
		graph.rootNodes = rootNodes==null ? null : rootNodes.clone();
		return graph;
	}
}