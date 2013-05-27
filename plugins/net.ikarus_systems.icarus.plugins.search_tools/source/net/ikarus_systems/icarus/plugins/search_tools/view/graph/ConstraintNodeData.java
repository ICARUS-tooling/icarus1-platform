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

import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="nodeConstraints")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintNodeData extends ConstraintCellData<ConstraintNodeData> {

	private static final long serialVersionUID = -5561783573729079886L;
	
	@XmlAttribute
	private NodeType nodeType = NodeType.GENERAL;

	public ConstraintNodeData() {
		// no-op
	}

	public ConstraintNodeData(ConstraintNodeData source) {
		copyFrom(source);
	}

	public ConstraintNodeData(int size) {
		constraints = new SearchConstraint[size];
	}
	
	/**
	 * 
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.graph.ConstraintCellData#copyFrom(net.ikarus_systems.icarus.plugins.search_tools.view.graph.ConstraintCellData)
	 */
	@Override
	public void copyFrom(ConstraintNodeData source) {
		negated = source.negated;
		nodeType = source.nodeType;
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

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * 
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.graph.ConstraintCellData#clone()
	 */
	@Override
	public ConstraintNodeData clone() {
		return new ConstraintNodeData(this);
	}
}
