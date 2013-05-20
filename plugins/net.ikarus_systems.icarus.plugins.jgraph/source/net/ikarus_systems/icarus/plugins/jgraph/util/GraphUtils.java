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

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;
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

	public static boolean isOrderEdge(mxIGraphModel model, Object edge) {
		return model.getValue(edge) instanceof Order;
	}
	
	/**
	 * Checks whether some edge is directed from "left" to "right".
	 * Direction is determined by the relative position of the center
	 * points of the two terminal vertices of the edge on the x-axis.
	 */
	public static boolean isLtrEdge(mxIGraphModel model, Object edge) {
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
			
			if(model.getValue(edge) instanceof Order) {
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

	public static ListCellRenderer<Object> fontFamilyRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = -333044504694220350L;
		
		private Map<String, Font> fonts;
		
		private Font getFont(String name) {
			if(fonts==null)
				fonts = new Hashtable<String, Font>();
			
			Font font = fonts.get(name);
			
			if(font==null) {
				font = getFont();
				font = new Font(name, font.getStyle(), font.getSize());
				fonts.put(name, font);
			}
			
			return font;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			setFont(getFont((String) value));
			
			return this;
		}
	};
	
	public static ListCellRenderer<?> shapeRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = -5300412794404495530L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			String shape = (String)value;
			shape = shape.replace("Perimeter", ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			setIcon(IconRegistry.getGlobalRegistry().getIcon(String.format("shape_%s.gif", shape))); //$NON-NLS-1$
			setText(ResourceManager.getInstance().get("config.options."+shape)); //$NON-NLS-1$
			
			return this;
		}
	};
	
	public static ListCellRenderer<?> gridStyleRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = -5300412794404495530L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			/*
			 * 	public static final int GRID_STYLE_DOT = 0;
			 *	public static final int GRID_STYLE_CROSS = 1;
			 *	public static final int GRID_STYLE_LINE = 2;
			 *  public static final int GRID_STYLE_DASHED = 3;
			 */
			switch ((Integer)value) {
			case 0:
				setText(ResourceManager.getInstance().get("config.options.gridStyleDot")); //$NON-NLS-1$
				setIcon(IconRegistry.getGlobalRegistry().getIcon("grid_dot.gif")); //$NON-NLS-1$
				break;

			case 1:
				setText(ResourceManager.getInstance().get("config.options.gridStyleCross")); //$NON-NLS-1$
				setIcon(IconRegistry.getGlobalRegistry().getIcon("grid_cross.gif")); //$NON-NLS-1$
				break;

			case 2:
				setText(ResourceManager.getInstance().get("config.options.gridStyleLine")); //$NON-NLS-1$
				setIcon(IconRegistry.getGlobalRegistry().getIcon("grid_line.gif")); //$NON-NLS-1$
				break;

			case 3:
				setText(ResourceManager.getInstance().get("config.options.gridStyleDashed")); //$NON-NLS-1$
				setIcon(IconRegistry.getGlobalRegistry().getIcon("grid_dashed.gif")); //$NON-NLS-1$
				break;

			default:
				break;
			}
			
			
			return this; 
		}
	};
}
