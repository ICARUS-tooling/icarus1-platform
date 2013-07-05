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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class DefaultGraphNode implements SearchNode {
	
	private static final AtomicInteger idCounter = new AtomicInteger();
	
	@XmlID
	@XmlAttribute
	private String id = "node_"+idCounter.getAndIncrement(); //$NON-NLS-1$
	
	@XmlAttribute
	private NodeType nodeType = NodeType.GENERAL;
	
	@XmlElement
	@XmlJavaTypeAdapter(value=ConstraintAdapter.class)
	private SearchConstraint[] constraints;
	
	@XmlElement
	@XmlIDREF
	@XmlJavaTypeAdapter(value=EdgeAdapter.class)
	private List<SearchEdge> incomingEdges;
	
	@XmlElement
	@XmlIDREF
	@XmlJavaTypeAdapter(value=EdgeAdapter.class)
	private List<SearchEdge> outgoingEdges;
	
	@XmlAttribute
	private boolean negated;
	
	@XmlTransient
	private int height = -1;
	
	@XmlTransient
	private int descendantCount = -1;
	
	private int childCount = -1;

	public DefaultGraphNode() {
		// no-op
	}
	
	public DefaultGraphNode(SearchNode node) {
		setId(node.getId());
		setNegated(node.isNegated());

		// TODO maintain source value of edge
		for(int i=0; i<node.getIncomingEdgeCount(); i++) {
			addEdge(node.getIncomingEdgeAt(i), true);
		}

		// TODO maintain source value of edge
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			addEdge(node.getOutgoingEdgeAt(i), false);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getConstraints()
	 */
	@Override
	public SearchConstraint[] getConstraints() {
		return constraints;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#isNegated()
	 */
	@Override
	public boolean isNegated() {
		return negated;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getOutgoingEdgeCount()
	 */
	@Override
	public int getOutgoingEdgeCount() {
		return outgoingEdges==null ? 0 : outgoingEdges.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getOutgoingEdgeAt(int)
	 */
	@Override
	public SearchEdge getOutgoingEdgeAt(int index) {
		return outgoingEdges.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getIncomingEdgeCount()
	 */
	@Override
	public int getIncomingEdgeCount() {
		return incomingEdges==null ? 0 : incomingEdges.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getIncomingEdgeAt(int)
	 */
	@Override
	public SearchEdge getIncomingEdgeAt(int index) {
		return incomingEdges.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getHeight()
	 */
	@Override
	public int getHeight() {
		
		if(height==-1) {
			int value = 0;
			
			if(outgoingEdges!=null) {
				for(SearchEdge edge : outgoingEdges) {
					value = Math.max(value, edge.getTarget().getHeight());
				}
			}
			
			height = value + 1;
		}
		
		return height;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getDescendantCount()
	 */
	@Override
	public int getDescendantCount() {
		
		if(descendantCount==-1) {
			int value = 0;
			
			if(outgoingEdges!=null) {
				value = outgoingEdges.size();
				for(SearchEdge edge : outgoingEdges) {
					value += edge.getTarget().getDescendantCount();
				}
			}
			descendantCount = value;
		}
		
		return descendantCount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setConstraints(SearchConstraint[] constraints) {
		this.constraints = constraints;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	/**
	 * 
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getNodeType()
	 */
	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
		
		childCount = -1;
	}

	public void addEdge(SearchEdge edge, boolean incoming) {
		if(edge==null)
			throw new IllegalArgumentException("Invalid edge"); //$NON-NLS-1$
		
		if(incoming) {
			if(incomingEdges==null) {
				incomingEdges = new ArrayList<>();
			}
			incomingEdges.add(edge);
		} else {
			if(outgoingEdges==null) {
				outgoingEdges = new ArrayList<>();
			}
			outgoingEdges.add(edge);
		}
		
		height = -1;
		descendantCount = -1;
		childCount = -1;
	}
	
	public void addEdges(Collection<SearchEdge> newEdges, boolean incoming) {
		if(newEdges==null)
			throw new IllegalArgumentException("Invalid outgoingEdges"); //$NON-NLS-1$
		if(newEdges.isEmpty()) {
			return;
		}
		
		if(incoming) {
			if(incomingEdges==null) {
				incomingEdges = new ArrayList<>();
			}
			incomingEdges.addAll(newEdges);
		} else {
			if(outgoingEdges==null) {
				outgoingEdges = new ArrayList<>();
			}
			outgoingEdges.addAll(outgoingEdges);
		}
		
		height = -1;
		descendantCount = -1;
		childCount = -1;
	}
	
	public void sortEdges(Comparator<SearchEdge> comparator) {
		if(comparator==null)
			throw new IllegalArgumentException("Invalid comparator"); //$NON-NLS-1$
		
		Collections.sort(outgoingEdges, comparator);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getChildCount()
	 */
	@Override
	public int getChildCount() {
		
		if(childCount==-1) {
			if(nodeType==NodeType.LEAF || negated 
					|| outgoingEdges==null || outgoingEdges.isEmpty()) {
				childCount = 0;
			} else if(nodeType==NodeType.DISJUNCTION) {
				childCount = 1;

				for(SearchEdge edge : outgoingEdges) {
					if(edge.getTarget().isNegated()) {
						childCount = 0;
						break;
					}
				}
			} else {
				childCount = 0;
				for(SearchEdge edge : outgoingEdges) {
					SearchNode node = edge.getTarget();
					if(node.getNodeType()==NodeType.DISJUNCTION) {
						childCount += node.getChildCount();
					} else {
						childCount++;
					}
				}
			}
		}
		
		return childCount;
	}
}
