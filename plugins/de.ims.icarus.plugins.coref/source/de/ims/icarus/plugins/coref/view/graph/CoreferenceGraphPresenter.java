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
package de.ims.icarus.plugins.coref.view.graph;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JComboBox;

import org.java.plugin.registry.Extension;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.config.ConfigDelegate;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.jgraph.layout.GraphLayout;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.jgraph.layout.GraphStyle;
import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceGraphPresenter extends GraphPresenter {

	private static final long serialVersionUID = -6564065119073757454L;
	
	protected CoreferenceDocumentData document;
	protected CoreferenceAllocation allocation;
	protected CoreferenceAllocation goldAllocation;
	
	protected boolean showGoldEdges = false;
	protected boolean showGoldNodes = true;
	protected boolean markFalseEdges = true;
	protected boolean markFalseNodes = true;
	protected boolean filterSingletons = true;

	public CoreferenceGraphPresenter() {
		// no-op
	}

	@Override
	protected GraphLayout createDefaultGraphLayout() {
		return new CoreferenceGraphLayout();
	}

	@Override
	protected GraphStyle createDefaultGraphStyle() {
		return new CoreferenceGraphStyle();
	}

	@Override
	protected GraphRenderer createDefaultGraphRenderer() {
		return super.createDefaultGraphRenderer();
	}

	@Override
	protected JComboBox<Extension> feedSelector(Options options, String command) {
		return null;
	}

	@Override
	protected mxGraph createGraph() {
		return new CorefGraph();
	}
	
	@Override
	protected void loadPreferences() {
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		setAutoZoomEnabled(config.getBoolean(
				"plugins.jgraph.appearance.coref.autoZoom")); //$NON-NLS-1$
		setCompressEnabled(config.getBoolean(
				"plugins.jgraph.appearance.coref.compressGraph")); //$NON-NLS-1$
		setMarkFalseEdges(config.getBoolean(
				"plugins.jgraph.appearance.coref.markFalseEdges")); //$NON-NLS-1$
		setShowGoldEdges(config.getBoolean(
				"plugins.jgraph.appearance.coref.showGoldEdges")); //$NON-NLS-1$
		setFilterSingletons(config.getBoolean(
				"plugins.jgraph.appearance.coref.filterSingletons")); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return document!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public CoreferenceDocumentData getPresentedData() {
		return document;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#setData(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	protected void setData(Object data, Options options) {
		document = (CoreferenceDocumentData) data;
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		allocation = (CoreferenceAllocation) options.get("allocation"); //$NON-NLS-1$
		goldAllocation = (CoreferenceAllocation) options.get("goldAllocation"); //$NON-NLS-1$
	}
	
	@Override
	protected ConfigDelegate createConfigDelegate() {
		return new GraphConfigDelegate("plugins.jgraph.appearance.coref", null); //$NON-NLS-1$
	}

	@Override
	protected void initGraphComponentInternals() {
		super.initGraphComponentInternals();
		
		editableMainToolBarListId = "plugins.coref.corefGraphPresenter.editableMainToolBarList"; //$NON-NLS-1$
		editablePopupMenuListId = "plugins.coref.corefGraphPresenter.editablePopupMenuList"; //$NON-NLS-1$
	}

	/*@Override
	protected EdgeHighlightHandler createEdgeHighlightHandler() {
		// Edge highlighting not supported
		return null;
	}*/

	@Override
	protected CallbackHandler createCallbackHandler() {
		return new CorefCallbackHandler();
	}

	@Override
	protected ActionManager createActionManager() {
		ActionManager actionManager = super.createActionManager();

		// Load coreference graph actions
		URL actionLocation = CoreferenceGraphPresenter.class.getResource(
				"coref-graph-presenter-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: coref-graph-presenter-actions.xml"); //$NON-NLS-1$
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
		actionManager.setSelected(isMarkFalseEdges(), 
				"plugins.coref.corefGraphPresenter.toggleMarkFalseEdgesAction"); //$NON-NLS-1$
		actionManager.setSelected(isMarkFalseNodes(), 
				"plugins.coref.corefGraphPresenter.toggleMarkFalseNodesAction"); //$NON-NLS-1$
		actionManager.setSelected(isShowGoldEdges(), 
				"plugins.coref.corefGraphPresenter.toggleShowGoldEdgesAction"); //$NON-NLS-1$
		actionManager.setSelected(isShowGoldNodes(), 
				"plugins.coref.corefGraphPresenter.toggleShowGoldNodesAction"); //$NON-NLS-1$
		actionManager.setSelected(isFilterSingletons(), 
				"plugins.coref.corefGraphPresenter.toggleFilterSingletonsAction"); //$NON-NLS-1$
		
		// Register callback functions
		actionManager.addHandler(
				"plugins.coref.corefGraphPresenter.toggleMarkFalseEdgesAction",  //$NON-NLS-1$
				callbackHandler, "markFalseEdges"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.corefGraphPresenter.toggleMarkFalseNodesAction",  //$NON-NLS-1$
				callbackHandler, "markFalseNodes"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.corefGraphPresenter.toggleShowGoldEdgesAction",  //$NON-NLS-1$
				callbackHandler, "showGoldEdges"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.corefGraphPresenter.toggleShowGoldNodesAction",  //$NON-NLS-1$
				callbackHandler, "showGoldNodes"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.corefGraphPresenter.toggleFilterSingletonsAction",  //$NON-NLS-1$
				callbackHandler, "filterSingletons"); //$NON-NLS-1$
	}
	
	protected Object createVertex(Span span, int nodeType) {
		CoreferenceData sentence = span.isROOT() ?
				CoreferenceUtils.emptySentence
				: document.get(span.getSentenceIndex());
		
		mxCell cell = new mxCell(new CorefNodeData(span, sentence, nodeType));
		cell.setVertex(true);
		cell.setGeometry(new mxGeometry());
		
		return cell;
	}
	
	protected Object createEdge(Edge edge, Object source, Object target, int edgeType) {
		mxCell cell = new mxCell(new CorefEdgeData(edge, edgeType));
		cell.setEdge(true);
		
		graph.getModel().setTerminal(cell, source, true);
		graph.getModel().setTerminal(cell, target, false);
		
		mxGeometry geometry = new mxGeometry();
		geometry.setRelative(true);
		
		cell.setGeometry(geometry);
		
		return cell;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#syncToGraph()
	 */
	@Override
	protected void syncToGraph() {
		
		mxIGraphModel model = graph.getModel();
		pauseGraphChangeHandling();
		model.beginUpdate();
		try {
			// Clear graph
			GraphUtils.clearGraph(graph);
			
			if(document==null) {
				return;
			}
			
			EdgeSet edgeSet = CoreferenceUtils.getEdgeSet(document, allocation);
			EdgeSet goldSet = CoreferenceUtils.getGoldEdgeSet(document, goldAllocation);
			
			// Clear gold set if it is the same as the one displayed
			if(edgeSet==goldSet) {
				goldSet = null;
			}
			
			Collection<Edge> edgeLookup = null;
			if(goldSet!=null) {
				edgeLookup = new HashSet<>(goldSet.getEdges());
			}
			
			Collection<Span> spanLookup = null;
			if(goldSet!=null) {
				spanLookup = CoreferenceUtils.collectSpans(goldSet);
			}
			
			Map<Span, Object> cellMap = new HashMap<>();
			Object parent = graph.getDefaultParent();
			
			Collection<Edge> edges = edgeSet.getEdges();
			if(isFilterSingletons()) {
				edges = CoreferenceUtils.removeSingletons(edges);
			}
			
			//System.out.println(Arrays.toString(edges.toArray()));
			
			for(Edge edge : edges) {
				Span spanS = edge.getSource();
				Span spanT = edge.getTarget();
				
				Object cellS = cellMap.get(spanS);
				if(cellS==null) {
					boolean falseNode = spanLookup!=null && !spanLookup.remove(spanS);
					cellS = createVertex(spanS, falseNode ? CorefCellData.FALSE_PREDICTED : 0);
					cellMap.put(spanS, cellS);
					
					model.add(parent, cellS, model.getChildCount(parent));
					//System.out.println("added source cell for "+spanS);
					
					graph.cellSizeUpdated(cellS, false);
				}
				
				Object cellT = cellMap.get(spanT);
				if(cellT==null) {
					boolean falseNode = spanLookup!=null && !spanLookup.remove(spanT);
					cellT = createVertex(spanT, falseNode ? CorefCellData.FALSE_PREDICTED : 0);
					cellMap.put(spanT, cellT);
					
					model.add(parent, cellT, model.getChildCount(parent));
					//System.out.println("added target cell for "+spanT+" false="+falseNode);
					
					graph.cellSizeUpdated(cellT, false);
				}
				
				boolean falseEdge = edgeLookup!=null && !edgeLookup.remove(edge);
				
				Object cellE = createEdge(edge, cellS, cellT, falseEdge ?
						CorefCellData.FALSE_PREDICTED : 0);
				
				model.add(parent, cellE, model.getChildCount(parent));
				//	System.out.println("added edge: "+edge);
			}
			
			// Insert all missing gold nodes and edges
			if((isShowGoldEdges() || isShowGoldNodes()) 
					&& edgeLookup!=null && !edgeLookup.isEmpty()) {
				if(isFilterSingletons()) {
					edgeLookup = CoreferenceUtils.removeSingletons(edgeLookup);
				}
				
				for(Edge edge : edgeLookup) {
					//System.out.println("adding gold edge: "+edge);
					
					Span spanS = edge.getSource();
					Span spanT = edge.getTarget();
					
					Object cellS = cellMap.get(spanS);
					Object cellT = cellMap.get(spanT);
					
					if(!isShowGoldNodes() && (cellS==null || cellT==null)) {
						continue;
					}
					if(!isShowGoldEdges() && !spanS.isROOT()) {
						continue;
					}
					
					boolean isNew = cellS==null || cellT==null;
					
					if(cellS==null) {
						cellS = createVertex(spanS, CorefCellData.MISSING_GOLD);
						cellMap.put(spanS, cellS);
						
						model.add(parent, cellS, model.getChildCount(parent));
						
						graph.cellSizeUpdated(cellS, false);
					}
					
					if(cellT==null) {
						cellT = createVertex(spanT, CorefCellData.MISSING_GOLD);
						cellMap.put(spanT, cellT);
						
						model.add(parent, cellT, model.getChildCount(parent));
						
						graph.cellSizeUpdated(cellT, false);
					}
					
					if(isShowGoldEdges() || (spanS.isROOT() && isNew)) {
						Object cellE = createEdge(edge, cellS, cellT, CorefCellData.MISSING_GOLD);
						
						model.add(parent, cellE, model.getChildCount(parent));
					}
				}
			}
			
		} finally {
			model.endUpdate();
		}
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#syncToData()
	 */
	@Override
	protected void syncToData() {
		// TODO enable modifications as soon as the inner data storage is a clone of supplied data!
	}
	
	public boolean isShowGoldEdges() {
		return showGoldEdges;
	}
	
	public boolean isShowGoldNodes() {
		return showGoldNodes;
	}

	public boolean isMarkFalseEdges() {
		return markFalseEdges;
	}

	public boolean isMarkFalseNodes() {
		return markFalseNodes;
	}

	public boolean isFilterSingletons() {
		return filterSingletons;
	}

	public void setShowGoldEdges(boolean showGoldEdges) {
		if(showGoldEdges==this.showGoldEdges) {
			return;
		}
		
		boolean oldValue = this.showGoldEdges;
		this.showGoldEdges = showGoldEdges;

		rebuildGraph();
		
		getActionManager().setSelected(showGoldEdges, 
				"plugins.coref.corefGraphPresenter.toggleShowGoldEdgesAction"); //$NON-NLS-1$
		
		firePropertyChange("showGoldEdges", oldValue, showGoldEdges); //$NON-NLS-1$
	}

	public void setShowGoldNodes(boolean showGoldNodes) {
		if(showGoldNodes==this.showGoldNodes) {
			return;
		}
		
		boolean oldValue = this.showGoldNodes;
		this.showGoldNodes = showGoldNodes;

		rebuildGraph();
		
		getActionManager().setSelected(showGoldNodes, 
				"plugins.coref.corefGraphPresenter.toggleShowGoldNodesAction"); //$NON-NLS-1$
		
		firePropertyChange("showGoldNodes", oldValue, showGoldNodes); //$NON-NLS-1$
	}

	public void setMarkFalseEdges(boolean markFalseEdges) {
		if(markFalseEdges==this.markFalseEdges) {
			return;
		}
		
		boolean oldValue = this.markFalseEdges;
		this.markFalseEdges = markFalseEdges;

		rebuildGraph();
		
		getActionManager().setSelected(markFalseEdges, 
				"plugins.coref.corefGraphPresenter.toggleMarkFalseEdgesAction"); //$NON-NLS-1$
		
		firePropertyChange("markFalseEdges", oldValue, markFalseEdges); //$NON-NLS-1$
	}

	public void setMarkFalseNodes(boolean markFalseNodes) {
		if(markFalseNodes==this.markFalseNodes) {
			return;
		}
		
		boolean oldValue = this.markFalseNodes;
		this.markFalseNodes = markFalseNodes;

		rebuildGraph();
		
		getActionManager().setSelected(markFalseNodes, 
				"plugins.coref.corefGraphPresenter.toggleMarkFalseNodesAction"); //$NON-NLS-1$
		
		firePropertyChange("markFalseNodes", oldValue, markFalseNodes); //$NON-NLS-1$
	}

	public void setFilterSingletons(boolean filterSingletons) {
		if(filterSingletons==this.filterSingletons) {
			return;
		}
		
		boolean oldValue = this.filterSingletons;
		this.filterSingletons = filterSingletons;

		rebuildGraph();
		
		getActionManager().setSelected(filterSingletons, 
				"plugins.coref.corefGraphPresenter.toggleFilterSingletonsAction"); //$NON-NLS-1$
		
		firePropertyChange("filterSingletons", oldValue, filterSingletons); //$NON-NLS-1$
	}

	protected class CorefGraph extends DelegatingGraph {

		@Override
		protected void init() {
			super.init();
			
			setCellsEditable(false);
			setGridEnabled(false);
			setMultigraph(true);
		}

		@Override
		public mxRectangle getPreferredSizeForCell(Object cell) {
			mxRectangle bounds = super.getPreferredSizeForCell(cell);
			
			bounds.setWidth(bounds.getWidth()+12);
			bounds.setHeight(bounds.getHeight()+2);
			
			return bounds;
		}
	}
	
	public class CorefCallbackHandler extends CallbackHandler {
		
		protected CorefCallbackHandler() {
			// no-op
		}

		public void markFalseEdges(boolean b) {
			setMarkFalseEdges(b);
		}

		public void markFalseEdges(ActionEvent e) {
			// ignore
		}

		public void markFalseNodes(boolean b) {
			setMarkFalseNodes(b);
		}

		public void markFalseNodes(ActionEvent e) {
			// ignore
		}

		public void showGoldEdges(boolean b) {
			setShowGoldEdges(b);
		}

		public void showGoldEdges(ActionEvent e) {
			// ignore
		}

		public void showGoldNodes(boolean b) {
			setShowGoldNodes(b);
		}

		public void showGoldNodes(ActionEvent e) {
			// ignore
		}

		public void filterSingletons(boolean b) {
			setFilterSingletons(b);
		}

		public void filterSingletons(ActionEvent e) {
			// ignore
		}
	}
}
