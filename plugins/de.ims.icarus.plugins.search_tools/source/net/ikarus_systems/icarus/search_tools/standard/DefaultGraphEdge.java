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

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.search_tools.EdgeType;
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
public class DefaultGraphEdge implements SearchEdge {
	
	private static final AtomicInteger idCounter = new AtomicInteger();
	
	@XmlAttribute
	@XmlID
	private String id = "edge_"+idCounter.getAndIncrement(); //$NON-NLS-1$

	@XmlElement
	@XmlJavaTypeAdapter(value=ConstraintAdapter.class)
	private SearchConstraint[] constraints;
	
	@XmlElement
	@XmlIDREF
	@XmlJavaTypeAdapter(value=NodeAdapter.class)
	private SearchNode source;

	@XmlElement
	@XmlIDREF
	@XmlJavaTypeAdapter(value=NodeAdapter.class)
	private SearchNode target;
	
	@XmlAttribute
	private boolean negated = false;
	
	@XmlAttribute
	private EdgeType edgeType = EdgeType.DOMINANCE;
	
	public DefaultGraphEdge(SearchEdge edge) {
		setSource(edge.getSource());
		setTarget(edge.getTarget());
		setConstraints(edge.getConstraints());
		setEdgeType(edge.getEdgeType());
		setNegated(edge.isNegated());
		setId(edge.getId());
	}

	public DefaultGraphEdge(SearchNode source, SearchNode target) {
		setSource(source);
		setTarget(target);
	}

	public DefaultGraphEdge(SearchNode source, SearchNode target, SearchConstraint[] constraints) {
		setSource(source);
		setTarget(target);
		setConstraints(constraints);
	}
	
	public DefaultGraphEdge() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchEdge#getConstraints()
	 */
	@Override
	public SearchConstraint[] getConstraints() {
		return constraints;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchEdge#isNegated()
	 */
	@Override
	public boolean isNegated() {
		return negated;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchEdge#getEdgeType()
	 */
	@Override
	public EdgeType getEdgeType() {
		return edgeType;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchEdge#getSource()
	 */
	@Override
	public SearchNode getSource() {
		return source;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchEdge#getTarget()
	 */
	@Override
	public SearchNode getTarget() {
		return target;
	}

	public void setConstraints(SearchConstraint[] constraints) {
		this.constraints = constraints;
	}

	public void setSource(SearchNode source) {
		if(source==null)
			throw new IllegalArgumentException("Invalid source"); //$NON-NLS-1$
		this.source = source;
	}

	public void setTarget(SearchNode target) {
		if(target==null)
			throw new IllegalArgumentException("Invalid target"); //$NON-NLS-1$
		this.target = target;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public void setEdgeType(EdgeType edgeType) {
		if(edgeType==null)
			throw new IllegalArgumentException("Invalid edgeType"); //$NON-NLS-1$
		this.edgeType = edgeType;
	}

	public String getId() {
		if(id==null || id.isEmpty())
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
