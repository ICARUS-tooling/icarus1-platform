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
package de.ims.icarus.plugins.prosody.ui.helper;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEParamsWrapper;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEIcon;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEParamsTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 2802079024997683283L;

	private final PaIntEGraph graph;

	private final transient PaIntEParams paramsBuffer = new PaIntEParams();

	private final PaIntEIcon painteIcon;

	public PaIntEParamsTableCellRenderer() {
		graph = createGraph();
		painteIcon = new PaIntEIcon(graph, paramsBuffer, true);
	}

	public PaIntEParamsTableCellRenderer(PaIntEGraph graph) {
		this.graph = graph;
		painteIcon = new PaIntEIcon(graph, paramsBuffer, false);
	}

	protected PaIntEGraph createGraph() {
		PaIntEGraph graph = new PaIntEGraph();
		graph.setPaintYAxis(false);
		graph.setPaintXAxis(false);
		graph.setPaintBorder(true);

		return graph;
	}

	public PaIntEIcon getPaIntEIcon() {
		return painteIcon;
	}

	public PaIntEGraph getGraph() {
		return graph;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		boolean paintCompact = false;

		if(value instanceof PaIntEParamsWrapper) {
			PaIntEParamsWrapper wrapper = (PaIntEParamsWrapper) value;
			value = wrapper.getParams();
			paintCompact = wrapper.isCompact();
		}

		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);

		painteIcon.getCurve().setPaintComapct(paintCompact);
		paramsBuffer.setParams((PaIntEParams) value);

		setIcon(painteIcon);
		setToolTipText(null);
		setText(null);

		return this;
	}

}
