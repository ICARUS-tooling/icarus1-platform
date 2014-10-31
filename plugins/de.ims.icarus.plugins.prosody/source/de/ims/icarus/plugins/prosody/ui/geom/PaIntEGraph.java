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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.geom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import de.ims.icarus.plugins.prosody.painte.PaIntEParams;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEGraph {

	public static final boolean DEFAULT_PAINT_X_AXIS = true;
	public static final boolean DEFAULT_PAINT_Y_AXIS = true;
	public static final boolean DEFAULT_PAINT_BORDER = false;
	public static final boolean DEFAULT_PAINT_GRID = false;
	public static final GridStyle DEFAULT_GRID_STYLE = GridStyle.SOLID_FULL;
	public static final Color DEFAULT_GRID_COLOR = Color.gray;
	public static final Color DEFAULT_BORDER_COLOR = Color.black;

	private boolean paintXAxis = DEFAULT_PAINT_X_AXIS;
	private boolean paintYAxis = DEFAULT_PAINT_Y_AXIS;
	private boolean paintBorder = DEFAULT_PAINT_BORDER;
	private boolean paintGrid = DEFAULT_PAINT_GRID;

	private Axis xAxis;
	private Axis yAxis;

	private GridStyle gridStyle = DEFAULT_GRID_STYLE;
	private Color gridColor = DEFAULT_GRID_COLOR;
	private Color borderColor = DEFAULT_BORDER_COLOR;

	private final PaIntECurve curve;

	public PaIntEGraph() {
		xAxis = new Axis.Integer(false, -1, +2, 1);
		xAxis.setLabelStepSize(1);
		xAxis.setLabelVertical(false);

		yAxis = new Axis.Integer(true, 60, 200, 20);
		yAxis.setLabelStepSize(2);
		yAxis.setLabelVertical(false);

		curve = new PaIntECurve();
	}

	public PaIntEGraph(Axis xAxis, Axis yAxis, PaIntECurve curve) {
		if (curve == null)
			throw new NullPointerException("Invalid curve"); //$NON-NLS-1$

		setXAxis(xAxis);
		setYAxis(yAxis);

		this.curve = curve;
	}

	public void paint(Graphics graphics, PaIntEParams params, Rectangle area) {

		final int x = area.x;
		final int y = area.y;
		final int w = area.width;
		final int h = area.height;

		Graphics2D g = (Graphics2D) graphics;

		final Color c = g.getColor();
		g.translate(x, y);

		int yAxisWidth = paintYAxis ? yAxis.getRequiredWidth(g) : 0;
		int xAxisHeight = paintXAxis ? xAxis.getRequiredHeight(g) : 0;

		Rectangle buffer = new Rectangle();

		if(paintBorder) {
			g.setColor(borderColor);
			g.drawRect(0, 0, w, h);
		}

		// Now paint axis areas if required

		if(paintYAxis) {
			buffer.setBounds(0, 0, yAxisWidth, h-xAxisHeight);
			yAxis.paint(g, buffer);
		}

		if(paintXAxis) {
			buffer.setBounds(yAxisWidth, h-xAxisHeight, w-yAxisWidth, xAxisHeight);
			xAxis.paint(g, buffer);
		}

		if(paintGrid) {
			gridStyle.paintGrid(g, xAxis, yAxis, gridColor, yAxisWidth, 0, w-yAxisWidth, h-xAxisHeight);
		}

		buffer.setBounds(yAxisWidth, 0, w-yAxisWidth, h-xAxisHeight);

		if(params!=null) {
			curve.paint(g, params, buffer, xAxis, yAxis);
		}

		g.setColor(c);
		g.translate(-x, -y);
	}

	/**
	 * @return the paintBorder
	 */
	public boolean isPaintBorder() {
		return paintBorder;
	}

	/**
	 * @return the paintXAxis
	 */
	public boolean isPaintXAxis() {
		return paintXAxis;
	}

	/**
	 * @return the paintYAxis
	 */
	public boolean isPaintYAxis() {
		return paintYAxis;
	}

	/**
	 * @return the paintGrid
	 */
	public boolean isPaintGrid() {
		return paintGrid;
	}

	/**
	 * @return the gridStyle
	 */
	public GridStyle getGridStyle() {
		return gridStyle;
	}

	/**
	 * @return the gridColor
	 */
	public Color getGridColor() {
		return gridColor;
	}

	/**
	 * @param gridColor the gridColor to set
	 */
	public void setGridColor(Color gridColor) {
		if (gridColor == null)
			throw new NullPointerException("Invalid gridColor"); //$NON-NLS-1$

		this.gridColor = gridColor;
	}

	/**
	 * @return the borderColor
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * @param borderColor the borderColor to set
	 */
	public void setBorderColor(Color borderColor) {
		if (borderColor == null)
			throw new NullPointerException("Invalid borderColor"); //$NON-NLS-1$

		this.borderColor = borderColor;
	}

	/**
	 * @param paintGrid the paintGrid to set
	 */
	public void setPaintGrid(boolean paintGrid) {
		this.paintGrid = paintGrid;
	}

	/**
	 * @param gridStyle the gridStyle to set
	 */
	public void setGridStyle(GridStyle gridStyle) {
		if (gridStyle == null)
			throw new NullPointerException("Invalid gridStyle"); //$NON-NLS-1$

		this.gridStyle = gridStyle;
	}

	/**
	 * @param paintBorder the paintBorder to set
	 */
	public void setPaintBorder(boolean paintBorder) {
		this.paintBorder = paintBorder;
	}

	/**
	 * @param paintXAxis the paintXAxis to set
	 */
	public void setPaintXAxis(boolean paintXAxis) {
		this.paintXAxis = paintXAxis;
	}

	/**
	 * @param paintYAxis the paintYAxis to set
	 */
	public void setPaintYAxis(boolean paintYAxis) {
		this.paintYAxis = paintYAxis;
	}

	/**
	 * @return the xAxis
	 */
	public Axis getXAxis() {
		return xAxis;
	}

	/**
	 * @return the yAxis
	 */
	public Axis getYAxis() {
		return yAxis;
	}

	public void setXAxis(Axis xAxis) {
		if (xAxis == null)
			throw new NullPointerException("Invalid xAxis");

		if(xAxis.isVertical())
			throw new IllegalArgumentException("Cannot use vertical axis for x values"); //$NON-NLS-1$

		this.xAxis = xAxis;
	}

	/**
	 * @return the curve
	 */
	public PaIntECurve getCurve() {
		return curve;
	}

	public void setYAxis(Axis yAxis) {
		if (yAxis == null)
			throw new NullPointerException("Invalid yAxis");

		if(!yAxis.isVertical())
			throw new IllegalArgumentException("Cannot use horizontal axis for y values"); //$NON-NLS-1$

		this.yAxis = yAxis;
	}

	/**
	 * Translates the given {@code Point} into coordinates
	 * of the underlying curve if the point is reasonably close
	 * to the curve. The point is expected to be already translated
	 * to this graph's painting rectangle, specified by the {@code area}
	 * argument.
	 */
	public PaIntEHitBox translate(int x, int y, Graphics graphics, Rectangle area, PaIntEParams params, double accuracy) {

		final int w = area.width;
		final int h = area.height;

		Graphics2D g = (Graphics2D) graphics;

		int yAxisWidth = paintYAxis ? yAxis.getRequiredWidth(g) : 0;
		int xAxisHeight = paintXAxis ? xAxis.getRequiredHeight(g) : 0;

		if(paintYAxis && x<=yAxisWidth) {
			if(y>h-xAxisHeight) {
				// Outside axis area
				return null;
			}
			double axisValue = yAxis.translate(h-xAxisHeight-y, h-xAxisHeight);
			return new PaIntEHitBox(0, axisValue, yAxis);
		}

		if(paintXAxis && y>=h-xAxisHeight) {
			if(x<yAxisWidth) {
				// Outside axis area
				return null;
			}
			double axisValue = xAxis.translate(x-yAxisWidth, w-yAxisWidth);
			return new PaIntEHitBox(axisValue, 0, xAxis);
		}

		if(xAxisHeight>0 || yAxisWidth>0) {
			area = new Rectangle(yAxisWidth, 0, w-yAxisWidth, h-xAxisHeight);
			x -= yAxisWidth;
		}

		PaIntEHitBox hitBox = curve.translate(x, y, area, params, accuracy, xAxis, yAxis);
		if(hitBox==null) {
			hitBox = new PaIntEHitBox(params);
		}

		return hitBox;
	}
}
