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

import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.util.List;
import java.util.Map;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxConnectorShape;
import com.mxgraph.util.mxPoint;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ArcConnectorShape extends mxConnectorShape implements GraphLayoutConstants {

	public static final double getArcHeight(double width) {
		return width * ARC_HEIGHT_RATIO;
	}

	public static final double getArcBase(double y1, double y2) {
		return Math.min(y1, y2) - ARC_BASE_OFFSET;
	}

	public static final double getInvertedArcBase(double y1, double y2) {
		return Math.max(y1, y2) + ARC_BASE_OFFSET;
	}
	
	public ArcConnectorShape() {
		// no-op
	}

	@Override
	protected void paintPolyline(mxGraphics2DCanvas canvas,
			List<mxPoint> pts, Map<String, Object> style) {

		Graphics2D g = canvas.getGraphics();
		
		mxPoint ps = pts.get(0);
		mxPoint pe = pts.get(pts.size() - 1);
		if (ps.getX() > pe.getX()) {
			mxPoint tmp = ps;
			ps = pe;
			pe = tmp;
		}
		
		if (pts.size() > 2) {
			mxPoint pc = pts.get(pts.size() / 2);
			double ctrlY;

			double base = ps.getY() < pe.getY() ? ps.getY() : pe.getY();
			if(pc.getY()<base) {
				ctrlY = pc.getY() - (base - pc.getY())
						+ ARC_BASE_OFFSET;
			} else {
				ctrlY = base + (pc.getY() - base)
						- ARC_BASE_OFFSET;
			}
			
			ctrlY += ARC_TOP_EXTEND;
			
			// FIXME very strange bug, see comment below
			
			// When commenting out the above block and replacing the 
			// curve creation with it the rendering produces a single
			// horizontal line on a particular dependency data in every
			// graph. (index 258 in conll09 english-train corpus)
			// SENTENCE: One , co - sponsored by Sen. Sam Nunn ( D. , Ga . ) and Rep. Dave McCurdy ( D. , Okla. ) , would have restricted federal college subsidies to students who had served .
			//
			// Apparently incrementing the ctrlY value by one or simply 
			// rounding down all coordinates solves the issue (needs further checking?)
			/* QuadCurve2D.Double curve = new QuadCurve2D.Double(
					ps.getX(), ps.getY(), 
					pc.getX(), ctrlY,
					pe.getX(), pe.getY());
			 */

			QuadCurve2D.Double curve = new QuadCurve2D.Double(
					Math.floor(ps.getX()), Math.floor(ps.getY()), 
					Math.floor(pc.getX()), Math.floor(ctrlY), 
					Math.floor(pe.getX()), Math.floor(pe.getY()));

			//System.out.printf("curve: start=%s ctrl=%s end=%s extend=%1.0f\n", ps, pc, pe, ctrlY);
			
			g.draw(curve);
		} else {
			g.drawLine((int) ps.getX(), (int) ps.getY(), (int) pe.getX(),
					(int) pe.getY());
		}
	}
}
