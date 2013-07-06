/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools.view.graph;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.util.SearchUtils;


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

	public ConstraintEdgeData(SearchEdge source) {
		copyFrom(source);
	}

	public ConstraintEdgeData(int size) {
		constraints = new SearchConstraint[size];
	}
	
	/**
	 * 
	 * @see de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData#copyFrom(de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData)
	 */
	@Override
	public void copyFrom(ConstraintEdgeData source) {
		negated = source.negated;
		edgeType = source.edgeType;
		id = source.id;
		constraints = SearchUtils.cloneConstraints(source.constraints);
	}

	public void copyFrom(SearchEdge source) {
		negated = source.isNegated();
		edgeType = source.getEdgeType();
		id = source.getId();
		constraints = SearchUtils.cloneConstraints(source.getConstraints());
	}

	/**
	 * 
	 * @see de.ims.icarus.plugins.search_tools.view.graph.ConstraintCellData#clone()
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
