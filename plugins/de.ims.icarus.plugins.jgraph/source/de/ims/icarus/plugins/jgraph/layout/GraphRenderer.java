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


import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.Installable;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class GraphRenderer extends mxInteractiveCanvas implements Installable {

	public GraphRenderer() {
		// no-op
	}
	
	/**
	 * Calculates the required size of the given cell.
	 * If an implementation does not wish to calculate that size
	 * itself then {@code null} should be returned. This tells
	 * the graph to compute the preferred size based on the
	 * cell's label text. Note that delegating to the {@link mxGraph#getPreferredSizeForCell(Object)}
	 * method in the supplied {@code GraphOwner}'s graph might cause
	 * an infinite loop when said graph is using this {@code GraphRenderer}
	 * object!
	 * <p>
	 * The default implementation returns {@code null};
	 */
	public mxRectangle getPreferredSizeForCell(GraphOwner owner, Object cell) {
		return null;
	}

	/**
	 * Generates a label for the given cell.
	 * <p>
	 * The default implementation uses the {@link Object#toString()} method
	 * of the cell's value if available or returns the empty string
	 * if the value is {@code null};
	 */
	public String convertValueToString(GraphOwner owner, Object cell) {
		Object value = owner.getGraph().getModel().getValue(cell);
		return value==null ? "" : value.toString(); //$NON-NLS-1$
	}

	/**
	 * Generates a tool-tip text for the given cell.
	 * <p>
	 * The default implementation delegates to {@link #convertValueToString(GraphOwner, Object)}
	 */
	public String getToolTipForCell(GraphOwner owner, Object cell) {
		return UIUtil.toSwingTooltip(convertValueToString(owner, cell));
	}
}
