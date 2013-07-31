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
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CurveConnectorShape extends mxConnectorShape {

	public CurveConnectorShape() {
		// no-op
	}

	@Override
	protected void paintPolyline(mxGraphics2DCanvas canvas,
			List<mxPoint> pts, Map<String, Object> style) {

		Graphics2D g = canvas.getGraphics();
		
		mxPoint ps = pts.get(0);
		mxPoint pe = pts.get(pts.size() - 1);
		/*if (ps.getX() > pe.getX()) {
			mxPoint tmp = ps;
			ps = pe;
			pe = tmp;
		}*/
		
		if (pts.size() > 1) {
			double ctrlX = (ps.getX() + pe.getX()) * 0.5;

			double height = Math.abs(ps.getY()-pe.getY());
			double ctrlY = ps.getY()>pe.getY() ?
					pe.getY() + (height * 0.8)
					: ps.getY() + (height * 0.2);
			
			QuadCurve2D.Double curve = new QuadCurve2D.Double(
					Math.floor(ps.getX()), Math.floor(ps.getY()), 
					Math.floor(ctrlX), Math.floor(ctrlY), 
					Math.floor(pe.getX()), Math.floor(pe.getY()));

			g.draw(curve);
		} else {
			g.drawLine((int) ps.getX(), (int) ps.getY(), (int) pe.getX(),
					(int) pe.getY());
		}
	}

}
