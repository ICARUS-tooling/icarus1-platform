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
	
	int getConstraintCount() {
		return constraints==null ? 0 : constraints.length;
	}
}
