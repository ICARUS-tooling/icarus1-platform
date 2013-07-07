/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency.graph;

import com.mxgraph.model.mxIGraphModel;

import net.ikarus_systems.icarus.language.dependency.DependencyNodeData;
import net.ikarus_systems.icarus.plugins.jgraph.layout.CellMerger;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyCellMerger implements CellMerger {

	public DependencyCellMerger() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.CellMerger#merge(net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void merge(GraphOwner owner, Object parent, Object cell) {
		mxIGraphModel model = owner.getGraph().getModel();
		
		DependencyNodeData nodeData = (DependencyNodeData) model.getValue(cell);
		DependencyNodeData parentData = (DependencyNodeData) model.getValue(parent);
		
		model.beginUpdate();
		try {
			parentData = parentData.clone();
			parentData.addChild(nodeData.clone());
			
			model.setValue(parent, parentData);
		} finally {
			model.endUpdate();
		}
	}

}
