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

import java.util.Collection;

import org.java.plugin.registry.Extension;

import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.Order;

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
		return model.getGeometry(source).getCenterX()<model.getGeometry(target).getCenterX();
	}

	public static Object[] getAllCells(mxIGraphModel model, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}
}
