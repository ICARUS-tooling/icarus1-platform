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

import java.awt.Dimension;
import java.util.List;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;

import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.ConstraintFactory;
import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.NodeType;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.HtmlUtils.HtmlTableBuilder;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintGraphRenderer extends GraphRenderer {
	
	protected HtmlTableBuilder tableBuilder = new HtmlTableBuilder(500);
	
	protected StringBuilder sb;
	
	public static final Dimension disjunctionNodeSize = new Dimension(25, 25);
	
	protected char disjunctionSymbol = (char) 0x2228;
	protected String disjunctionString = String.valueOf(disjunctionSymbol);
	protected char negationSymbol = (char) 0x00AC;
	protected String negationString = String.valueOf(negationSymbol);
	
	protected ConstraintGraphPresenter presenter;

	public ConstraintGraphRenderer() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof ConstraintGraphPresenter) {
			presenter = (ConstraintGraphPresenter) target;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
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
		
		if(GraphUtils.isOrderEdge(owner, model, cell)
				|| ConstraintGraphPresenter.isLinkEdge(owner, cell)) {
			return ""; //$NON-NLS-1$
		}
		
		Object value = model.getValue(cell);
		
		if(sb==null) {
			sb = new StringBuilder(200);
		}
		
		sb.setLength(0);
		boolean leaveEmpty = false;
		
		List<ConstraintFactory> factories = null;
		SearchConstraint[] constraints = null;
		
		if(value instanceof ConstraintNodeData) {
			ConstraintNodeData data = (ConstraintNodeData)value;
			
			if(data.getNodeType()==NodeType.DISJUNCTION) {
				return data.isNegated() ? negationString+disjunctionString : disjunctionString;
			}
			
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
			leaveEmpty = data.getEdgeType()==EdgeType.DOMINANCE;
			
			if(data.getEdgeType()==EdgeType.LINK) {
				return ""; //$NON-NLS-1$
			}
			
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
		if(factories!=null && !factories.isEmpty() && constraints!=null) {
			for(int i=0; i<constraints.length; i++) {
				if(!constraints[i].isUndefined()) {
					SearchOperator operator = constraints[i].getOperator();
					Object label = SearchManager.isGroupingOperator(operator) ? ""  //$NON-NLS-1$
							: factories.get(i).valueToLabel(constraints[i].getValue());
					sb.append("\n").append(factories.get(i).getName()) //$NON-NLS-1$
					.append(" ").append(operator.getSymbol()).append(" ").append(label); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		
		String result = sb.toString().trim();
		
		if(result.isEmpty() && !leaveEmpty) {
			result = SearchUtils.DATA_UNDEFINED_LABEL;
		}
		
		return result;
	}

	@Override
	public String getToolTipForCell(GraphOwner owner, Object cell) {
		String tooltip = null;
		if(ConstraintGraphPresenter.isDisjunctionNode(owner, cell)) {
			tooltip = ResourceManager.getInstance().get(
					"plugins.searchTools.nodeType.disjunction.description"); //$NON-NLS-1$
		} else if(ConstraintGraphPresenter.isLinkEdge(owner, cell)) {
			tooltip = ResourceManager.getInstance().get(
					"plugins.searchTools.edgeType.link.description"); //$NON-NLS-1$
		} else {
			// TODO use table builder to create full view of constraints!!
			tooltip = convertValueToString(owner, cell);
		}
		
		return UIUtil.toSwingTooltip(tooltip);
	}
	
	@Override
	public mxRectangle getPreferredSizeForCell(GraphOwner owner, Object cell) {
		if(ConstraintGraphPresenter.isDisjunctionNode(owner, cell)) {
			return new mxRectangle(0, 0, disjunctionNodeSize.width, disjunctionNodeSize.height);
		} else {
			mxRectangle size = super.getPreferredSizeForCell(owner, cell);
			if(size!=null && size.getWidth()>0 && size.getWidth()<50) {
				size.setWidth(50);
			}
			
			return size;
		}
	}
}