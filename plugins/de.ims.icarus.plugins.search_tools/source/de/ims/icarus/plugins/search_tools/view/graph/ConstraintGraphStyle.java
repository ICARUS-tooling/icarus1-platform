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
package de.ims.icarus.plugins.search_tools.view.graph;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.plugins.jgraph.layout.DefaultGraphStyle;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.search_tools.Grouping;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintGraphStyle extends DefaultGraphStyle {
	
	public ConstraintGraphStyle() {
		// no-op
	}
	
	@Override
	protected void initStylesheet() {
		super.initStylesheet();
		
		Map<String, Object> disjunctionStyle = new HashMap<>();
		disjunctionStyle.put("shape", "ellipse"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("perimeter", "ellipsePerimeter"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("verticalAlign", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("align", "center"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("verticalLabelPosition", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("labelPosition", "center"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("strokeColor", "blue"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("fontColor", "black"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("fontSize", 14); //$NON-NLS-1$
		disjunctionStyle.put("overflow", "fill"); //$NON-NLS-1$ //$NON-NLS-2$
		disjunctionStyle.put("fillColor", "white"); //$NON-NLS-1$ //$NON-NLS-2$
		
		stylesheet.putCellStyle("disjunction", disjunctionStyle); //$NON-NLS-1$
		
		Map<String, Object> linkStyle = new HashMap<>();
		linkStyle.put("endArrow", "none"); //$NON-NLS-1$ //$NON-NLS-2$
		linkStyle.put("startArrow", "none"); //$NON-NLS-1$ //$NON-NLS-2$
		linkStyle.put("strokeColor", "green"); //$NON-NLS-1$ //$NON-NLS-2$
		linkStyle.put("exitPerimeter", 1); //$NON-NLS-1$ 
		linkStyle.put("entryPerimeter", 1); //$NON-NLS-1$
		linkStyle.put("dashed", true); //$NON-NLS-1$
		linkStyle.put("dashPattern", "2 4"); //$NON-NLS-1$ //$NON-NLS-2$

		stylesheet.putCellStyle("link", linkStyle); //$NON-NLS-1$
	}

	@Override
	protected void refreshStylesheet(Handle handle) {
		super.refreshStylesheet(handle);

		ConfigRegistry config = handle.getSource();
		
		Map<String, Object> linkStyle = stylesheet.getStyles().get("link"); //$NON-NLS-1$
		linkStyle.put("strokeColor", int2ColString(config.getInteger(config.getChildHandle(handle, "linkStrokeColor")))); //$NON-NLS-1$ //$NON-NLS-2$
		linkStyle.put("strokeWidth", config.getInteger(config.getChildHandle(handle, "linkStrokeWidth"))); //$NON-NLS-1$ //$NON-NLS-2$
		
	}

	@Override
	public String getStyle(GraphOwner owner, Object cell, Options options) {
		if(ConstraintGraphPresenter.isDisjunctionNode(owner, cell)) {
			return "disjunction"; //$NON-NLS-1$
		} else if(ConstraintGraphPresenter.isLinkEdge(owner, cell)) {
			return "link"; //$NON-NLS-1$
		} else {
			String style = super.getStyle(owner, cell, options);

			SearchConstraint[] constraints = null;
			Object value = owner.getGraph().getModel().getValue(cell);
			if(value instanceof ConstraintCellData) {
				constraints = ((ConstraintCellData<?>)value).getConstraints();
			}
			
			if(constraints!=null) {
				for(SearchConstraint constraint : constraints) {
					if(constraint==null || !constraint.isActive() || !SearchManager.isGroupingOperator(constraint.getOperator())) {
						continue;
					}
					
					int index = (int) constraint.getValue();
					Grouping grouping = Grouping.getGrouping(index);
					style += ";strokeColor="+int2ColString(grouping.getColor().getRGB()); //$NON-NLS-1$
				}
			}
			
			return style;
		}
	}
}
