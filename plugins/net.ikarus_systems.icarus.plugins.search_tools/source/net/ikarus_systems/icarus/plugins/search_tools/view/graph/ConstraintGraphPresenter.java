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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.ikarus_systems.icarus.config.ConfigDelegate;
import net.ikarus_systems.icarus.language.LanguageManager;
import net.ikarus_systems.icarus.plugins.jgraph.layout.DefaultGraphLayout;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayoutConstants;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphRenderer;
import net.ikarus_systems.icarus.plugins.jgraph.util.CellBuffer;
import net.ikarus_systems.icarus.plugins.jgraph.util.GraphUtils;
import net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter;
import net.ikarus_systems.icarus.search_tools.ConstraintContext;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;
import net.ikarus_systems.icarus.search_tools.standard.DefaultGraphEdge;
import net.ikarus_systems.icarus.search_tools.standard.DefaultGraphNode;
import net.ikarus_systems.icarus.search_tools.standard.DefaultSearchGraph;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.Order;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxIGraphModel.mxAtomicGraphModelChange;
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
	
	public static final String CELL_DATA_TYPE = ConstraintCellData.class.getName();

	private static final long serialVersionUID = -674508666642816142L;
	
	protected DefaultSearchGraph searchGraph;
	
	protected ConstraintContext constraintContext;
	
	public ConstraintGraphPresenter() {
		// no-op
	}

	@Override
	protected void initGraphComponentInternals() {
		super.initGraphComponentInternals();
		
		setMinimumNodeSize(new Dimension(75, 20));
	}
	
	protected ConstraintContext createDefaultContext() {
		ContentType contentType = LanguageManager.getInstance().getSentenceDataContentType();
		return SearchManager.getInstance().getConstraintContext(contentType);
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
		return new DefaultGraphLayout();
	}

	@Override
	protected ConfigDelegate createConfigDelegate() {
		return new GraphConfigDelegate("plugins.jgraph.appearance.constraints", null); //$NON-NLS-1$
	}

	@Override
	protected mxConnectPreview createConnectPreview() {
		return new CConnectPreview();
	}

	@Override
	protected Options createLayoutOptions() {
		Options options = super.createLayoutOptions();
		if(options==null) {
			options = new Options();
		}
		
		// Apply new settings
		options.put(GraphLayoutConstants.MIN_BASELINE_KEY, 80);
		
		return options;
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
		DefaultSearchGraph newGraph = null;
		if(data instanceof DefaultSearchGraph) {
			newGraph = (DefaultSearchGraph) data;
		} else if(data instanceof SearchGraph) {
			SearchGraph searchGraph = (SearchGraph) data;
			
			newGraph = new DefaultSearchGraph();
			newGraph.setEdges(searchGraph.getEdges());
			newGraph.setNodes(searchGraph.getNodes());
			newGraph.setRootNodes(newGraph.getRootNodes());
		}
		
		searchGraph = newGraph;
	}
	
	protected ConstraintNodeData createNodeData() {
		List<ConstraintFactory> factories = getConstraintContext().getNodeFactories();
		
		ConstraintNodeData nodeData = new ConstraintNodeData(factories.size());
		for(int i=0; i<factories.size(); i++) {
			ConstraintFactory factory = factories.get(i);
			SearchOperator operator = factory.getSupportedOperators()[0];
			Object value = factory.getDefaultValue();
			
			nodeData.setConstraint(i, new DefaultConstraint(factory.getToken(), value, operator));
		}
				
		return nodeData;
	}
	
	protected ConstraintEdgeData createEdgeData() {
		List<ConstraintFactory> factories = getConstraintContext().getEdgeFactories();
		
		ConstraintEdgeData edgeData = new ConstraintEdgeData(factories.size());
		for(int i=0; i<factories.size(); i++) {
			ConstraintFactory factory = factories.get(i);
			SearchOperator operator = factory.getSupportedOperators()[0];
			Object value = factory.getDefaultValue();
			
			edgeData.setConstraint(i, new DefaultConstraint(factory.getToken(), value, operator));
		}
				
		return edgeData;
	}

	@Override
	public boolean isOrderEdge(Object cell) {
		Object value = graph.getModel().getValue(cell);
		if(value instanceof Order) {
			return true;
		}
		if(value instanceof ConstraintEdgeData) {
			return ((ConstraintEdgeData)value).getEdgeType()==EdgeType.PRECEDENCE;
		}
		return false;
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
		pauseChangeHandling();
		try {
			searchGraph = (DefaultSearchGraph) snapshot();
		} finally {
			resumeChangeHandling();
		}
	}
	
	/**
	 * Creates a {@code SearchGraph} that represents the current
	 * visual state of the {@code mxGraph} this presenter is using.
	 */
	public SearchGraph snapshot() {
		mxIGraphModel model = graph.getModel();
		Object parent = graph.getDefaultParent();
		int cellCount = model.getChildCount(parent);
		
		if(cellCount==0) {
			return null;
		}
		
		Map<Object, DefaultGraphNode> searchNodes = new LinkedHashMap<>();
		Map<Object, DefaultGraphEdge> searchEdges = new LinkedHashMap<>();
		
		// Create and map nodes and edges
		for(int i=0; i<cellCount; i++) {
			Object cell = model.getChildAt(parent, i);
			if(model.isVertex(cell)) {
				ConstraintNodeData nodeData = (ConstraintNodeData)model.getValue(cell);
				DefaultGraphNode node = new DefaultGraphNode();
				node.setNegated(nodeData.isNegated());
				node.setNodeType(nodeData.getNodeType());
				node.setConstraints(nodeData.getConstraints());
				
				searchNodes.put(cell, node);
			} else if(model.isEdge(cell)) {
				ConstraintEdgeData edgeData = (ConstraintEdgeData)model.getValue(cell);
				DefaultGraphEdge edge = new DefaultGraphEdge();
				edge.setNegated(edgeData.isNegated());
				edge.setEdgeType(edgeData.getEdgeType());
				edge.setConstraints(edgeData.getConstraints());
				
				searchEdges.put(cell, edge);
			}
		}
		
		// Now do the mapping
		for(int i=0; i<cellCount; i++) {
			Object cell = model.getChildAt(parent, i);
			if(model.isVertex(cell)) {
				
				// Connect edges to vertices
				DefaultGraphNode node = searchNodes.get(cell);
				int edgeCount = model.getEdgeCount(cell);
				for(int j=0; j<edgeCount; j++) {
					Object edge = model.getEdgeAt(cell, j);
					node.addEdge(searchEdges.get(edge), 
							model.getTerminal(edge, false)==cell);
				}
			} else if(model.isEdge(cell)) {
				
				// Add terminals to edges
				DefaultGraphEdge edge = searchEdges.get(cell);
				edge.setSource(searchNodes.get(model.getTerminal(cell, true)));
				edge.setTarget(searchNodes.get(model.getTerminal(cell, false)));
			}
		}
		
		List<Object> roots = graph.findTreeRoots(parent);
		List<SearchNode> rootNodes = new ArrayList<>();
		for(Object root : roots) {
			rootNodes.add(searchNodes.get(root));
		}
		
		// Now wrap everything into a graph object
		DefaultSearchGraph searchGraph = new DefaultSearchGraph();		
		searchGraph.setRootNodes(rootNodes.toArray(new SearchNode[0]));
		searchGraph.setNodes(searchNodes.values().toArray(new SearchNode[0]));
		searchGraph.setEdges(searchEdges.values().toArray(new SearchEdge[0]));
		
		return searchGraph;
	}

	@Override
	protected mxGraph createGraph() {
		// TODO Auto-generated method stub
		return super.createGraph();
	}

	@Override
	public CellBuffer exportCells(Object[] cells) {
		return cells!=null ? 
				CellBuffer.createBuffer(cells, graph.getModel(), CELL_DATA_TYPE)
				: CellBuffer.createBuffer(graph.getModel(),	null, CELL_DATA_TYPE);
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

		if(GraphUtils.isAncestor(graph.getModel(), source, target, !orderEdge, orderEdge)) {
			return;
		}
		
		model.beginUpdate();
		try {
			Object value = orderEdge ? Order.BEFORE : createEdgeData();
			
			Object cell = graph.insertEdge(null, null, value, source, target);
			
			if(enforceTree && !orderEdge) {
				ensureTree(cell);
			}

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

	public boolean isEnforceTree() {
		return enforceTree;
	}

	public void setEnforceTree(boolean enforceTree) {
		if(this.enforceTree==enforceTree) {
			return;
		}
		
		boolean oldValue = this.enforceTree;
		this.enforceTree = enforceTree;
		
		firePropertyChange("enforceTree", oldValue, enforceTree); //$NON-NLS-1$
	}

	public ConstraintContext getConstraintContext() {
		return constraintContext;
	}

	public void setConstraintContext(ConstraintContext context) {
		if(context==null)
			throw new IllegalArgumentException("Invalid context"); //$NON-NLS-1$
		
		if(this.constraintContext==context) {
			return;
		}
		
		graph.getModel().beginUpdate();
		try {
			executeChange(new ContextChange(context));
		} finally {
			graph.getModel().endUpdate();
		}
	}

	public SearchGraph getData() {
		return searchGraph;
	}
	
	protected void ensureTree(Object newEdge) {
		mxIGraphModel model = graph.getModel();
		
		model.beginUpdate();
		try {			
			
			Object cell = model.getTerminal(newEdge, false);
			
			// Remove incoming edges other than 'newEdge'
			for(int i=0; i<model.getEdgeCount(cell); i++) {
				Object edge = model.getEdgeAt(cell, i);
				
				// Ignore outgoing and order edges
				if(edge==newEdge || isOrderEdge(edge) 
						|| cell==model.getTerminal(edge, true)) {
					continue;
				}
				
				model.remove(edge);
				i--;
			}
		} finally {
			model.endUpdate();
		}
	}
	
	protected class ContextChange extends mxAtomicGraphModelChange {
		
		protected ConstraintContext context, previous;
		
		public ContextChange(ConstraintContext context) {
			this.context = context;
			this.previous = this.context;
		}

		/**
		 * @see com.mxgraph.util.mxUndoableEdit.mxUndoableChange#execute()
		 */
		@Override
		public void execute() {
			context = previous;
			previous = constraintContext;

			
			constraintContext = context;
			
			// Erase all content when context is switched
			clearGraph();
			
			firePropertyChange("constraintContext", previous, context); //$NON-NLS-1$
		}
		
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

		@Override
		public Object stop(boolean commit, MouseEvent e) {
			Object result = (sourceState != null) ? sourceState.getCell() : null;
			
			if(commit && enforceTree) {
				if(previewState!=null ) {
					mxIGraphModel model = graph.getModel();
					
					// TODO access model only when committing?
					model.beginUpdate();
					try {
						result = super.stop(commit, e);
						
						if(commit && result!=null) {
							ensureTree(result);
						}
					} finally {
						model.endUpdate();
					}
				}
			} else {
				result = super.stop(commit, e);
			}
			
			
			return result;
		}		
	}
}
