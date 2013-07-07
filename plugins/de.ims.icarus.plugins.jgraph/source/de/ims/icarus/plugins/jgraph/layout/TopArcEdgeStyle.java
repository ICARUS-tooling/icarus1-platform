/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.jgraph.layout;

import java.util.List;

import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle.mxEdgeStyleFunction;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TopArcEdgeStyle implements mxEdgeStyleFunction {

	public TopArcEdgeStyle() {
		// no-op
	}

	@Override
	public void apply(mxCellState state, mxCellState source,
			mxCellState target, List<mxPoint> points, List<mxPoint> result) {
		mxPoint ps = state.getAbsolutePoint(0);
		mxPoint pe = state.getAbsolutePoint(state.getAbsolutePointCount() - 1);

		if (ps != null) {
			source = new mxCellState();
			source.setX(ps.getX());
			source.setY(ps.getY());
		}

		if (pe != null) {
			target = new mxCellState();
			target.setX(pe.getX());
			target.setY(pe.getY());
		}

		if (target != null && source != null) {
			double width = Math.abs(target.getX() - source.getX());
			double center = (target.getX() + source.getX()) * 0.5;
			double base = ArcConnectorShape.getArcBase(source.getY(), target.getY());

			mxPoint pc = new mxPoint(center, base - ArcConnectorShape.getArcHeight(width));

			result.add(pc);
		}
	}
}
