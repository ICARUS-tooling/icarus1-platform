/*
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
	protected EdgeSet edgeSet;
	protected EdgeSet goldSet;
	
	protected boolean showGoldEdges = false;
	protected boolean showGoldNodes = false;
	protected boolean markFalseEdges = true;
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
		edgeSet = (EdgeSet) options.get("edges"); //$NON-NLS-1$
		goldSet = (EdgeSet) options.get("goldEdges"); //$NON-NLS-1$
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

	@Override
	protected EdgeHighlightHandler createEdgeHighlightHandler() {
		// Edge highlighting not supported
		return null;
	}

	@Override
	protected CallbackHandler createCallbackHandler() {
		return new CorefCallbackHandler();
	}

	@Override
	protected ActionManager createActionManager() {
		ActionManager actionManager = super.createActionManager();

		// Load default actions
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
				"plugins.coref.corefGraphPresenter.toggleShowGoldEdgesAction",  //$NON-NLS-1$
				callbackHandler, "showGoldEdges"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.corefGraphPresenter.toggleShowGoldNodesAction",  //$NON-NLS-1$
				callbackHandler, "showGoldNodes"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.corefGraphPresenter.toggleFilterSingletonsAction",  //$NON-NLS-1$
				callbackHandler, "filterSingletons"); //$NON-NLS-1$
	}
	
	protected Object createVertex(Span span) {
		CoreferenceData sentence = span.isROOT() ?
				CoreferenceUtils.emptySentence
				: document.get(span.getSentenceIndex());
		
		mxCell cell = new mxCell(new CorefNodeData(span, sentence));
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
			
			// If no EdgeSet is set then fetch the one directly 
			// allocated to the document
			if(edgeSet==null) {
				edgeSet = document.getEdgeSet();
			}
			
			// Still no EdgeSet available -> take the default allocation
			if(edgeSet==null) {
				edgeSet = document.getDefaultEdgeSet();
			}
			
			// Abort if there are no edges to visualize
			if(edgeSet==null || edgeSet.size()==0) {
				return;
			}
			
			// Use the default allocation if there is no gold
			// EdgeSet defined
			if(goldSet==null) {
				goldSet = document.getDefaultEdgeSet();
			}
			
			// Clear gold set if it is the same as the one displayed
			if(edgeSet==goldSet) {
				goldSet = null;
			}
			
			Collection<Edge> required = null;
			if(goldSet!=null) {
				required = new HashSet<>(goldSet.getEdges());
			}
			
			Map<Span, Object> cellMap = new HashMap<>();
			Object parent = graph.getDefaultParent();
			
			Collection<Edge> edges = edgeSet.getEdges();
			if(isFilterSingletons()) {
				edges = CoreferenceUtils.removeSingletons(edges);
			}
			
			for(Edge edge : edges) {
				Span spanS = edge.getSource();
				Span spanT = edge.getTarget();
				
				Object cellS = cellMap.get(spanS);
				if(cellS==null) {
					cellS = createVertex(spanS);
					cellMap.put(spanS, cellS);
					
					model.add(parent, cellS, model.getChildCount(parent));
					
					graph.cellSizeUpdated(cellS, false);
				}
				
				Object cellT = cellMap.get(spanT);
				if(cellT==null) {
					cellT = createVertex(spanT);
					cellMap.put(spanT, cellT);
					
					model.add(parent, cellT, model.getChildCount(parent));
					
					graph.cellSizeUpdated(cellT, false);
				}
				
				boolean falseEdge = required!=null && !required.remove(edge);
				
				Object cellE = createEdge(edge, cellS, cellT, falseEdge ?
						CorefEdgeData.FALSE_PREDICTED_EDGE : 0);
				
				model.add(parent, cellE, model.getChildCount(parent));
			}
			
			// Insert all missing gold edges
			if((isShowGoldEdges() || isShowGoldNodes()) 
					&& required!=null && !required.isEmpty()) {
				if(isFilterSingletons()) {
					required = CoreferenceUtils.removeSingletons(required);
				}
				
				for(Edge edge : required) {
					Span spanS = edge.getSource();
					Span spanT = edge.getTarget();
					
					Object cellS = cellMap.get(spanS);
					Object cellT = cellMap.get(spanT);
					
					if(!isShowGoldNodes() && (cellS==null || cellT==null)) {
						continue;
					}
					
					if(cellS==null) {
						cellS = createVertex(spanS);
						cellMap.put(spanS, cellS);
						
						model.add(parent, cellS, model.getChildCount(parent));
						
						graph.cellSizeUpdated(cellS, false);
					}
					
					if(cellT==null) {
						cellT = createVertex(spanT);
						cellMap.put(spanT, cellT);
						
						model.add(parent, cellT, model.getChildCount(parent));
						
						graph.cellSizeUpdated(cellT, false);
					}
					Object cellE = createEdge(edge, cellS, cellT, CorefEdgeData.MISSING_GOLD_EDGE);
					
					//if(isShowGoldEdges() || spanS.isROOT()) {
						model.add(parent, cellE, model.getChildCount(parent));
					//}
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
