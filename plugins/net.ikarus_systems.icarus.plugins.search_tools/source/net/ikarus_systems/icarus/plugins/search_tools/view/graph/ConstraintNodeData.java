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

import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.standard.ConstraintAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintNodeData implements Serializable {

	private static final long serialVersionUID = -5561783573729079886L;

	@XmlAttribute(required=false)
	private boolean negated = false;
	
	@XmlAttribute(required=false)
	private boolean root = false;
	
	@XmlElement
	@XmlJavaTypeAdapter(value=ConstraintAdapter.class)
	private SearchConstraint[] constraints;

	public ConstraintNodeData() {
		// no-op
	}

	public ConstraintNodeData(ConstraintNodeData source) {
		copyFrom(source);
	}

	public ConstraintNodeData(int size) {
		constraints = new SearchConstraint[size];
	}
	
	public void setConstraint(int index, SearchConstraint constraint) {
		constraints[index] = constraint;
	}
	
	public void copyFrom(ConstraintNodeData source) {
		negated = source.negated;
		root = source.root;
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
	public ConstraintNodeData clone() {
		return new ConstraintNodeData(this);
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
	
	public int getConstraintCount() {
		return constraints==null ? 0 : constraints.length;
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean isRoot) {
		this.root = isRoot;
	}
}
