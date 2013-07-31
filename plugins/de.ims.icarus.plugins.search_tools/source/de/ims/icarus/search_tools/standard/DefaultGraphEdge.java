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

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchNode;


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
	 * @see de.ims.icarus.search_tools.SearchEdge#getConstraints()
	 */
	@Override
	public SearchConstraint[] getConstraints() {
		return constraints;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#isNegated()
	 */
	@Override
	public boolean isNegated() {
		return negated;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#getEdgeType()
	 */
	@Override
	public EdgeType getEdgeType() {
		return edgeType;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#getSource()
	 */
	@Override
	public SearchNode getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#getTarget()
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
