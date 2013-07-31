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
