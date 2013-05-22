/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency.graph;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.ikarus_systems.icarus.config.ConfigDelegate;
import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.language.SentenceDataEvent;
import net.ikarus_systems.icarus.language.SentenceDataListener;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.DependencyDataEvent;
import net.ikarus_systems.icarus.language.dependency.DependencyNodeData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.dependency.MutableDependencyData;
import net.ikarus_systems.icarus.language.dependency.SimpleDependencyData;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayoutConstants;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphRenderer;
import net.ikarus_systems.icarus.plugins.jgraph.util.CellBuffer;
import net.ikarus_systems.icarus.plugins.jgraph.util.GraphUtils;
import net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter;
import net.ikarus_systems.icarus.util.CollectionUtils;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.Order;
import net.ikarus_systems.icarus.util.data.ContentType;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
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
public class DependencyGraphPresenter extends GraphPresenter {
	
	private static final long serialVersionUID = -8262542126697438425L;
	
	public static final String NODE_DATA_TYPE = DependencyNodeData.class.getName();
	
	protected MutableDependencyData data;
	
	protected boolean refreshGraphOnChange = true;

	public DependencyGraphPresenter() {
		// no-op
	}

	/**
	 * 
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#createGraph()
	 */
	@Override
	protected mxGraph createGraph() {
		DGraph graph = new DGraph();
		
		// TODO modify graph
		
		return graph;
	}

	@Override
	protected Handler createHandler() {
		return new DHandler();
	}

	@Override
	protected CallbackHandler createCallbackHandler() {
		return new DCallbackHandler();
	}

	@Override
	protected mxICellEditor createCellEditor() {
		return new DependencyCellEditor(this);
	}

	@Override
	protected mxConnectPreview createConnectPreview() {
		return new DConnectPreview();
	}

	@Override
	protected GraphRenderer createDefaultGraphRenderer() {
		return new DependencyGraphRenderer();
	}
	
	@Override
	protected ConfigDelegate createConfigDelegate() {
		return new GraphConfigDelegate("plugins.jgraph.appearance.dependency", null); //$NON-NLS-1$
	}

	public boolean isRefreshGraphOnChange() {
		return refreshGraphOnChange;
	}

	public void setRefreshGraphOnChange(boolean refreshGraphOnChange) {
		if(this.refreshGraphOnChange==refreshGraphOnChange) {
			return;
		}
		
		boolean oldValue = this.refreshGraphOnChange;
		this.refreshGraphOnChange = refreshGraphOnChange;
		
		firePropertyChange("refreshGraphOnChange", oldValue, refreshGraphOnChange); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return data;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();
	}
	
	public MutableDependencyData getData() {
		return data;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#setData(java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	protected void setData(Object data, Options options) {		
		MutableDependencyData newData = null;
		if(data instanceof MutableDependencyData) {
			newData = (MutableDependencyData)data;
		} else {
			newData = new MutableDependencyData();
			if(data!=null) {
				newData.copyFrom((DependencyData)data);
			}
		}
		
		MutableDependencyData oldData = getData();
		if(oldData!=null) {
			oldData.removeSentenceDataListener((SentenceDataListener) getHandler());
		}
		
		this.data = newData;
		
		if(newData!=null) {
			newData.addSentenceDataListener((SentenceDataListener) getHandler());
		}
	}

	@Override
	protected Options createLayoutOptions() {
		if(isCompressEnabled()) {
			return new Options(GraphLayoutConstants.CELL_MERGER_KEY, new DependencyCellMerger());
		} else {
			return null;
		}
	}

	protected mxCell createVertex(Object item, String id, double x, double y) {
		mxCell cell = new mxCell(item);
		cell.setId(id);
		cell.setStyle("defaultVertex"); //$NON-NLS-1$
		
		cell.setGeometry(new mxGeometry(x, y, 50, 24));
		cell.setVertex(true);
		cell.setConnectable(true);

		return cell;
	}

	protected mxCell createEdge(Object value, String id, Object source, Object target) {
		mxCell cell = new mxCell(value);
		cell.setEdge(true);
		cell.setId(id);
		cell.setStyle("defaultEdge"); //$NON-NLS-1$
		
		mxGeometry geo = new mxGeometry();
		geo.setRelative(true);
		cell.setGeometry(geo);

		return cell;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#syncToGraph()
	 */
	@Override
	protected void syncToGraph() {
		
		mxIGraphModel model = graph.getModel();
		pauseGraphChangeHandling();
		model.beginUpdate();
		try {
			// Clear graph
			GraphUtils.clearGraph(graph);

			Object parent = graph.getDefaultParent();
			
			// Just abort if nothing to display
			if(data==null || data.isEmpty()) {
				return;
			}
			
			Object[] vertices = new Object[data.length()];
			double x = 2*graph.getGridSize();
			double y = 2*graph.getGridSize();
			
			// Add vertices
			for(int i=0; i<data.length(); i++) {
				DependencyNodeData nodeData = new DependencyNodeData(data, i);
				Object cell = createVertex(nodeData, "node"+i, x, y); //$NON-NLS-1$
				
				x += model.getGeometry(cell).getWidth()+ graph.getGridSize();
				graph.addCell(cell);
				
				vertices[i] = cell;
			}
			
			// Add edges
			for(int i=0; i<data.length(); i++) {
				int head = data.getHead(i);
				if(LanguageUtils.isUndefined(head) || LanguageUtils.isRoot(head)) {
					continue;
				}
				
				Object source = vertices[head];
				Object target = vertices[i];
				
				Object edge = createEdge(data.getRelation(i), "head"+i,  //$NON-NLS-1$
						model.getValue(source), model.getValue(target));
				
				graph.addEdge(edge, parent, source, target, null);
			}
		} finally {
			model.endUpdate();
			resumeGraphChangeHandling();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter#syncToData()
	 */
	@Override
	protected void syncToData() {
		pauseChangeHandling();
		try {
			DependencyData snapshot = snapshot();
			
			// Empty graph -> clear data and abort
			if(snapshot==null) {
				data.clear();
			} else {
				data.copyFrom(snapshot);
			}
		} finally {
			resumeChangeHandling();
		}
	}
	
	public DependencyData snapshot() {
		Object parent = graph.getDefaultParent();
		mxIGraphModel model = graph.getModel();
		int childCount = model.getChildCount(parent);
		
		if(childCount==0) {
			return null;
		}
		
		// Collect all nodes including compressed ones
		List<DependencyNodeData> nodes = new ArrayList<>();
		for(int i=0; i<childCount; i++) {
			Object cell = model.getChildAt(parent, i);
			
			if(model.isEdge(cell)) {
				// Theoretically we can break the loop here since we
				// only insert edges after all the nodes have been added?
				// TODO verify need for further traversal
				continue;
			}
			
			DependencyNodeData data = (DependencyNodeData) model.getValue(cell);
			nodes.add(data);
			nodes.addAll(data.getChildren(false));
		}
		
		// Sort nodes by index
		Collections.sort(nodes, DependencyNodeData.INDEX_SORTER);
		
		int size = nodes.size();
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] poss = new String[size];
		String[] features = new String[size];
		String[] relations = new String[size];
		int[] heads = new int[size];
		long[] flags = new long[size];
		
		// Save node contents and construct sentence data wrapper
		for(int i=0; i<nodes.size(); i++) {
			DependencyNodeData data = nodes.get(i);
			
			forms[i] = data.getForm();
			lemmas[i] = data.getLemma();
			poss[i] = data.getPos();
			features[i] = data.getFeatures();
			relations[i] = data.getRelation();
			heads[i] = data.getHead();
			flags[i] = data.getFlags();
		}
		
		return new SimpleDependencyData(
				forms, lemmas, features, poss, relations, heads, flags);
	}

	/**
	 * Sorts vertices by index
	 */
	protected Comparator<Object> vertexSorter = new Comparator<Object>() {

		@Override
		public int compare(Object cellA, Object cellB) {
			DependencyNodeData a = (DependencyNodeData) graph.getModel().getValue(cellA);
			DependencyNodeData b = (DependencyNodeData) graph.getModel().getValue(cellB);

			return a.getIndex() == b.getIndex() ? 0 : 
				a.getIndex() > b.getIndex() ? 1 : -1;
		}
	};

	protected Object getCell(String id) {
		mxIGraphModel model = graph.getModel();
		if(model instanceof mxGraphModel) {
			return ((mxGraphModel)model).getCell(id);
		} else 
			throw new CorruptedStateException("Model not supporting id lookup: "+model.getClass()); //$NON-NLS-1$
	}
	
	protected void setId(Object cell, String id) {
		if(!(cell instanceof mxCell))
			throw new CorruptedStateException("Cell not supporting id: "+cell.getClass()); //$NON-NLS-1$
		((mxCell)cell).setId(id);
	}
	
	/**
	 * Clears the head of a node and returns the latest content.
	 */
	protected DependencyNodeData removeHead(Object cell, boolean removeEdges) {
		mxIGraphModel model = graph.getModel();
		DependencyNodeData oldValue = (DependencyNodeData) model.getValue(cell);
		if(!oldValue.hasHead()) {
			return oldValue;
		}

		// Create and set new value
		DependencyNodeData newValue = oldValue.clone();
		newValue.clearHead();
		
		model.beginUpdate();
		try {			
			model.setValue(cell, newValue);
			
			if(removeEdges) {
				// Remove incoming non-order edges
				for(Object edge : GraphUtils.getNonOrderEdges(model, cell, false, true)) {
					model.remove(edge);
				}
			}			
		} finally {
			model.endUpdate();
		}
		
		return newValue;
	}
	
	/**
	 * Replaces the head of a node with the index of another node's data.
	 * If the {@code removeEdges} parameter is {@code true} all incoming edges
	 * on {@code cell} that are not originating from {@code newHead} will be removed. 
	 */
	protected DependencyNodeData replaceHead(Object cell, Object newHead, boolean removeEdges) {
		mxIGraphModel model = graph.getModel();
		
		// Create and set new value
		DependencyNodeData newValue = ((DependencyNodeData) model.getValue(cell)).clone();		
		DependencyNodeData headData = (DependencyNodeData) model.getValue(newHead);
		
		newValue.setHead(headData.getIndex());
		
		model.beginUpdate();
		try {			
			model.setValue(cell, newValue);
			
			if(removeEdges) {
				// Remove incoming edges not originating from 'newHead'
				for(int i=0; i<model.getEdgeCount(cell); i++) {
					Object edge = model.getEdgeAt(cell, i);
					
					// Ignore outgoing and order edges
					if(GraphUtils.isOrderEdge(model, edge) 
							|| cell==model.getTerminal(edge, true)
							|| newHead==model.getTerminal(edge, true)) {
						continue;
					}
					
					model.remove(edge);
					i--;
				}
			}			
		} finally {
			model.endUpdate();
		}
		
		return newValue;
	}
	
	protected void handleDataChange(SentenceDataEvent evt) {
		if(isIgnoringChanges()) {
			return;
		}
		
		if(evt.getSource()!=data)
			throw new IllegalArgumentException("Foreign change source: "+evt.getSource()); //$NON-NLS-1$
		if(!(evt instanceof DependencyDataEvent))
			throw new IllegalArgumentException("Invalid event: "+evt.getClass()); //$NON-NLS-1$
		
		DependencyDataEvent event = (DependencyDataEvent)evt;
		
		boolean rebuild = isRefreshGraphOnChange();
		
		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {		
			if(!rebuild) {
				// Try to honor locality of changes
				switch (event.getType()) {
				case SentenceDataEvent.CHANGE_EVENT:
					// Entire data has changed
					rebuild = true;
					break;
	
				case SentenceDataEvent.INSERT_EVENT:
					// Too much trouble to find locations for new cells
					rebuild = true;
					break;
	
				case SentenceDataEvent.REMOVE_EVENT:
					// Maintaining cell id mapping becomes a mess when we try to
					// handle events without rebuilds
					rebuild = true;
					break;
	
				case SentenceDataEvent.UPDATE_EVENT:
					// Only case we can 'preserve' locality of required updates
					for(int i=event.getStartIndex(); i<event.getEndIndex(); i++) {
						Object cell = getCell("node"+i); //$NON-NLS-1$
						if(cell==null) {
							// Something went wrong since we should always
							// be up to date with our id mapping!
							// To solve this state we rebuild the entire graph
							rebuild = true;
							break;
						}
						
						DependencyNodeData oldvalue = (DependencyNodeData) model.getValue(cell);
						DependencyNodeData newValue = new DependencyNodeData(data, i);
						
						// Handle changed head
						if(newValue.getHead()!=oldvalue.getHead()) {
							Object edge = getCell("head"+i); //$NON-NLS-1$
							if(LanguageUtils.isUndefined(newValue.getHead())
									|| LanguageUtils.isRoot(newValue.getHead())) {
								// Head got removed
								model.remove(edge);
							} else if(LanguageUtils.isUndefined(oldvalue.getHead())
									|| LanguageUtils.isRoot(oldvalue.getHead())) {
								// Head got created
								Object source = getCell("node"+newValue.getHead()); //$NON-NLS-1$
								edge = createEdge(data.getRelation(i), "head"+i, model.getValue(source), newValue); //$NON-NLS-1$
								graph.addEdge(edge, graph.getDefaultParent(), source, cell, null);
							} else {
								// Reroute edge
								Object source = getCell("node"+newValue.getHead()); //$NON-NLS-1$
								model.setTerminal(edge, source, true);
							}
						}
						
						// Refresh data
						model.setValue(cell, newValue);
					}
					break;
				}
			}
			
			if(rebuild) {
				syncToGraph();
				refreshAll();
			}
		} finally {
			model.endUpdate();
		}
	}
	
	@Override
	public void deleteCells(Object[] cells) {
		if (!isEditable()) {
			return;
		}

		if (cells == null) {
			cells = graph.getSelectionCells();
		}
		if(cells.length==0) {
			return;
		}
		
		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			Set<Object> toDelete = CollectionUtils.asSet(cells);
			
			for(Object cell : cells) {
				Object[] edges = graph.getEdges(cell);
				for(Object edge : edges) {
					toDelete.add(edge);
					if(GraphUtils.isOrderEdge(model, edge)) {
						// Nothing special to do
						continue;
					}
					
					if(model.getTerminal(edge, false)==cell) {
						// Incoming edge -> source needs no update
						continue;
					}
					
					// Outgoing edge -> target needs update
					Object target = model.getTerminal(edge, false);
					if(toDelete.contains(target)) {
						// Target will be deleted anyway
						continue;
					}
					
					// Clear head
					removeHead(target, false);
				}
			}
			
			graph.removeCells(toDelete.toArray());
		} finally {
			model.endUpdate();
		}
	}

	@Override
	public void cloneCells(Object[] cells) {
		if(!canEdit()) {
			return;
		}
		
		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			double dx = graph.getGridSize();
			double dy = graph.getGridSize()*2;
			
			graph.moveCells(cells, dx, dy, true);
		} finally {
			model.endUpdate();
		}
	}

	@Override
	public void importCells(CellBuffer buffer) {
		if(!canEdit()) {
			return;
		}
		
		if(buffer==null)
			throw new IllegalArgumentException("Invalid cell buffer"); //$NON-NLS-1$
		
		if(!NODE_DATA_TYPE.equals(buffer.graphType)) {
			return;
		}
		
		Object[] cells = CellBuffer.buildCells(buffer);
		
		// TODO Verify content?
		
		if(cells==null || cells.length==0) {
			return;
		}
		
		graph.moveCells(cells, 0, 0, true);
	}

	@Override
	public CellBuffer exportCells(Object[] cells) {
		return cells!=null ? 
				CellBuffer.createBuffer(cells, graph.getModel(), NODE_DATA_TYPE)
				: CellBuffer.createBuffer(graph.getModel(),	null, NODE_DATA_TYPE);
	}

	@Override
	public void addNode() {
		if(!canEdit()) {
			return;
		}
		
		// Adding the dummy item causes an entire graph rebuild
		// Problem: this makes it impossible to select the new node
		data.addDummyItem();
	}

	@Override
	public void addEdge(Object source, Object target, boolean orderEdge) {
		if(!canEdit()) {
			return;
		}
		
		Object newEdge = null;
		
		// Add edge
		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			for(Object edge : graph.getEdgesBetween(source, target)) {
				// Allow only one edge of any kind between nodes
				if(GraphUtils.isOrderEdge(model, edge) == orderEdge) {
					return;
				}
			}
			
			Object value = orderEdge ? Order.BEFORE : LanguageUtils.DATA_UNDEFINED_LABEL;
			
			// Refresh target
			DependencyNodeData targetData = (DependencyNodeData)model.getValue(target);
			if(!orderEdge) {
				targetData = replaceHead(target, source, true);
			}
			String id = (orderEdge ? "order" : "head")+targetData.getIndex(); //$NON-NLS-1$ //$NON-NLS-2$
			
			newEdge = createEdge(value, id, model.getValue(source), model.getValue(target));
			graph.addEdge(newEdge, graph.getDefaultParent(), source, target, null);
			
			if(graphStyle!=null) {
				model.setStyle(newEdge, graphStyle.getStyle(this, newEdge, null));
			}
			if(graphLayout!=null) {
				model.setStyle(newEdge, graphLayout.getEdgeStyle(this, newEdge, null));
			}
		} finally {
			model.endUpdate();
		}
		
		if(newEdge!=null) {
			graph.setSelectionCell(newEdge);
		}
	}

	// TODO honestly: do we need this?
	public void flipEdge(Object edge) {
		if(!canEdit()) {
			return;
		}
		
		if(edge==null) {
			edge = graph.getSelectionCell();
		}
		
		mxIGraphModel model = graph.getModel();
		if(!model.isEdge(edge)) {
			return;
		}
		
		// Flip edge
		model.beginUpdate();
		try {
			// TODO
		} finally {
			model.endUpdate();
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DHandler extends Handler implements SentenceDataListener {

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataListener#dataChanged(net.ikarus_systems.icarus.language.SentenceDataEvent)
		 */
		@Override
		public void dataChanged(SentenceDataEvent event) {
			try {
				handleDataChange(event);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to handle data change", e); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DGraph extends DelegatingGraph {

		public DGraph() {
			super(new mxGraphModel());
		}

		@Override
		public Object[] cloneCells(Object[] cells, boolean allowInvalidEdges) {
			if(cells==null || cells.length==0) {
				return cells;
			}
			
			// Skip entire cloning if there are invalid types contained
			for(Object cell : cells) {
				if((model.isVertex(cell) && !(model.getValue(cell) instanceof DependencyNodeData))
						|| (model.isEdge(cell) && !(model.getValue(cell) instanceof String))) {
					return null;
				}
			}
			
			// Content check successful -> proceed with regular cloning
			return super.cloneCells(cells, allowInvalidEdges);
		}

		@Override
		public Object[] moveCells(Object[] cells, double dx, double dy,
				boolean clone, Object target, Point location) {
			
			// We only need to do special handling in case
			// the moved cells should be cloned cause this would
			// mess our indices on the node data objects
			if(!clone) {
				return super.moveCells(cells, dx, dy, clone, target, location);
			}
			
			if(cells==null || cells.length==0) {
				return cells;
			}
			
			if(target==null) {
				target = getDefaultParent();
			}
			
			mxIGraphModel model = getModel();
			
			model.beginUpdate();
			try {
				int nextIndex = 0;
				int childCount = model.getChildCount(target);
				for(int i=0; i<childCount; i++) {
					if(model.isVertex(model.getChildAt(target, i))) {
						nextIndex++;
					}
				}
				
				cells = super.moveCells(cells, dx, dy, clone, target, location);

				List<Object> vertices = new ArrayList<>(cells.length);
				for(Object cell : cells) {
					if(model.isVertex(cell)) {
						vertices.add(cell);
					}
				}
				
				Collections.sort(vertices, vertexSorter);
				
				// First pass: refresh index
				// Here each cell gets assigned a new value
				for(Object cell : vertices) {
					DependencyNodeData nodeData = (DependencyNodeData) model.getValue(cell);
					nodeData = nodeData.clone();
					nodeData.setIndex(nextIndex++);
					model.setValue(cell, nodeData);
				}
				
				// Second pass: refresh head
				// Values are already cloned, so only apply head modification
				cell_loop : for(Object cell : vertices) {
					DependencyNodeData nodeData = (DependencyNodeData) model.getValue(cell);
					
					int edgeCount = model.getEdgeCount(cell);
					for(int i=0; i<edgeCount; i++) {
						Object edge = model.getEdgeAt(cell, i);
						if(cell==model.getTerminal(edge, false)) {
							Object head = model.getTerminal(edge, true);
							DependencyNodeData headData = (DependencyNodeData) model.getValue(head);
							
							nodeData.setHead(headData.getIndex());
							
							continue cell_loop;
						}
					}
					
					// No incoming edge present -> make sure there is no weird
					// head value remaining on the cell!
					nodeData.clearHead();
				}
				
			} finally {
				model.endUpdate();
			}
			
			return cells;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DConnectPreview extends DelegatingConnectPreview {

		@Override
		protected Object createCell(mxCellState startState, String style) {
			mxICell cell = (mxICell) super.createCell(startState, style);
			
			cell.setValue(LanguageUtils.DATA_UNDEFINED_LABEL);
			
			return cell;
		}

		@Override
		public Object stop(boolean commit, MouseEvent e) {
			if(!isEditable()) {
				return null;
			}

			Object result = (sourceState != null) ? sourceState.getCell() : null;
			
			if(previewState!=null) {
				mxIGraphModel model = graph.getModel();
				
				// TODO access model only when committing?
				model.beginUpdate();
				try {
					result = super.stop(commit, e);
					
					if(commit && result!=null) {
						Object source = model.getTerminal(result, true);
						Object target = model.getTerminal(result, false);
						
						replaceHead(target, source, true);
					}
				} finally {
					model.endUpdate();
				}
			}
			
			return result;
		}		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public class DCallbackHandler extends CallbackHandler {
		
		protected DCallbackHandler() {
			// no-op
		}

		/**
		 * @see GraphPresenter#deleteCells(Object[])
		 */
		public void flipEdge(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			if(graph.getSelectionCount()!=1) {
				return;
			}

			try {
				DependencyGraphPresenter.this.flipEdge(graph.getSelectionCell());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to flip edge", ex); //$NON-NLS-1$
			}
		}
	}
}