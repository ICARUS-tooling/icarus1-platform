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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.ikarus_systems.icarus.language.SentenceDataEvent;
import net.ikarus_systems.icarus.language.SentenceDataListener;
import net.ikarus_systems.icarus.language.dependency.DependencyConstants;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.DependencyDataEvent;
import net.ikarus_systems.icarus.language.dependency.DependencyNodeData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.dependency.MutableDependencyData;
import net.ikarus_systems.icarus.language.dependency.SimpleDependencyData;
import net.ikarus_systems.icarus.logging.LoggerFactory;
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
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyGraphPresenter extends GraphPresenter {
	
	private static final long serialVersionUID = -8262542126697438425L;
	
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
		DelegatingGraph graph = new DelegatingGraph(new mxGraphModel());
		
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
	protected mxConnectPreview createConnectPreview() {
		return new DConnectPreview();
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
				if(DependencyUtils.isUndefined(head) || DependencyUtils.isRoot(head)) {
					continue;
				}
				
				Object source = vertices[head];
				Object target = vertices[i];
				
				Object edge = createEdge(data.getRelation(i), "head"+i,  //$NON-NLS-1$
						model.getValue(source), model.getValue(target));
				
				graph.addEdge(edge, parent, source, target, null);
			}
			
			// Apply styles
			refreshStyles();
			
			// Apply layout
			refreshLayout();
		} finally {
			model.endUpdate();
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
			DependencyNodeData[] children = data.getChildren();
			if(children!=null) {
				for(DependencyNodeData child : children) {
					nodes.add(child);
				}
			}
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
				for(Object edge : graph.getIncomingEdges(cell)) {
					if(!GraphUtils.isOrderEdge(graph, edge)) {
						model.remove(edge);
					}
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
				int edgeCount = model.getEdgeCount(cell);
				for(int i=0; i<edgeCount; i++) {
					Object edge = model.getEdgeAt(cell, i);
					
					// Ignore outgoing and order edges
					if(GraphUtils.isOrderEdge(graph, edge) 
							|| cell==model.getTerminal(edge, true)) {
						continue;
					}
					
					if(newHead==model.getTerminal(edge, true)) {
						model.remove(edge);
					}
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
							if(DependencyUtils.isUndefined(newValue.getHead())
									|| DependencyUtils.isRoot(newValue.getHead())) {
								// Head got removed
								model.remove(edge);
							} else if(DependencyUtils.isUndefined(oldvalue.getHead())
									|| DependencyUtils.isRoot(oldvalue.getHead())) {
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
				rebuildGraph();
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
					if(GraphUtils.isOrderEdge(graph, edge)) {
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

		// Sync to data
		pauseChangeHandling();
		try {
			syncToData();
		} finally {
			resumeChangeHandling();
		}
	}

	@Override
	public void cloneCells(Object[] cells) {
		if(!canEdit()) {
			return;
		}
		
		// TODO Auto-generated method stub
		super.cloneCells(cells);
	}

	@Override
	public void importCells(CellBuffer buffer) {
		if(!canEdit()) {
			return;
		}
		
		// TODO Auto-generated method stub
		super.importCells(buffer);
	}

	@Override
	public void addNode() {
		if(!canEdit()) {
			return;
		}
		
		// Adding the dummy item causes an entire graph rebuild
		data.addDummyItem();
	}

	@Override
	public void addEdge(Object source, Object target, boolean orderEdge) {
		if(!canEdit()) {
			return;
		}
		
		// Add edge
		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			for(Object edge : graph.getEdgesBetween(source, target)) {
				// Allow only one edge of any kind between nodes
				if(GraphUtils.isOrderEdge(graph, edge) == orderEdge) {
					return;
				}
			}
			
			Object value = orderEdge ? Order.BEFORE : DependencyConstants.DATA_UNDEFINED_LABEL;
			
			// Refresh target
			DependencyNodeData targetData = removeHead(target, true);
			String id = (orderEdge ? "order" : "head")+targetData.getIndex(); //$NON-NLS-1$ //$NON-NLS-2$
			
			Object edge = createEdge(value, id, model.getValue(source), model.getValue(target));
			graph.addEdge(edge, graph.getDefaultParent(), source, target, null);
		} finally {
			model.endUpdate();
		}

		// Sync to data
		pauseChangeHandling();
		try {
			syncToData();
		} finally {
			resumeChangeHandling();
		}
	}

	// TODO honestly: do we need this?
	public void flipEdges(Object[] edges) {
		if(!canEdit()) {
			return;
		}
		
		if(edges==null) {
			edges = getSelectionEdges();
		}
		if(edges.length==0) {
			return;
		}
		
		// Flip edges
		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			// TODO
		} finally {
			model.endUpdate();
		}

		// Sync to data
		pauseChangeHandling();
		try {
			syncToData();
		} finally {
			resumeChangeHandling();
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
	protected class DConnectPreview extends DelegatingConnectPreview {

		@Override
		protected Object createCell(mxCellState startState, String style) {
			mxICell cell = (mxICell) super.createCell(startState, style);
			
			cell.setValue(DependencyUtils.DATA_UNDEFINED_LABEL);
			
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
		public void flipEdges(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			if(graph.getSelectionCount()==0) {
				return;
			}
			
			Object[] edges = getSelectionEdges();

			try {
				DependencyGraphPresenter.this.flipEdges(edges);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to flip edges", ex); //$NON-NLS-1$
			}
		}
	}
}