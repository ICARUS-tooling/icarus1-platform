/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.jgraph.util;

import java.util.ArrayList;
import java.util.List;


import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Order;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class GraphUtils {

	private GraphUtils() {
		// no-op
	}

	public static void translatePoint(List<mxPoint> points, int index, mxPoint offset) {
		if (offset != null) {
			mxPoint pt = (mxPoint) points.get(index).clone();
			pt.setX(pt.getX() + offset.getX());
			pt.setY(pt.getY() + offset.getY());
			points.set(index, pt);
		}
	}
	
	public static Object getEdgeValue(GraphOwner owner, Object cell) {
		if(owner.getGraph().getModel().isEdge(cell)) {
			return owner.getGraph().getModel().getValue(cell);
		} else {
			return null;
		}
	}
	
	public static Object getNodeValue(GraphOwner owner, Object cell) {
		if(owner.getGraph().getModel().isVertex(cell)) {
			return owner.getGraph().getModel().getValue(cell);
		} else {
			return null;
		}
	}
	
	public static Object getEdgeValue(mxCellState state) {
		mxIGraphModel model = state.getView().getGraph().getModel();
		return model.isEdge(state.getCell()) ? model.getValue(state.getCell()) : null;
	}
	
	public static Object getNodeValue(mxCellState state) {
		mxIGraphModel model = state.getView().getGraph().getModel();
		return model.isVertex(state.getCell()) ? model.getValue(state.getCell()) : null;
	}

	public static boolean isOrderEdge(mxIGraphModel model, Object edge) {
		return model.isEdge(edge) && model.getValue(edge) instanceof Order;
	}
	
	public static boolean isOrderEdge(GraphOwner owner, mxIGraphModel model, Object cell) {
		if(owner instanceof GraphPresenter) {
			return ((GraphPresenter)owner).isOrderEdge(cell);
		} else {
			return isOrderEdge(model, cell);
		}
	}
	
	public static boolean isLinkEdge(GraphOwner owner, Object cell) {
		if(owner instanceof GraphPresenter) {
			return ((GraphPresenter)owner).isLinkEdge(cell);
		} else {
			return false;
		}
	}
	
	/**
	 * Checks whether some edge is directed from "left" to "right".
	 * Direction is determined by the relative position of the center
	 * points of the two terminal vertices of the edge on the x-axis.
	 */
	public static boolean isLtrEdge(mxIGraphModel model, Object edge) {
		if(!model.isEdge(edge)) {
			return false;
		}
		
		Object source = model.getTerminal(edge, true);
		Object target = model.getTerminal(edge, false);
		
		if(source==null || target==null) {
			return false;
		}
		
		return model.getGeometry(source).getCenterX()<model.getGeometry(target).getCenterX();
	}
	
	public static int getIncomingEdgeCount(mxIGraphModel model, Object cell) {
		return getIncomingEdgeCount(model, cell, false, false);
	}
	
	public static int getIncomingEdgeCount(mxIGraphModel model, Object cell, boolean includeNormal, boolean includeOrder) {
		if(!includeNormal && !includeOrder) {
			includeNormal = includeOrder = true;
		}
		
		int count = 0;
		
		int edgeCount = model.getEdgeCount(cell);
		for(int i=0; i<edgeCount; i++) {
			Object edge = model.getEdgeAt(cell, i);
			boolean isOrder = model.getValue(edge) instanceof Order;
			
			if(isOrder!=includeOrder && isOrder==includeNormal) {
				continue;
			}
			
			if(model.getTerminal(edge, false)==cell) {
				count++;
			}
		}
		
		return count;
	}
	
	public static int getOutgoingEdgeCount(mxIGraphModel model, Object cell) {
		return getOutgoingEdgeCount(model, cell, false, false);
	}
	
	public static int getOutgoingEdgeCount(mxIGraphModel model, Object cell, boolean includeNormal, boolean includeOrder) {
		if(!includeNormal && !includeOrder) {
			includeNormal = includeOrder = true;
		}
		
		int count = 0;
		
		int edgeCount = model.getEdgeCount(cell);
		for(int i=0; i<edgeCount; i++) {
			Object edge = model.getEdgeAt(cell, i);
			boolean isOrder = model.getValue(edge) instanceof Order;
			
			if(isOrder!=includeOrder && isOrder==includeNormal) {
				continue;
			}
			
			if(model.getTerminal(edge, true)==cell) {
				count++;
			}
		}
		
		return count;
	}

	public static Object[] getOrderEdges(mxIGraphModel model, Object cell) {
		return getOrderEdges(model, cell, false, false);
	}
	
	public static Object[] getOrderEdges(mxIGraphModel model, Object cell, boolean outgoing, boolean incoming) {
		if(!outgoing && !incoming) {
			outgoing = incoming = true;
		}
		
		List<Object> items = new ArrayList<>();

		int edgeCount = model.getEdgeCount(cell);
		for(int i=0; i<edgeCount; i++) {
			Object edge = model.getEdgeAt(cell, i);
			
			if(model.getValue(edge) instanceof Order) {
				if((outgoing && model.getTerminal(edge, true)==cell)
						|| (incoming && model.getTerminal(edge, false)==cell)) {
					items.add(cell);
				}
			}
		}
		
		return items.toArray();
	}

	public static Object[] getNonOrderEdges(mxIGraphModel model, Object cell) {
		return getNonOrderEdges(model, cell, false, false);
	}
	
	public static Object[] getNonOrderEdges(mxIGraphModel model, Object cell, boolean outgoing, boolean incoming) {
		if(!outgoing && !incoming) {
			outgoing = incoming = true;
		}
		
		List<Object> items = new ArrayList<>();

		int edgeCount = model.getEdgeCount(cell);
		for(int i=0; i<edgeCount; i++) {
			Object edge = model.getEdgeAt(cell, i);
			
			if(model.getValue(edge) instanceof Order) {
				continue;
			}
			
			if((outgoing && model.getTerminal(edge, true)==cell)
					|| (incoming && model.getTerminal(edge, false)==cell)) {
				items.add(cell);
			}
		}
		
		return items.toArray();
	}
	
	public static Object getAncestor(mxIGraphModel model, Object cell) {
		int edgeCount = model.getEdgeCount(cell);
		for(int i=0; i<edgeCount; i++) {
			Object edge = model.getEdgeAt(cell, i);
			
			if(model.getValue(edge) instanceof Order) {
				continue;
			}
			
			if(model.getTerminal(edge, false)==cell) {
				return model.getTerminal(edge, true);
			}
		}
		
		return null;
	}
	
	public static Object[] getDescendants(mxIGraphModel model, Object cell) {
		List<Object> result = new ArrayList<>();
		
		int edgeCount = model.getEdgeCount(cell);
		for(int i=0; i<edgeCount; i++) {
			Object edge = model.getEdgeAt(cell, i);
			
			if(isOrderEdge(model, edge)) {
				continue;
			}
			
			if(model.getTerminal(edge, true)==cell) {
				result.add(model.getTerminal(edge, false));
			}
		}
		
		return result.toArray();
	}
	
	public static Object[] getDescendants(GraphOwner owner, Object cell) {
		List<Object> result = new ArrayList<>();
		
		mxIGraphModel model = owner.getGraph().getModel();
		int edgeCount = model.getEdgeCount(cell);
		for(int i=0; i<edgeCount; i++) {
			Object edge = model.getEdgeAt(cell, i);
			
			if(isOrderEdge(owner, model, edge)) {
				continue;
			}
			
			if(model.getTerminal(edge, true)==cell) {
				result.add(model.getTerminal(edge, false));
			}
		}
		
		return result.toArray();
	}
	
	public static Object[] getRootCells(mxIGraphModel model, Object[] cells) {
		List<Object> result = new ArrayList<>();
		
		for(Object cell : cells) {
			if(getIncomingEdgeCount(model, cell, true, false)==0) {
				result.add(cell);
			}
		}

		return result.toArray();
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

		graph.moveCells(cells, dx, dy, false);
	}
	
	public static boolean isAncestor(mxIGraphModel model, Object node, 
			Object ancestor, boolean includeNormalEdges, boolean includeOrderEdges) {
		if(!includeNormalEdges && !includeOrderEdges)
			throw new IllegalArgumentException();
		
		if(node==null)
			return false;
		
		if(node==ancestor)
			return true;
		
		Object[] edges = mxGraphModel.getIncomingEdges(model, node);
		boolean isOrderEdge;
		for(Object edge : edges) {
			isOrderEdge = model.getValue(edge) instanceof Order; 
			if((isOrderEdge && !includeOrderEdges) 
					|| (!isOrderEdge && !includeNormalEdges)) {
				continue;
			} else if(isAncestor(model, model.getTerminal(edge, true), ancestor,
					includeNormalEdges, includeOrderEdges)) {
				return true;
			}
		}

		return false;
	}
}
