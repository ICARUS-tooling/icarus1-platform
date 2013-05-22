/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.graph;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.standard.ConstraintAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintEdgeData implements Serializable {

	private static final long serialVersionUID = 3694619821568182574L;

	@XmlAttribute(required=false)
	private boolean negated = false;

	@XmlAttribute
	private EdgeType edgeType = EdgeType.DOMINANCE;
	
	@XmlElement
	@XmlJavaTypeAdapter(value=ConstraintAdapter.class)
	private SearchConstraint[] constraints;

	public ConstraintEdgeData() {
	}

	public ConstraintEdgeData(ConstraintEdgeData source) {
		copyFrom(source);
	}

	public ConstraintEdgeData(int size) {
		constraints = new SearchConstraint[size];
	}
	
	public void setConstraint(int index, SearchConstraint constraint) {
		constraints[index] = constraint;
	}
	
	public void copyFrom(ConstraintEdgeData source) {
		negated = source.negated;
		edgeType = source.edgeType;
		constraints = null;
		
		if(source.constraints!=null) {
			int size = source.constraints.length;
			SearchConstraint[] newConstraints = new SearchConstraint[size];
			for(int i=0; i<size; i++) {
				newConstraints[i] = source.constraints[i].clone();
			}
			constraints = newConstraints;
		}
	}

	@Override
	public ConstraintEdgeData clone() {
		return new ConstraintEdgeData(this);
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public SearchConstraint[] getConstraints() {
		return constraints;
	}

	public void setConstraints(SearchConstraint[] constraints) {
		this.constraints = constraints;
	}

	public EdgeType getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(EdgeType edgeType) {
		this.edgeType = edgeType;
	}
	
	public int getConstraintCount() {
		return constraints==null ? 0 : constraints.length;
	}
}
