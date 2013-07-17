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
