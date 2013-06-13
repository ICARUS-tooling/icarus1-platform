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
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.standard.ConstraintAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ConstraintCellData<E extends ConstraintCellData<E>> implements Serializable {

	private static final long serialVersionUID = -3036452029676803967L;

	@XmlAttribute(required=false)
	boolean negated = false;
	
	@XmlElement
	@XmlJavaTypeAdapter(value=ConstraintAdapter.class)
	SearchConstraint[] constraints;
	
	@XmlTransient
	String id = "<undefined>"; //$NON-NLS-1$
	
	protected ConstraintCellData() {
		// no-op
	}

	@Override
	public abstract E clone();
	
	public abstract void copyFrom(E source);

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	public void setConstraint(int index, SearchConstraint constraint) {
		constraints[index] = constraint;
	}

	public SearchConstraint[] getConstraints() {
		return constraints;
	}

	public void setConstraints(SearchConstraint[] constraints) {
		this.constraints = constraints;
	}
	
	public void setConstraints(SearchConstraint[] constraints, Map<String, Integer> constraintMap) {
		if(this.constraints==null)
			throw new IllegalStateException("Cannot assign constraints - not initialized"); //$NON-NLS-1$
		
		if(constraints==null || constraints.length==0) {
			return;
		}
		
		for(SearchConstraint constraint : constraints) {
			int index = constraintMap.get(constraint.getToken());
			this.constraints[index] = constraint;
		}
	}
	
	int getConstraintCount() {
		return constraints==null ? 0 : constraints.length;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
