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
public class BottomArcEdgeStyle implements mxEdgeStyleFunction {

	public BottomArcEdgeStyle() {
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
			double base = ArcConnectorShape.getInvertedArcBase(source.getY(), target.getY());

			mxPoint pc = new mxPoint(center, base + ArcConnectorShape.getArcHeight(width));

			result.add(pc);
		}
	}
}
