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

import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphRenderer;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.SearchUtils;
import net.ikarus_systems.icarus.util.HtmlUtils.HtmlTableBuilder;

import com.mxgraph.model.mxIGraphModel;

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
	public void install(GraphOwner target) {
		if(target instanceof ConstraintGraphPresenter) {
			presenter = (ConstraintGraphPresenter) target;
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(GraphOwner target) {
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
		Object value = model.getValue(cell);
		
		if(sb==null) {
			sb = new StringBuilder(200);
		}
		
		sb.setLength(0);
		
		if(value instanceof ConstraintNodeData) {
			ConstraintNodeData data = (ConstraintNodeData)value;
			
			// Negated state
			if(data.isNegated()) {
				sb.append(ResourceManager.getInstance().get(
						"plugins.searchTools.labels.negated")).append(": ").append(data.isNegated()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Root
			if(data.isRoot()) {
				sb.append(ResourceManager.getInstance().get(
						"plugins.searchTools.labels.root")).append(": ").append(data.isRoot()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			// Constraints
			ConstraintFactory[] factories = presenter.getNodeConstraintFactories();
			SearchConstraint[] constraints = data.getConstraints();
			if(factories!=null && factories.length>0) {
				for(int i=0; i<constraints.length; i++) {
					if(!constraints[i].isUndefined()) {
						Object label = factories[i].valueToLabel(constraints[i].getValue());
						SearchOperator operator = constraints[i].getOperator();
						sb.append("\n").append(factories[i].getName()) //$NON-NLS-1$
						.append(" ").append(operator.getSymbol()).append(" ").append(label); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			
		} else if(value instanceof ConstraintEdgeData) {
			ConstraintEdgeData data = (ConstraintEdgeData)value;
			
			// Negated state
			if(data.isNegated()) {
				sb.append(ResourceManager.getInstance().get(
						"plugins.searchTools.labels.negated")).append(": ").append(data.isNegated()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			// Edge Type
			sb.append("\n").append(ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.searchTools.labels.edgeType")).append(": ").append(data.getEdgeType().getName()); //$NON-NLS-1$ //$NON-NLS-2$
			
			// Constraints
			ConstraintFactory[] factories = presenter.getEdgeConstraintFactories();
			SearchConstraint[] constraints = data.getConstraints();
			if(factories!=null && factories.length>0) {
				for(int i=0; i<constraints.length; i++) {
					if(!constraints[i].isUndefined()) {
						Object label = factories[i].valueToLabel(constraints[i].getValue());
						SearchOperator operator = constraints[i].getOperator();
						sb.append("\n").append(factories[i].getName()) //$NON-NLS-1$
						.append(" ").append(operator.getSymbol()).append(" ").append(label); //$NON-NLS-1$ //$NON-NLS-2$
					}
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
}