/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.util;

import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.Order;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class GraphUtils {

	private GraphUtils() {
		// no-op
	}

	public static boolean isOrderEdge(mxGraph graph, Object edge) {
		return graph.getModel().getValue(edge) instanceof Order;
	}
	
	/**
	 * Checks whether some edge is directed from "left" to "right".
	 * Direction is determined by the relative position of the center
	 * points of the two terminal vertices of the edge on the x-axis.
	 */
	public static boolean isLtrEdge(mxGraph graph, Object edge) {
		mxIGraphModel model = graph.getModel();
		Object source = model.getTerminal(edge, true);
		Object target = model.getTerminal(edge, false);
		
		if(source==null || target==null) {
			return false;
		}
		
		return model.getGeometry(source).getCenterX()<model.getGeometry(target).getCenterX();
	}

	public static Object[] getAllCells(mxIGraphModel model, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void importCells(mxGraph graph, CellBuffer buffer) {
		Object[] cells = CellBuffer.buildCells(buffer);
		if(cells==null || cells.length==0) {
			return;
		}

		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			graph.addCells(cells);
		} finally {
			model.endUpdate();
		}
	}
	
	public static void clearGraph(mxGraph graph) {
		mxIGraphModel model = graph.getModel();
		if(model instanceof mxGraphModel) {
			((mxGraphModel)model).clear();
		} else {
			Object[] cells = mxGraphModel.getChildren(model, graph.getDefaultParent());
			for(Object cell : cells) {
				model.remove(cell);
			} 
		}
	}
	
	public static void deleteCells(mxGraph graph, Object[] cells) {
		if (cells == null) {
			cells = graph.getSelectionCells();
		}
		if(cells.length==0) {
			return;
		}
		
		graph.getModel().beginUpdate();
		try {
			graph.removeCells(cells);
		} finally {
			graph.getModel().endUpdate();
		}
	}
	
	public static void moveCells(mxGraph graph, Object[] cells, double dx, double dy) {
		if(dx==0 &&dy==0) {
			return;
		}
		if (cells == null) {
			cells = graph.getSelectionCells();
		}
		if(cells.length==0) {
			return;
		}

		graph.moveCells(cells, dx, dy);
	}
	public static boolean isAncestor(mxGraph graph, Object node, 
			Object ancestor, boolean includeNormalEdges, boolean includeOrderEdges) {
		if(!includeNormalEdges && !includeOrderEdges)
			throw new IllegalArgumentException();
		
		if(node==null)
			return false;
		
		if(node==ancestor)
			return true;
		
		Object[] edges = graph.getIncomingEdges(node);
		boolean isOrderEdge;
		for(Object edge : edges) {
			isOrderEdge = graph.getModel().getValue(edge) instanceof Order; 
			if((isOrderEdge && !includeOrderEdges) 
					|| (!isOrderEdge && !includeNormalEdges)) {
				continue;
			} else if(isAncestor(graph, graph.getModel().getTerminal(edge, true), ancestor,
					includeNormalEdges, includeOrderEdges)) {
				return true;
			}
		}

		return false;
	}
}
