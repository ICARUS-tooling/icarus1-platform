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
public class DefaultTreeNode implements SearchNode {
	
	private static final AtomicInteger idCounter = new AtomicInteger();
	
	@XmlID
	@XmlAttribute
	private String id = "node_"+idCounter.getAndIncrement(); //$NON-NLS-1$
	
	@XmlElement
	@XmlJavaTypeAdapter(value=ConstraintAdapter.class)
	private SearchConstraint[] constraints;

	@XmlElement
	@XmlIDREF
	@XmlJavaTypeAdapter(value=EdgeAdapter.class)
	private SearchEdge head;
	
	@XmlElement
	@XmlIDREF
	@XmlJavaTypeAdapter(value=EdgeAdapter.class)
	private List<SearchEdge> edges;
	
	@XmlAttribute
	private boolean negated;
	
	@XmlTransient
	private int height = -1;
	
	@XmlTransient
	private int descendantCount = -1;

	public DefaultTreeNode() {
		// no-op
	}
	
	public DefaultTreeNode(SearchNode node) {
		setId(node.getId());
		setNegated(node.isNegated());
		
		if(node.getIncomingEdgeCount()>0) {
			setHead(node.getIncomingEdgeAt(0));
		}
		
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			addEdge(node.getOutgoingEdgeAt(i));
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
		return edges==null ? 0 : edges.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getOutgoingEdgeAt(int)
	 */
	@Override
	public SearchEdge getOutgoingEdgeAt(int index) {
		return edges.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getIncomingEdgeCount()
	 */
	@Override
	public int getIncomingEdgeCount() {
		return head==null ? 0 : 1;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getIncomingEdgeAt(int)
	 */
	@Override
	public SearchEdge getIncomingEdgeAt(int index) {
		if(head==null)
			throw new IllegalArgumentException("Node has no head"); //$NON-NLS-1$
		return head;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#isLeafNode()
	 */
	@Override
	public boolean isLeafNode() {
		return edges==null || edges.isEmpty();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#isRootNode()
	 */
	@Override
	public boolean isRootNode() {
		return head==null;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchNode#getHeight()
	 */
	@Override
	public int getHeight() {
		
		if(height==-1) {
			int value = 0;
			
			if(edges!=null) {
				for(SearchEdge edge : edges) {
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
			
			if(edges!=null) {
				value = edges.size();
				for(SearchEdge edge : edges) {
					value += edge.getTarget().getDescendantCount();
				}
			}
			descendantCount = value;
		}
		
		return descendantCount;
	}

	public SearchEdge getHead() {
		return head;
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

	public void setHead(SearchEdge head) {
		this.head = head;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	public void addEdge(SearchEdge edge) {
		if(edge==null)
			throw new IllegalArgumentException("Invalid edge"); //$NON-NLS-1$
		
		if(edges==null) {
			edges = new ArrayList<>();
		}
		
		edges.add(edge);
		
		height = -1;
		descendantCount = -1;
	}
	
	public void addEdges(Collection<SearchEdge> newEdges) {
		if(newEdges==null)
			throw new IllegalArgumentException("Invalid edges"); //$NON-NLS-1$
		if(newEdges.isEmpty()) {
			return;
		}
		
		if(edges==null) {
			edges = new ArrayList<>();
		}
		
		edges.addAll(edges);
		
		height = -1;
		descendantCount = -1;
	}
	
	public void sortEdges(Comparator<SearchEdge> comparator) {
		if(comparator==null)
			throw new IllegalArgumentException("Invalid comparator"); //$NON-NLS-1$
		
		Collections.sort(edges, comparator);
	}
}
