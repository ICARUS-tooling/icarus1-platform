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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;

import de.ims.icarus.plugins.prosody.painte.PaIntEParams;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEIcon implements Icon {

	private final PaIntEGraph graph;
	private final PaIntEParams params;
	private final boolean adjustAxis;

	private final Dimension iconSize = new Dimension(32, 32);

	private final transient Rectangle bounds = new Rectangle();

	private boolean clipCurve;

	public PaIntEIcon() {

		graph = new PaIntEGraph();
		graph.setPaintYAxis(false);
		graph.setPaintXAxis(false);
		graph.setPaintBorder(true);

		params = new PaIntEParams();

		adjustAxis = true;
		clipCurve = graph.getCurve().isClipCurve();
	}

	public PaIntEIcon(PaIntEGraph graph, PaIntEParams params, boolean adjustAxis) {
		if (graph == null)
			throw new NullPointerException("Invalid graph"); //$NON-NLS-1$
		if (params == null)
			throw new NullPointerException("Invalid params"); //$NON-NLS-1$

		this.graph = graph;
		this.params = params;
		this.adjustAxis = adjustAxis;
		clipCurve = graph.getCurve().isClipCurve();
	}

	public PaIntEParams getParams() {
		return params;
	}

	public PaIntECurve getCurve() {
		return graph.getCurve();
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {

		bounds.setBounds(x+1, y+1, getIconWidth()-2, getIconHeight()-2);

		if(adjustAxis) {
			double dMax = params.getD();
			double dMin = params.getD()-Math.max(params.getC1(), params.getC2());

			Axis.Integer yAxis = (Axis.Integer) graph.getYAxis();
			yAxis.setMinValue((int) Math.floor(dMin));
			yAxis.setMaxValue((int) Math.ceil(dMax));
		}

		g.setColor(Color.black);
		g.drawRect(x, y, getIconWidth()-1, getIconHeight()-1);

		boolean doClip = graph.getCurve().isClipCurve();

		graph.getCurve().setClipCurve(clipCurve);

		graph.getCurve().paint(g, params, bounds,
				graph.getXAxis(), graph.getYAxis());

		graph.getCurve().setClipCurve(doClip);
	}

	@Override
	public int getIconWidth() {
		return iconSize.width;
	}

	@Override
	public int getIconHeight() {
		return iconSize.height;
	}

	public Dimension getIconSize() {
		return new Dimension(iconSize);
	}

	public void setIconSize(Dimension newSize) {
		if (newSize == null)
			throw new NullPointerException("Invalid newSize"); //$NON-NLS-1$

		iconSize.setSize(newSize);
	}

	public boolean isClipCurve() {
		return clipCurve;
	}

	public void setClipCurve(boolean clipCurve) {
		this.clipCurve = clipCurve;
	}
}
