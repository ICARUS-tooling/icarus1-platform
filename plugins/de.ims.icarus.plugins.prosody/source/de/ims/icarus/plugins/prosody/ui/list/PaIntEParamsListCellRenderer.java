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
package de.ims.icarus.plugins.prosody.ui.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEParamsWrapper;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEParamsListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 4022063161688722353L;

	private final PaIntEGraph graph;

	private final Dimension iconSize = new Dimension(32, 32);

	private final transient PaIntEParams paramsBuffer = new PaIntEParams();

	private final boolean adjustAxis;

	private final Icon graphIcon = new Icon() {

		private final Rectangle bounds = new Rectangle();

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {

			bounds.setBounds(x+1, y+1, getIconWidth()-2, getIconHeight()-2);

			if(adjustAxis) {
				double dMax = paramsBuffer.getD();
				double dMin = paramsBuffer.getD()-Math.max(paramsBuffer.getC1(), paramsBuffer.getC2());

				Axis.Integer yAxis = (Axis.Integer) graph.getYAxis();
				yAxis.setMinValue((int) Math.floor(dMin));
				yAxis.setMaxValue((int) Math.ceil(dMax));
			}

			g.setColor(Color.black);
			g.drawRect(x, y, getIconWidth()-1, getIconHeight()-1);

			graph.getCurve().paint(g, paramsBuffer, bounds,
					graph.getXAxis(), graph.getYAxis());
		}

		@Override
		public int getIconWidth() {
			return iconSize.width;
		}

		@Override
		public int getIconHeight() {
			return iconSize.height;
		}
	};

	public PaIntEParamsListCellRenderer() {
		graph = createGraph();
		adjustAxis = true;
	}

	public PaIntEParamsListCellRenderer(PaIntEGraph graph) {
		this.graph = graph;
		adjustAxis = false;
	}

	protected PaIntEGraph createGraph() {
		PaIntEGraph graph = new PaIntEGraph();
		graph.setPaintYAxis(false);
		graph.setPaintXAxis(false);
		graph.setPaintBorder(true);

		return graph;
	}

	public PaIntEGraph getGraph() {
		return graph;
	}

	public Dimension getIconSize() {
		return new Dimension(iconSize);
	}

	public void setIconSize(Dimension newSize) {
		if (newSize == null)
			throw new NullPointerException("Invalid newSize"); //$NON-NLS-1$

		iconSize.setSize(newSize);
	}

	protected String getLabel(PaIntEParams params) {
		return String.format(Locale.ENGLISH, "A1:%.02f A2:%.02f B:%.02f C1:%.02f C2:%.02f D:%.02f alignment:%.02f", //$NON-NLS-1$
				params.getA1(), params.getA2(), params.getB(), params.getC1(),
				params.getC2(), params.getD(), params.getAlignment());
	}

	protected static final PaIntEConstraintParams constraints = new PaIntEConstraintParams();

	protected String getConstraintLabel(PaIntEParams params) {
		constraints.setParams(params);
		return constraints.toString();
	}

	protected static String toString(double value) {
		return String.format(Locale.ENGLISH, "%.02f", value); //$NON-NLS-1$
	}

	protected String substitute(String label, PaIntEParams params) {
		// Extremely stupid and wasteful approach, but should still be performant enough...
		label = label.replaceAll("\\$a1", toString(params.getA1())); //$NON-NLS-1$
		label = label.replaceAll("\\$a2", toString(params.getA2())); //$NON-NLS-1$
		label = label.replaceAll("\\$b", toString(params.getB())); //$NON-NLS-1$
		label = label.replaceAll("\\$c1", toString(params.getC1())); //$NON-NLS-1$
		label = label.replaceAll("\\$c2", toString(params.getC2())); //$NON-NLS-1$
		label = label.replaceAll("\\$d", toString(params.getD())); //$NON-NLS-1$
		label = label.replaceAll("\\$alignment", toString(params.getAlignment())); //$NON-NLS-1$
		label = label.replace("$params", getLabel(params)); //$NON-NLS-1$
		label = label.replace("$constraints", getConstraintLabel(params)); //$NON-NLS-1$

		return label;
	}

	/**
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		String label = null;

		if(value instanceof PaIntEParamsWrapper) {
			PaIntEParamsWrapper wrapper = (PaIntEParamsWrapper) value;
			value = wrapper.get();
			label = wrapper.getLabel();

			if(label!=null && !label.isEmpty() && label.indexOf('$')!=-1) {
				label = substitute(label, wrapper.get());
			}
		}

		// Use super implementation for selection, border and stuff
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		paramsBuffer.setParams((PaIntEParams) value);

		if(label==null || label.isEmpty()) {
			label = getLabel(paramsBuffer);
		}

		setIcon(graphIcon);
		setText(label);

		return this;
	}

}
