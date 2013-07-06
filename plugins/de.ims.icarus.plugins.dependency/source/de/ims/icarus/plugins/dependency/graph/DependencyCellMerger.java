/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.dependency.graph;

import com.mxgraph.model.mxIGraphModel;

import de.ims.icarus.language.dependency.DependencyNodeData;
import de.ims.icarus.plugins.jgraph.layout.CellMerger;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;


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
	 * @see de.ims.icarus.plugins.jgraph.layout.CellMerger#merge(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object, java.lang.Object)
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
