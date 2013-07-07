/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.layout;

import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxConnectorShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

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
	public void paintShape(mxGraphics2DCanvas canvas, mxCellState state) {
		if (state.getAbsolutePointCount() > 1
				&& configureGraphics(canvas, state, false)) {
			ArrayList<mxPoint> pts = new ArrayList<mxPoint>(state
					.getAbsolutePoints());
			Map<String, Object> style = state.getStyle();

			// Paints the markers and updates the points
			// Switch off any dash pattern for markers
			boolean dashed = mxUtils.isTrue(style, mxConstants.STYLE_DASHED);
			Object dashedValue = style.get(mxConstants.STYLE_DASHED);

			if (dashed) {
				style.remove(mxConstants.STYLE_DASHED);
				canvas.getGraphics().setStroke(canvas.createStroke(style));
			}

			translatePoint(pts, 0, paintMarker(canvas, state, true));
			translatePoint(pts, pts.size() - 1, paintMarker(canvas, state,
					false));

			if (dashed) {
				// Replace the dash pattern
				style.put(mxConstants.STYLE_DASHED, dashedValue);
				canvas.getGraphics().setStroke(canvas.createStroke(style));
			}

			Graphics2D g = canvas.getGraphics();
			
			/*g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					RenderingHints.VALUE_ANTIALIAS_ON);*/
			
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

	private void translatePoint(List<mxPoint> points, int index, mxPoint offset) {
		if (offset != null) {
			mxPoint pt = (mxPoint) points.get(index).clone();
			pt.setX(pt.getX() + offset.getX());
			pt.setY(pt.getY() + offset.getY());
			points.set(index, pt);
		}
	}
}
