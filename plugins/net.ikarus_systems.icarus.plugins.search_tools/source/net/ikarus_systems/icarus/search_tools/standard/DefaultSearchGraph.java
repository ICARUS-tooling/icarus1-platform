/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="searchGraph")
public class DefaultSearchGraph implements SearchGraph {
	
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
	 * @see net.ikarus_systems.icarus.search_tools.SearchGraph#getNodes()
	 */
	@Override
	public SearchNode[] getNodes() {
		return nodes;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchGraph#getEdges()
	 */
	@Override
	public SearchEdge[] getEdges() {
		return edges;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchGraph#getRootNodes()
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
	
	public SearchGraph clone() {
		DefaultSearchGraph graph = new DefaultSearchGraph();
		graph.rootOperator = rootOperator;
		graph.nodes = nodes==null ? null : nodes.clone();
		graph.edges = edges==null ? null : edges.clone();
		graph.rootNodes = rootNodes==null ? null : rootNodes.clone();
		return graph;
	}
}