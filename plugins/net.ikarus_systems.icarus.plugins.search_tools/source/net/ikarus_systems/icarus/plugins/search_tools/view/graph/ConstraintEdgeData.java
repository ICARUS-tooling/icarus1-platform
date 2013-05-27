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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="edgeConstraints")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintEdgeData extends ConstraintCellData<ConstraintEdgeData> {

	private static final long serialVersionUID = 3694619821568182574L;

	@XmlAttribute
	private EdgeType edgeType = EdgeType.DOMINANCE;

	public ConstraintEdgeData() {
		// no-op
	}

	public ConstraintEdgeData(ConstraintEdgeData source) {
		copyFrom(source);
	}

	public ConstraintEdgeData(int size) {
		constraints = new SearchConstraint[size];
	}
	
	/**
	 * 
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.graph.ConstraintCellData#copyFrom(net.ikarus_systems.icarus.plugins.search_tools.view.graph.ConstraintCellData)
	 */
	@Override
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

	/**
	 * 
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.graph.ConstraintCellData#clone()
	 */
	@Override
	public ConstraintEdgeData clone() {
		return new ConstraintEdgeData(this);
	}

	public EdgeType getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(EdgeType edgeType) {
		this.edgeType = edgeType;
	}
}
