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

import java.awt.Dimension;

import net.ikarus_systems.icarus.config.ConfigDelegate;
import net.ikarus_systems.icarus.plugins.jgraph.layout.DefaultTreeLayout;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphRenderer;
import net.ikarus_systems.icarus.plugins.jgraph.util.CellBuffer;
import net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.SearchUtils;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.Order;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.handler.mxConnectPreview;
import com.mxgraph.swing.view.mxICellEditor;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintGraphPresenter extends GraphPresenter {

	private static final long serialVersionUID = -674508666642816142L;
	
	protected SearchGraph searchGraph;
	
	protected ConstraintFactory[] nodeConstraintFactories;
	protected ConstraintFactory[] edgeConstraintFactories;
	
	protected ContentType constraintTargetType;
	
	public ConstraintGraphPresenter() {
		// no-op
	}

	@Override
	protected void initGraphComponentInternals() {
		super.initGraphComponentInternals();
		
		setMinimumNodeSize(new Dimension(35, 25));
	}

	@Override
	protected mxICellEditor createCellEditor() {
		return new ConstraintCellEditor(this);
	}

	@Override
	protected GraphRenderer createDefaultGraphRenderer() {
		return new ConstraintGraphRenderer();
	}
	
	@Override
	protected GraphLayout createDefaultGraphLayout() {
		return new DefaultTreeLayout();
	}

	@Override
	protected ConfigDelegate createConfigDelegate() {
		return new GraphConfigDelegate("plugins.jgraph.appearance.constraints", null); //$NON-NLS-1$
	}

	@Override
	protected mxConnectPreview createConnectPreview() {
		return new CConnectPreview();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return searchGraph!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		// TODO create SearchGraph object from our onscreen graph and return
		return searchGraph;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(SearchGraph.class);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#setData(java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	protected void setData(Object data, Options options) {
	}
	
	protected ConstraintNodeData createNodeData() {
		if(nodeConstraintFactories==null)
			throw new IllegalStateException("Cannot create constraint data without factorsies"); //$NON-NLS-1$
		
		int size = nodeConstraintFactories.length;
		ConstraintNodeData nodeData = new ConstraintNodeData(size);
		for(int i=0; i<size; i++) {
			ConstraintFactory factory = nodeConstraintFactories[i];
			SearchOperator operator = factory.getSupportedOperators()[0];
			Object value = factory.getDefaultValue();
			
			nodeData.setConstraint(i, factory.createConstraint(value, operator));
		}
				
		return nodeData;
	}
	
	protected ConstraintEdgeData createEdgeData() {
		if(edgeConstraintFactories==null)
			throw new IllegalStateException("Cannot create constraint data without factorsies"); //$NON-NLS-1$
		
		int size = edgeConstraintFactories.length;
		ConstraintEdgeData edgeData = new ConstraintEdgeData(size);
		for(int i=0; i<size; i++) {
			ConstraintFactory factory = edgeConstraintFactories[i];
			SearchOperator operator = factory.getSupportedOperators()[0];
			Object value = factory.getDefaultValue();
			
			edgeData.setConstraint(i, factory.createConstraint(value, operator));
		}
		
		return edgeData;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#syncToGraph()
	 */
	@Override
	protected void syncToGraph() {
		// TODO
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#syncToData()
	 */
	@Override
	protected void syncToData() {
		// Our "backing data" is directly represented in the tree
	}

	@Override
	protected mxGraph createGraph() {
		// TODO Auto-generated method stub
		return super.createGraph();
	}

	@Override
	public CellBuffer exportCells(Object[] cells) {
		// TODO Auto-generated method stub
		return super.exportCells(cells);
	}

	@Override
	public void importCells(CellBuffer buffer) {
		// TODO Auto-generated method stub
		super.importCells(buffer);
	}

	@Override
	public void addNode() {

		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			Object cell = graph.insertVertex(null, null, createNodeData(), 
					40, 40, 40, 25);
			
			graph.cellSizeUpdated(cell, false);

			if(graphStyle!=null) {
				model.setStyle(cell, graphStyle.getStyle(this, cell, null));
			}
			
			graph.setSelectionCell(cell);
		} finally {
			model.endUpdate();
		}
	}

	@Override
	public void addEdge(Object source, Object target, boolean orderEdge) {

		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			Object value = orderEdge ? Order.BEFORE : createEdgeData();
			
			Object cell = graph.insertEdge(null, null, value, source, target);

			if(graphStyle!=null) {
				model.setStyle(cell, graphStyle.getStyle(this, cell, null));
			}
			if(graphLayout!=null) {
				model.setStyle(cell, graphLayout.getEdgeStyle(this, cell, null));
			}
			
			graph.setSelectionCell(cell);
		} finally {
			model.endUpdate();
		}
	}

	public ContentType getConstraintTargetType() {
		return constraintTargetType;
	}

	public void setConstraintTargetType(ContentType constraintTargetType) {
		if(constraintTargetType==null)
			throw new IllegalArgumentException("Invalid constraint target type"); //$NON-NLS-1$
		
		if(this.constraintTargetType==constraintTargetType) {
			return;
		}
		
		ContentType oldValue = this.constraintTargetType;
		this.constraintTargetType = constraintTargetType;
		
		nodeConstraintFactories = null;
		edgeConstraintFactories = null;
		
		ConstraintFactory[] factories = SearchUtils.getConstraintFactories(constraintTargetType);
		if(factories!=null) {
			nodeConstraintFactories = SearchUtils.getNodeConstraintFactories(factories);
			edgeConstraintFactories = SearchUtils.getEdgeConstraintFactories(factories);
		}
		
		setCellEditor(createCellEditor());
		
		firePropertyChange("constraintTargetType", oldValue, constraintTargetType); //$NON-NLS-1$
	}

	public SearchGraph getSearchGraph() {
		return searchGraph;
	}

	public ConstraintFactory[] getNodeConstraintFactories() {
		return nodeConstraintFactories;
	}

	public ConstraintFactory[] getEdgeConstraintFactories() {
		return edgeConstraintFactories;
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class ConstraintGraph extends DelegatingGraph {

		public ConstraintGraph() {
			super(new mxGraphModel());
		}

		@Override
		public Object[] cloneCells(Object[] cells, boolean allowInvalidEdges) {
			if(cells==null || cells.length==0) {
				return cells;
			}
			
			// Skip entire cloning if there are invalid types contained
			// TODO check content and convert
			
			// Content check successful -> proceed with regular cloning
			return super.cloneCells(cells, allowInvalidEdges);
		}
	}
	
	protected class CConnectPreview extends DelegatingConnectPreview {

		@Override
		protected Object createCell(mxCellState startState, String style) {
			mxICell cell = (mxICell) super.createCell(startState, style);
			
			cell.setValue(createEdgeData());
			
			return cell;
		}
	}
}
