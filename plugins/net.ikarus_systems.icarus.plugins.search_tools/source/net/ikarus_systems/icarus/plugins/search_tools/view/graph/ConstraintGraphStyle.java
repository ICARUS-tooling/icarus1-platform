/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.graph;

import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.plugins.jgraph.layout.DefaultGraphStyle;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner;
import net.ikarus_systems.icarus.search_tools.Grouping;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.util.Options;

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
					if(constraint==null || !SearchManager.isGroupingOperator(constraint.getOperator())) {
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
