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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.java.plugin.registry.Extension;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.config.ConfigDelegate;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.CorefComparison;
import de.ims.icarus.language.coref.CorefErrorType;
import de.ims.icarus.language.coref.CorefMember;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanCache;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentHighlighting;
import de.ims.icarus.language.coref.helper.SpanFilters;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentDataPresenter;
import de.ims.icarus.plugins.jgraph.layout.GraphLayout;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.jgraph.layout.GraphStyle;
import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.AnnotationControl;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceGraphPresenter extends GraphPresenter implements Installable {

	private static final long serialVersionUID = -6564065119073757454L;

	protected CoreferenceDocumentDataPresenter parent;
	
	protected CoreferenceDocumentData document;
	protected CoreferenceAllocation allocation;
	protected CoreferenceAllocation goldAllocation;
	
	protected boolean includeGoldEdges = false;
	protected boolean includeGoldNodes = true;
	protected boolean markFalseEdges = true;
	protected boolean markFalseNodes = true;
	protected boolean filterSingletons = true;
	protected boolean showSpanBounds = true;

	protected CoreferenceDocumentDataPresenter.PresenterMenu presenterMenu;
	
	protected SpanCache cache = new SpanCache();

	public CoreferenceGraphPresenter() {
		// no-op
	}

	@Override
	protected JPopupMenu createPopupMenu() {
		
		String actionListId = isEditable() ? editablePopupMenuListId 
				: uneditablePopupMenuListId;
		
		Options options = new Options();
		options.put("showInMenu", presenterMenu); //$NON-NLS-1$
		
		return getActionManager().createPopupMenu(actionListId, options);
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
	protected AnnotationControl createAnnotationControl() {
		AnnotationControl annotationControl = super.createAnnotationControl();
		annotationControl.setAnnotationManager(new CoreferenceDocumentAnnotationManager());
		
		return annotationControl;
	}

	@Override
	public CoreferenceDocumentAnnotationManager getAnnotationManager() {
		return (CoreferenceDocumentAnnotationManager) super.getAnnotationManager();
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
		setIncludeGoldEdges(config.getBoolean(
				"plugins.jgraph.appearance.coref.includeGoldEdges")); //$NON-NLS-1$
		setMarkFalseNodes(config.getBoolean(
				"plugins.jgraph.appearance.coref.markFalseNodes")); //$NON-NLS-1$
		setIncludeGoldNodes(config.getBoolean(
				"plugins.jgraph.appearance.coref.includeGoldNodes")); //$NON-NLS-1$
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
		
		editableMainToolBarListId = "plugins.coref.coreferenceGraphPresenter.editableMainToolBarList"; //$NON-NLS-1$
		editablePopupMenuListId = "plugins.coref.coreferenceGraphPresenter.editablePopupMenuList"; //$NON-NLS-1$

		presenterMenu = new CoreferenceDocumentDataPresenter.PresenterMenu(this, getHandler());
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#createUpperToolBar()
	 */
	@Override
	protected ActionComponentBuilder createUpperToolBar() {
		ActionComponentBuilder builder = super.createUpperToolBar();
		
		builder.addOption("errorInfoLabel", CoreferenceUtils.createErrorInfoLabel()); //$NON-NLS-1$
		
		return builder;
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		parent = null;
		if(target instanceof CoreferenceDocumentDataPresenter) {
			parent = (CoreferenceDocumentDataPresenter) target;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		parent = null;
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
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseEdgesAction"); //$NON-NLS-1$
		actionManager.setSelected(isMarkFalseNodes(), 
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseNodesAction"); //$NON-NLS-1$
		actionManager.setSelected(isIncludeGoldEdges(), 
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldEdgesAction"); //$NON-NLS-1$
		actionManager.setSelected(isIncludeGoldNodes(), 
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldNodesAction"); //$NON-NLS-1$
		actionManager.setSelected(isFilterSingletons(), 
				"plugins.coref.coreferenceGraphPresenter.toggleFilterSingletonsAction"); //$NON-NLS-1$
		actionManager.setSelected(isShowSpanBounds(), 
				"plugins.coref.coreferenceGraphPresenter.toggleShowSpanBoundsAction"); //$NON-NLS-1$
		
		// Register callback functions
		actionManager.addHandler(
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseEdgesAction",  //$NON-NLS-1$
				callbackHandler, "markFalseEdges"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseNodesAction",  //$NON-NLS-1$
				callbackHandler, "markFalseNodes"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldEdgesAction",  //$NON-NLS-1$
				callbackHandler, "includeGoldEdges"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldNodesAction",  //$NON-NLS-1$
				callbackHandler, "includeGoldNodes"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.coreferenceGraphPresenter.toggleFilterSingletonsAction",  //$NON-NLS-1$
				callbackHandler, "filterSingletons"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.coref.coreferenceGraphPresenter.toggleShowSpanBoundsAction",  //$NON-NLS-1$
				callbackHandler, "showSpanBounds"); //$NON-NLS-1$
	}
	
	@Override
	protected void refreshActions() {
		super.refreshActions();
		
		ActionManager actionManager = getActionManager();

		boolean hasGold = goldAllocation!=null && goldAllocation!=allocation;
		actionManager.setEnabled(hasGold, 
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseEdgesAction",  //$NON-NLS-1$
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseNodesAction",  //$NON-NLS-1$
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldEdgesAction",  //$NON-NLS-1$
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldNodesAction"); //$NON-NLS-1$
	}

	@Override
	protected Handler createHandler() {
		return new CorefHandler();
	}

	protected Object createVertex(Span span, CorefErrorType nodeType, boolean gold) {
		CoreferenceData sentence = span.isROOT() ?
				CoreferenceUtils.emptySentence
				: document.get(span.getSentenceIndex());
		
		long highlight = 0L;
		CoreferenceDocumentAnnotationManager annotationManager = getAnnotationManager();
		if(annotationManager!=null && annotationManager.hasAnnotation() && !span.isROOT()) {
			int index = cache.getIndex(span);
			long hl = getAnnotationManager().getHighlight(index);
			if(CoreferenceDocumentHighlighting.getInstance().isNodeHighlighted(hl)) {
				long mask = ~(CoreferenceDocumentHighlighting.getInstance().getEdgeGroupingMask()
						| CoreferenceDocumentHighlighting.getInstance().getEdgeHighlightMask());
				highlight |= (hl & mask);
			}
		}
		
		mxCell cell = new mxCell(new CorefNodeData(span, sentence, nodeType, gold, highlight));
		cell.setVertex(true);
		cell.setGeometry(new mxGeometry());
		
		return cell;
	}
	
	protected Object createEdge(Edge edge, Object source, Object target, boolean gold) {

		long highlight = 0L;
		CoreferenceDocumentAnnotationManager annotationManager = getAnnotationManager();
		if(annotationManager!=null && annotationManager.hasAnnotation()) {
			int index = cache.getIndex(edge.getTarget());
			long hl = getAnnotationManager().getHighlight(index);
			if(CoreferenceDocumentHighlighting.getInstance().isEdgeHighlighted(hl)) {
				long mask = ~(CoreferenceDocumentHighlighting.getInstance().getNodeGroupingMask()
						| CoreferenceDocumentHighlighting.getInstance().getNodeHighlightMask());
				highlight |= (hl & mask);
			}
		}
		
		mxCell cell = new mxCell(new CorefEdgeData(edge, gold, highlight));
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
			
			Map<Span, Object> cellMap = new HashMap<>();
			Object parent = graph.getDefaultParent();
			
			CorefComparison comparison = CoreferenceUtils.compare(edgeSet, goldSet, isFilterSingletons());

			cache.clear();
			cache.cacheEdges(comparison.getEdgeSet().getEdges());
			
			//System.out.println(Arrays.toString(edges.toArray()));
			
			for(Edge edge : comparison.getEdges()) {
				//System.out.println("adding edge: "+edge);
				Span spanS = edge.getSource();
				Span spanT = edge.getTarget();
				
				Object cellS = cellMap.get(spanS);
				if(cellS==null) {
					cellS = createVertex(spanS, comparison.getErrorType(spanS), false);
					cellMap.put(spanS, cellS);
					
					model.add(parent, cellS, model.getChildCount(parent));
					//System.out.println("added source cell for "+spanS);
					
					graph.cellSizeUpdated(cellS, false);
				}
				
				Object cellT = cellMap.get(spanT);
				if(cellT==null) {
					cellT = createVertex(spanT, comparison.getErrorType(spanT), false);
					cellMap.put(spanT, cellT);
					
					model.add(parent, cellT, model.getChildCount(parent));
					//System.out.println("added target cell for "+spanT+" false="+falseNode);
					
					graph.cellSizeUpdated(cellT, false);
				}

				Object cellE = createEdge(edge, cellS, cellT, false);
				
				model.add(parent, cellE, model.getChildCount(parent));
				//	System.out.println("added edge: "+edge);
			}
			
			// Insert all missing gold nodes and edges
			if((isIncludeGoldEdges() || isIncludeGoldNodes()) 
					&& comparison.getGoldEdges()!=null && !comparison.getGoldEdges().isEmpty()) {
				
				for(Edge edge : comparison.getGoldEdges()) {
					//System.out.println("adding gold edge: "+edge);
					
					Span spanS = edge.getSource();
					Span spanT = edge.getTarget();
					
//					if(!comparison.isGold(spanS)
//							&& !comparison.isGold(spanT)) {
//						continue;
//					}
					
					Object cellS = cellMap.get(spanS);
					Object cellT = cellMap.get(spanT);
					
					boolean isNew = cellS==null || cellT==null;
					
					// Skip edges to or from gold nodes when those nodes should 
					// not be displayed
					if(!isIncludeGoldNodes() && isNew) {
						continue;
					}
					
					// Skip edges
					if(!isIncludeGoldEdges() && !isNew) {
						continue;
					}
					
					if(cellS==null) {
						cellS = createVertex(spanS, comparison.getErrorType(spanS), true);
						cellMap.put(spanS, cellS);
						
						model.add(parent, cellS, model.getChildCount(parent));
						
						graph.cellSizeUpdated(cellS, false);
					}
					
					if(cellT==null) {
						cellT = createVertex(spanT, comparison.getErrorType(spanT), true);
						cellMap.put(spanT, cellT);
						
						model.add(parent, cellT, model.getChildCount(parent));
						
						graph.cellSizeUpdated(cellT, false);
					}
					
					Object cellE = createEdge(edge, cellS, cellT, true);
					
					model.add(parent, cellE, model.getChildCount(parent));
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
	
	public boolean isIncludeGoldEdges() {
		return includeGoldEdges;
	}
	
	public boolean isIncludeGoldNodes() {
		return includeGoldNodes;
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

	public void setIncludeGoldEdges(boolean includeGoldEdges) {
		if(includeGoldEdges==this.includeGoldEdges) {
			return;
		}
		
		boolean oldValue = this.includeGoldEdges;
		this.includeGoldEdges = includeGoldEdges;

		rebuildGraph();
		
		getActionManager().setSelected(includeGoldEdges, 
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldEdgesAction"); //$NON-NLS-1$
		
		firePropertyChange("includeGoldEdges", oldValue, includeGoldEdges); //$NON-NLS-1$
	}

	public void setIncludeGoldNodes(boolean includeGoldNodes) {
		if(includeGoldNodes==this.includeGoldNodes) {
			return;
		}
		
		boolean oldValue = this.includeGoldNodes;
		this.includeGoldNodes = includeGoldNodes;

		rebuildGraph();
		
		getActionManager().setSelected(includeGoldNodes, 
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldNodesAction"); //$NON-NLS-1$
		
		firePropertyChange("includeGoldNodes", oldValue, includeGoldNodes); //$NON-NLS-1$
	}

	public void setMarkFalseEdges(boolean markFalseEdges) {
		if(markFalseEdges==this.markFalseEdges) {
			return;
		}
		
		boolean oldValue = this.markFalseEdges;
		this.markFalseEdges = markFalseEdges;

		rebuildGraph();
		
		getActionManager().setSelected(markFalseEdges, 
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseEdgesAction"); //$NON-NLS-1$
		
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
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseNodesAction"); //$NON-NLS-1$
		
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
				"plugins.coref.coreferenceGraphPresenter.toggleFilterSingletonsAction"); //$NON-NLS-1$
		
		firePropertyChange("filterSingletons", oldValue, filterSingletons); //$NON-NLS-1$
	}
	
	/**
	 * @return the showSpanBounds
	 */
	public boolean isShowSpanBounds() {
		return showSpanBounds;
	}

	/**
	 * @param showSpanBounds the showSpanBounds to set
	 */
	public void setShowSpanBounds(boolean showSpanBounds) {
		if(showSpanBounds==this.showSpanBounds) {
			return;
		}
		
		boolean oldValue = this.showSpanBounds;
		this.showSpanBounds = showSpanBounds;

		rebuildGraph();
		
		getActionManager().setSelected(showSpanBounds, 
				"plugins.coref.coreferenceGraphPresenter.toggleShowSpanBoundsAction"); //$NON-NLS-1$
		
		firePropertyChange("showSpanBounds", oldValue, showSpanBounds); //$NON-NLS-1$
	}

	protected void outlineProperties(Object value) {
		if(parent==null) {
			return;
		}
		
		try {
			Collection<CorefMember> members = new LinkedList<>();
			
			if(value instanceof CorefNodeData) {
				members.add(((CorefNodeData)value).getSpan());
			} else if(value instanceof CorefEdgeData) {
				Edge edge = ((CorefEdgeData)value).getEdge();
				
				if(!edge.getSource().isROOT()) {
					members.add(edge.getSource());
				}
				members.add(edge.getTarget());
			}
			
			parent.outlineMembers(members, null);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to outline properties: "+String.valueOf(value), e); //$NON-NLS-1$
		}
	}
	
	protected void togglePresenter(Extension extension) {
		if(extension==null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$
		
		if(parent==null) {
			return;
		}
		
		try {
			Options options = new Options();
			
			Object[] cells = getSelectionCells();
			if(cells!=null && cells.length>0) {
				Filter filter = createFilterForCells(cells);
				if(filter!=null) {
					options.put("filter", filter); //$NON-NLS-1$
				}
			}

			if(!options.isEmpty()) {
				parent.togglePresenter(extension, options);
			}
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to switch to presennter: "+extension.getUniqueId(), e); //$NON-NLS-1$
		}
	}
	
	protected Filter createFilterForCells(Object[] cells) {
		if(cells==null || cells.length==0) {
			return null;
		}
		
		Collection<Span> spans = new ArrayList<>();
		
		mxIGraphModel model = getGraph().getModel();
		for(Object cell : cells) {
			if(model.isEdge(cell)) {
				continue;
			}
			
			Object value = model.getValue(cell);
			if(!(value instanceof CorefNodeData)) {
				continue;
			}
			
			CorefNodeData nodeData = (CorefNodeData) value;
			
			spans.add(nodeData.getSpan());
		}
		
		return spans.isEmpty() ? null : new SpanFilters.SpanFilter(spans);
	}
	
	
	protected class CorefHandler extends Handler {

		@Override
		public void invoke(Object sender, mxEventObject evt) {
			if(sender==graph.getSelectionModel()) {
				Object[] selectedCells = graph.getSelectionCells();
				Object cell = null;
				if(selectedCells!=null && selectedCells.length>0) {
					cell = selectedCells[0];
				}
				
				Object value = cell==null ? null : graph.getModel().getValue(cell); 
				outlineProperties(value);
			}
			
			super.invoke(sender, evt);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() instanceof JMenuItem) {
				String uid = e.getActionCommand();
				Extension extension = PluginUtil.getExtension(uid);
				if(extension!=null) {
					togglePresenter(extension);
				}
			} else {
				super.actionPerformed(e);
			}
		}
		
	}

	protected class CorefGraph extends DelegatingGraph {

		@Override
		protected void init() {
			super.init();
			
			setCellsEditable(false);
			setGridEnabled(false);
			setMultigraph(true);
		}

//		@Override
//		public mxRectangle getPreferredSizeForCell(Object cell) {
//			mxRectangle bounds = super.getPreferredSizeForCell(cell);
//			
//			bounds.setWidth(bounds.getWidth()+12);
//			bounds.setHeight(bounds.getHeight()+2);
//			
//			return bounds;
//		}
		
		@Override
		public String convertValueToString(Object cell) {
			
			Object value = model.getValue(cell);

			String label = ((CorefCellData<?>)value).getLabel();
			
			if(label!=null) {
				return label;
			}
			
			if(value instanceof CorefEdgeData) {
//				CorefEdgeData edgeData = (CorefEdgeData) value;
//				label = edgeData.getEdge().toString();
				label = ""; //$NON-NLS-1$
			} else if(value instanceof CorefNodeData) {
				CorefNodeData nodeData = (CorefNodeData) value;
				Span data = nodeData.getSpan();
				
				if(data==null) {
					label = "-"; //$NON-NLS-1$
				} else if(data.isROOT()) {
					label = "\n  Document Root  \n "; //$NON-NLS-1$
				} else {
					StringBuilder sb = new StringBuilder();
					
					int i0 = data.getBeginIndex();
					int i1 = data.getEndIndex();
					
					for(int i=i0; i<=i1; i++) {
						if(i>i0) {
							sb.append(' ');
						}
						sb.append(nodeData.getSentence().getForm(i));
					}
					
					if(isShowSpanBounds()) {
						sb.append('\n');
						data.appendTo(sb);
					}
					
					label = sb.toString();
				}
			}
			
			((CorefCellData<?>)value).setLabel(label);
			
			return label;
		}

		@Override
		public String getToolTipForCell(Object cell) {
			Object value = getModel().getValue(cell);
			
			String tooltip = null;
			
			if(value instanceof CorefNodeData) {
				CorefNodeData nodeData = (CorefNodeData) value;
				if(!nodeData.getSpan().isROOT()) {
					tooltip = CoreferenceUtils.getSpanTooltip(
							nodeData.getSpan(), 
							nodeData.getSentence(), 
							nodeData.getErrorType());
				}
			} else if(value instanceof CorefEdgeData) {
				CorefEdgeData data = (CorefEdgeData) value;
				CorefNodeData nodeData = (CorefNodeData) getModel().getValue(getModel().getTerminal(cell, false));
				tooltip = CoreferenceUtils.getEdgeTooltip(data.getEdge(), nodeData.getErrorType());
			}
			
			return tooltip==null ? null : UIUtil.toUnwrappedSwingTooltip(tooltip);
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

		public void includeGoldEdges(boolean b) {
			setIncludeGoldEdges(b);
		}

		public void includeGoldEdges(ActionEvent e) {
			// ignore
		}

		public void includeGoldNodes(boolean b) {
			setIncludeGoldNodes(b);
		}

		public void includeGoldNodes(ActionEvent e) {
			// ignore
		}

		public void filterSingletons(boolean b) {
			setFilterSingletons(b);
		}

		public void filterSingletons(ActionEvent e) {
			// ignore
		}

		public void showSpanBounds(boolean b) {
			setShowSpanBounds(b);
		}

		public void showSpanBounds(ActionEvent e) {
			// ignore
		}
	}
}
