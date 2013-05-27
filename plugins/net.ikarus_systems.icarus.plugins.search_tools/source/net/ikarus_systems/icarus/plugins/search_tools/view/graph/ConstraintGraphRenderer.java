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

import java.util.List;

import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphRenderer;
import net.ikarus_systems.icarus.plugins.jgraph.util.GraphUtils;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.util.HtmlUtils.HtmlTableBuilder;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintGraphRenderer extends GraphRenderer {
	
	protected HtmlTableBuilder tableBuilder = new HtmlTableBuilder(500);
	
	protected StringBuilder sb;
	
	protected ConstraintGraphPresenter presenter;

	public ConstraintGraphRenderer() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof ConstraintGraphPresenter) {
			presenter = (ConstraintGraphPresenter) target;
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		presenter = null;
	}
	
	protected String normalize(String s) {
		return s==null || s.isEmpty() ? "-" : s; //$NON-NLS-1$
	}

	@Override
	public String convertValueToString(GraphOwner owner, Object cell) {
		if(presenter==null) {
			return ""; //$NON-NLS-1$
		}
		
		mxIGraphModel model = owner.getGraph().getModel();
		
		if(GraphUtils.isOrderEdge(owner, model, cell)) {
			return ""; //$NON-NLS-1$
		}
		
		Object value = model.getValue(cell);
		
		if(sb==null) {
			sb = new StringBuilder(200);
		}
		
		sb.setLength(0);
		
		List<ConstraintFactory> factories = null;
		SearchConstraint[] constraints = null;
		
		if(value instanceof ConstraintNodeData) {
			ConstraintNodeData data = (ConstraintNodeData)value;
			
			// Negated state
			if(data.isNegated()) {
				sb.append(ResourceManager.getInstance().get(
						"plugins.searchTools.labels.negated")).append(": ").append(data.isNegated()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			// Node Type
			if(data.getNodeType()!=NodeType.GENERAL) {
				sb.append("\n").append(data.getNodeType().getName()); //$NON-NLS-1$
			}
			
			// Constraints
			factories = presenter.getConstraintContext().getNodeFactories();
			constraints = data.getConstraints();
		} else if(value instanceof ConstraintEdgeData) {
			ConstraintEdgeData data = (ConstraintEdgeData)value;
			
			// Negated state
			if(data.isNegated()) {
				sb.append(ResourceManager.getInstance().get(
						"plugins.searchTools.labels.negated")).append(": ").append(data.isNegated()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			// Edge Type
			if(data.getEdgeType()!=EdgeType.DOMINANCE) {
				sb.append("\n").append(data.getEdgeType().getName()); //$NON-NLS-1$
			}
			
			// Constraints
			factories = presenter.getConstraintContext().getEdgeFactories();
			constraints = data.getConstraints();
		}

		// Constraints
		if(factories!=null && !factories.isEmpty()) {
			for(int i=0; i<constraints.length; i++) {
				if(!constraints[i].isUndefined()) {
					SearchOperator operator = constraints[i].getOperator();
					Object label = operator==SearchOperator.GROUPING ? ""  //$NON-NLS-1$
							: factories.get(i).valueToLabel(constraints[i].getValue());
					sb.append("\n").append(factories.get(i).getName()) //$NON-NLS-1$
					.append(" ").append(operator.getSymbol()).append(" ").append(label); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		
		String result = sb.toString().trim();
		
		if(result.isEmpty()) {
			result = SearchUtils.DATA_UNDEFINED_LABEL;
		}
		
		return result;
	}

	@Override
	public String getToolTipForCell(GraphOwner owner, Object cell) {
		// TODO use table builder to create full view of constraints!!
		return "<html>"+convertValueToString(owner, cell).replaceAll("\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public mxRectangle getPreferredSizeForCell(GraphOwner owner, Object cell) {
		// TODO Auto-generated method stub
		return super.getPreferredSizeForCell(owner, cell);
	}
}