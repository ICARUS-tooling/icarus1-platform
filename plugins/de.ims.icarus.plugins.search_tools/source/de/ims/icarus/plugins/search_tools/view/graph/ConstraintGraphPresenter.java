/*
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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxIGraphModel.mxAtomicGraphModelChange;
import com.mxgraph.swing.handler.mxConnectPreview;
import com.mxgraph.swing.view.mxICellEditor;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.config.ConfigDelegate;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.LanguageManager;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.jgraph.cells.CompoundGraphNode;
import de.ims.icarus.plugins.jgraph.cells.GraphCell;
import de.ims.icarus.plugins.jgraph.layout.DefaultTreeLayout;
import de.ims.icarus.plugins.jgraph.layout.GraphLayout;
import de.ims.icarus.plugins.jgraph.layout.GraphLayoutConstants;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.jgraph.layout.GraphStyle;
import de.ims.icarus.plugins.jgraph.util.CellBuffer;
import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.ConstraintFactory;
import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.NodeType;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchNode;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultGraphEdge;
import de.ims.icarus.search_tools.standard.DefaultGraphNode;
import de.ims.icarus.search_tools.standard.DefaultSearchGraph;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Order;
import de.ims.icarus.util.annotation.AnnotationControl;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.DataConversionException;
import de.ims.icarus.util.data.DataConverter;

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
	
	protected boolean disjuntiveRoots = false;
	
	public static boolean isLinkEdge(mxIGraphModel model, Object cell) {
		if(model.isVertex(cell)) {
			return false;
		}
		Object value = model.getValue(cell);
		if(!(value instanceof ConstraintEdgeData)) {
			return false;
		}
		
		return ((ConstraintEdgeData)value).getEdgeType()==EdgeType.LINK;
	}
	
	public static boolean isLinkEdge(mxCellState state) {
		return isLinkEdge(state.getView().getGraph().getModel(), state.getCell());
	}
	
	public static boolean isLinkEdge(GraphOwner owner, Object cell) {
		return isLinkEdge(owner.getGraph().getModel(), cell);
	}
	
	public static boolean isDisjunctionNode(mxIGraphModel model, Object cell) {
		if(model.isEdge(cell)) {
			return false;
		}
		Object value = model.getValue(cell);
		if(!(value instanceof ConstraintNodeData)) {
			return false;
		}
		
		return ((ConstraintNodeData)value).getNodeType()==NodeType.DISJUNCTION;
	}
	
	public static boolean isDisjunctionNode(mxCellState state) {
		return isDisjunctionNode(state.getView().getGraph().getModel(), state.getCell());
	}
	
	public static boolean isDisjunctionNode(GraphOwner owner, Object cell) {
		return isDisjunctionNode(owner.getGraph().getModel(), cell);
	}
	
	public ConstraintGraphPresenter() {
		// no-op
	}

	/**
	 * This presenter 'creates' constraints that themselves result in
	 * annotations, so there is no reason to provide navigable 
	 * visualization of annotation data here.
	 * 
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#createAnnotationControl()
	 */
	@Override
	protected AnnotationControl createAnnotationControl() {
		return null;
	}

	@Override
	protected CallbackHandler createCallbackHandler() {
		return new CCallbackHandler();
	}
	
	@Override
	protected void loadPreferences() {
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		setAutoZoomEnabled(config.getBoolean(
				"plugins.jgraph.appearance.constraints.autoZoom")); //$NON-NLS-1$
		setCompressEnabled(config.getBoolean(
				"plugins.jgraph.appearance.constraints.compressGraph")); //$NON-NLS-1$
	}

	@Override
	protected void initGraphComponentInternals() {
		super.initGraphComponentInternals();
		
		editableMainToolBarListId = "plugins.searchTools.constraintGraphPresenter.editableMainToolBarList"; //$NON-NLS-1$
		editablePopupMenuListId = "plugins.searchTools.constraintGraphPresenter.editablePopupMenuList"; //$NON-NLS-1$
		
		setMinimumNodeSize(new Dimension(50, 25));
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
	protected GraphStyle createDefaultGraphStyle() {
		return new ConstraintGraphStyle();
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

	@Override
	protected ActionManager createActionManager() {
		ActionManager actionManager = super.createActionManager();

		// Load default actions
		URL actionLocation = ConstraintGraphPresenter.class.getResource(
				"constraint-graph-presenter-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: constraint-graph-presenter-actions.xml"); //$NON-NLS-1$
		try {
			actionManager.loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to load actions from file: "+actionLocation, e); //$NON-NLS-1$
		}
		
		return actionManager;
	}

	@Override
	protected void registerActionCallbacks() {
		super.registerActionCallbacks();

		ActionManager actionManager = getActionManager();

		// Init 'selected' states
		actionManager.setSelected(isDisjuntiveRoots(), 
				"plugins.searchTools.constraintGraphPresenter.toggleRootOperatorAction"); //$NON-NLS-1$
		
		// Register callback functions
		actionManager.addHandler(
				"plugins.searchTools.constraintGraphPresenter.addDisjunctionAction",  //$NON-NLS-1$
				callbackHandler, "addDisjunction"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.searchTools.constraintGraphPresenter.toggleRootOperatorAction",  //$NON-NLS-1$
				callbackHandler, "toggleRootOperator"); //$NON-NLS-1$
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return searchGraph!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return searchGraph;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(SearchGraph.class);
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#setData(java.lang.Object, de.ims.icarus.util.Options)
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
		
		boolean disjuntive = newGraph!=null && newGraph.getRootOperator()==SearchGraph.OPERATOR_DISJUNCTION;
		getActionManager().setSelected(disjuntive, 
				"plugins.searchTools.constraintGraphPresenter.toggleRootOperatorAction"); //$NON-NLS-1$
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
	
	protected ConstraintNodeData createNodeData(SearchNode source, Map<String, Integer> constraintMap) {
		ConstraintNodeData nodeData = createNodeData();
		nodeData.setId(source.getId());
		nodeData.setNegated(source.isNegated());
		nodeData.setNodeType(source.getNodeType());
		
		nodeData.setConstraints(source.getConstraints(), constraintMap);
		
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
	
	protected ConstraintEdgeData createEdgeData(SearchEdge source, Map<String, Integer> constraintMap) {
		ConstraintEdgeData edgeData = createEdgeData();
		edgeData.setId(source.getId());
		edgeData.setNegated(source.isNegated());
		edgeData.setEdgeType(source.getEdgeType());
		
		edgeData.setConstraints(source.getConstraints(), constraintMap);
		
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

	@Override
	public boolean isLinkEdge(Object cell) {
		return isLinkEdge(this, cell);
	}
	
	protected Map<String, Integer> getConstraintMap() {		
		Map<String, Integer> map = new HashMap<>();

		List<ConstraintFactory> edgeFactories = getConstraintContext().getEdgeFactories();
		for(int i=0; i<edgeFactories.size(); i++) {
			map.put(edgeFactories.get(i).getToken(), i);
		}

		List<ConstraintFactory> nodeFactories = getConstraintContext().getNodeFactories();
		for(int i=0; i<nodeFactories.size(); i++) {
			map.put(nodeFactories.get(i).getToken(), i);
		}
		
		return map;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#syncToGraph()
	 */
	@Override
	protected void syncToGraph() {

		Map<String, Integer> constraintMap = getConstraintMap();

		mxIGraphModel model = graph.getModel();
		pauseGraphChangeHandling();
		model.beginUpdate();
		try {
			// Clear graph
			GraphUtils.clearGraph(graph);
			
			if(SearchUtils.isEmpty(searchGraph)) {
				return;
			}
			
			double x = 2*graph.getGridSize();
			double y = 2*graph.getGridSize();
			
			Map<SearchNode, Object> cellMap = new HashMap<>();
			
			for(SearchNode root : searchGraph.getRootNodes()) {
				mxRectangle bounds = layoutNode(root, x, y, null, null, cellMap, constraintMap);
				x += bounds.getWidth() + graph.getGridSize();
			}
			
			for(SearchEdge edge : searchGraph.getEdges()) {
				if(edge.getEdgeType()==EdgeType.LINK 
						|| edge.getEdgeType()==EdgeType.PRECEDENCE) {
					ConstraintEdgeData edgeData = new ConstraintEdgeData(edge);
					graph.insertEdge(null, null, edgeData, 
							cellMap.get(edge.getSource()), cellMap.get(edge.getTarget()));
				}
			}
		} finally {
			model.endUpdate();
			resumeGraphChangeHandling();
		}
	}
	
	protected mxRectangle layoutNode(SearchNode sourceNode, double x, double y, 
			SearchEdge sourceEdge, Object parent, Map<SearchNode, Object> cellMap,
			Map<String, Integer> constraintMap) {
		// Add new vertex
		ConstraintNodeData nodeData = createNodeData(sourceNode, constraintMap);
		Object cell = graph.insertVertex(null, null, nodeData, x, y, 50, 25);
		graph.cellSizeUpdated(cell, false);
		cellMap.put(sourceNode, cell);
		
		mxRectangle bounds = new mxRectangle(graph.getCellGeometry(cell));
		
		if(sourceEdge!=null && parent!=null) {
			ConstraintEdgeData edgeData = createEdgeData(sourceEdge, constraintMap);
			graph.insertEdge(parent, null, edgeData, parent, cell);
		}
		
		y+= bounds.getHeight()+2*graph.getGridSize();
		
		for(int i=0; i<sourceNode.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = sourceNode.getOutgoingEdgeAt(i);
			if(edge.getEdgeType()==EdgeType.DOMINANCE
					|| edge.getEdgeType()==EdgeType.TRANSITIVE) {
				mxRectangle childBounds = layoutNode(
						edge.getTarget(), x, y, edge, cell, cellMap, constraintMap);
				
				bounds.add(childBounds);
				
				x+= childBounds.getWidth()+graph.getGridSize();
			}
		}
		
		return bounds;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#syncToData()
	 */
	@Override
	protected void syncToData() {
		searchGraph = (DefaultSearchGraph) snapshot();
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
		List<DefaultGraphNode> roots = new ArrayList<>();
		
		// Create and map nodes and edges
		for(int i=0; i<cellCount; i++) {
			Object cell = model.getChildAt(parent, i);
			if(model.isVertex(cell)) {
				ConstraintNodeData nodeData = (ConstraintNodeData)model.getValue(cell);
				DefaultGraphNode node = new DefaultGraphNode();
				node.setNegated(nodeData.isNegated());
				node.setNodeType(nodeData.getNodeType());
				node.setConstraints(nodeData.getConstraints());
				node.setId(nodeData.getId());
				
				searchNodes.put(cell, node);
				
				// Check for roots
				if(getIncomingEdgeCount(model, cell, true, false)==0) {
					roots.add(node);
				}
				
			} else if(model.isEdge(cell)) {
				ConstraintEdgeData edgeData = (ConstraintEdgeData)model.getValue(cell);
				DefaultGraphEdge edge = new DefaultGraphEdge();
				edge.setNegated(edgeData.isNegated());
				edge.setEdgeType(edgeData.getEdgeType());
				edge.setConstraints(edgeData.getConstraints());
				edge.setId(edgeData.getId());
				
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
					if(searchEdges.containsKey(edge)) {
						node.addEdge(searchEdges.get(edge), 
								model.getTerminal(edge, false)==cell);
					}
				}
			} else if(model.isEdge(cell)) {
				
				// Add terminals to edges
				DefaultGraphEdge edge = searchEdges.get(cell);
				edge.setSource(searchNodes.get(model.getTerminal(cell, true)));
				edge.setTarget(searchNodes.get(model.getTerminal(cell, false)));
			}
		}
		
		/*List<Object> roots = graph.findTreeRoots(parent);
		List<SearchNode> rootNodes = new ArrayList<>();
		for(Object root : roots) {
			rootNodes.add(searchNodes.get(root));
		}*/
		
		// Now wrap everything into a graph object
		DefaultSearchGraph searchGraph = new DefaultSearchGraph();		
		searchGraph.setRootNodes(roots.toArray(new SearchNode[0]));
		searchGraph.setNodes(searchNodes.values().toArray(new SearchNode[0]));
		searchGraph.setEdges(searchEdges.values().toArray(new SearchEdge[0]));
		
		int operator = isDisjuntiveRoots() ? SearchGraph.OPERATOR_DISJUNCTION : 
			SearchGraph.OPERATOR_CONJUNCTION;
		searchGraph.setRootOperator(operator);
		
		return searchGraph;
	}
	
	protected int getIncomingEdgeCount(mxIGraphModel model, Object cell, boolean includeNormal, boolean includeOrder) {
		if(!includeNormal && !includeOrder) {
			includeNormal = includeOrder = true;
		}
		
		int count = 0;
		
		int edgeCount = model.getEdgeCount(cell);
		for(int i=0; i<edgeCount; i++) {
			Object edge = model.getEdgeAt(cell, i);
			boolean isOrder = isOrderEdge(edge);
			
			if(isOrder!=includeOrder && isOrder==includeNormal) {
				continue;
			}
			
			if(model.getTerminal(edge, false)==cell) {
				count++;
			}
		}
		
		return count;
	}

	@Override
	protected mxGraph createGraph() {
		return new ConstraintGraph();
	}

	@Override
	public CellBuffer exportCells(Object[] cells) {
		return cells!=null ? 
				CellBuffer.createBuffer(cells, graph.getModel(), CELL_DATA_TYPE)
				: CellBuffer.createBuffer(graph.getModel(),	null, CELL_DATA_TYPE);
	}

	@Override
	public void importCells(CellBuffer buffer) {
		Object[] cells = CellBuffer.buildCells(buffer);
		if(cells==null) {
			return;
		}
		
		importCells(cells, 0, 0, null, null);
	}

	@Override
	public Object[] importCells(Object[] cells, double dx, double dy,
			Object target, Point location) {
		Object[] result = super.importCells(cells, dx, dy, target, location);
		
		if(result!=null) {
			refreshAll();
		}
		
		return result;
	}

	@Override
	public void cloneCells(Object[] cells) {
		importCells(cells, 40, 40, null, null);
	}

	@Override
	public void addNode() {

		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			Object cell = graph.insertVertex(null, null, createNodeData(), 
					40, 40, 50, 25);
			
			//graph.cellSizeUpdated(cell, false);

			if(graphStyle!=null) {
				model.setStyle(cell, graphStyle.getStyle(this, cell, null));
			}
			
			graph.setSelectionCell(cell);
		} finally {
			model.endUpdate();
		}
	}
	public void addDisjunction() {

		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			
			ConstraintNodeData nodeData = createNodeData();
			nodeData.setNodeType(NodeType.DISJUNCTION);
			Object cell = graph.insertVertex(null, null, nodeData, 
					40, 40, 25, 25);
			
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
			ConstraintEdgeData value = createEdgeData();
			if(orderEdge) {
				value.setEdgeType(EdgeType.PRECEDENCE);
			}
			
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

	public boolean isDisjuntiveRoots() {
		return disjuntiveRoots;
	}

	public void setDisjuntiveRoots(boolean disjuntiveRoots) {
		if(disjuntiveRoots==this.disjuntiveRoots) {
			return;
		}
		
		boolean oldValue = this.disjuntiveRoots;
		this.disjuntiveRoots = disjuntiveRoots;
		
		if(searchGraph!=null) {
			int operator = disjuntiveRoots ? SearchGraph.OPERATOR_DISJUNCTION : 
				SearchGraph.OPERATOR_CONJUNCTION;
			searchGraph.setRootOperator(operator);
		}
		
		firePropertyChange("disjunctiveRoots", oldValue, disjuntiveRoots); //$NON-NLS-1$
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
		
		protected Object createValue(Object cell, Object data, Map<String, Integer> constraintMap) {
			if(data==null) {
				return model.isVertex(cell) ?
						createNodeData() : createEdgeData();
			} else if(model.isVertex(cell)) {
				return createNodeData((SearchNode) data, constraintMap);
			} else {
				return createEdgeData((SearchEdge) data, constraintMap);
			}
		}

		@Override
		public Object[] cloneCells(Object[] cells, boolean allowInvalidEdges) {
			if(cells==null || cells.length==0) {
				return cells;
			}
			
			// Skip entire cloning if there are invalid types contained
			Map<Object, Object> originalValues = new HashMap<>();
			for(Object cell : cells) {
				originalValues.put(cell, model.getValue(cell));
			}

			Options options = new Options();
			options.put(Options.CONTEXT, getConstraintContext());
			
			for(Object cell : cells) {
				Object value = originalValues.get(cell);
				if(value instanceof ConstraintCellData) {
					continue;
				}
				
				if(model.isEdge(cell)) {
					Object targetValue = originalValues.get(model.getTerminal(cell, false));
					if(targetValue instanceof CompoundGraphNode) {
						value = targetValue;
					}
				}
				
				Map<String, Integer> constraintMap = getConstraintMap();
				
				Object newValue = null;
				
				if(value instanceof GraphCell) {
					ContentType inputType = ContentTypeRegistry.getInstance().getEnclosingType(value);
					ContentType resultType = SearchUtils.getConstraintCellContentType();
					DataConverter converter = ContentTypeRegistry.getInstance().getConverter(inputType, resultType);
					
					if(converter!=null) {
						options.put("isVertex", model.isVertex(cell)); //$NON-NLS-1$
						try {
							newValue = converter.convert(value, options);
						} catch (DataConversionException e) {
							LoggerFactory.log(this, Level.SEVERE, 
									"Failed to convert cell value: "+String.valueOf(value), e); //$NON-NLS-1$
						}
					}
				}
				
				if(value==newValue) {
					continue;
				}
				
				model.setValue(cell, createValue(cell, newValue, constraintMap));
			}
			
			// Content check successful -> proceed with regular cloning
			return super.cloneCells(cells, allowInvalidEdges);
		}

		@Override
		public mxRectangle getPreferredSizeForCell(Object cell) {
			if(isDisjunctionNode(getModel(), cell)) {
				Dimension maxSize = getMaximumNodeSize();
				setMaximumNodeSize(ConstraintGraphRenderer.disjunctionNodeSize);
				mxRectangle cellSize = super.getPreferredSizeForCell(cell);
				setMaximumNodeSize(maxSize);
				
				return cellSize;
			} else {
				return super.getPreferredSizeForCell(cell);
			}
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
	
	public class CCallbackHandler extends CallbackHandler {
		
		public void addDisjunction(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				ConstraintGraphPresenter.this.addDisjunction();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add disjunction", ex); //$NON-NLS-1$
			}
		}

		public void toggleRootOperator(boolean b) {
			setDisjuntiveRoots(b);
		}

		public void toggleRootOperator(ActionEvent e) {
			// ignore
		}
	}
}
