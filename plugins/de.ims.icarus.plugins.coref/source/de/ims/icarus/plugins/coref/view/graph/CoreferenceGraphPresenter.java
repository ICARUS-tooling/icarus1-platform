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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MutableComboBoxModel;

import org.java.plugin.registry.Extension;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.handler.mxConnectPreview;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.config.ConfigDelegate;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.CorefComparison;
import de.ims.icarus.language.coref.CorefErrorType;
import de.ims.icarus.language.coref.CorefMember;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.helper.SpanFilters;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.coref.pattern.CorefLevel;
import de.ims.icarus.plugins.coref.pattern.CorefPatternContext;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentDataPresenter;
import de.ims.icarus.plugins.coref.view.PatternExample;
import de.ims.icarus.plugins.jgraph.layout.GraphLayout;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.jgraph.layout.GraphStyle;
import de.ims.icarus.plugins.jgraph.util.CellBuffer;
import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.resources.Localizable;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.TooltipFreezer;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.dialog.DummyFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.list.ListUtils;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.AnnotationControl;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.strings.pattern.PatternFactory;
import de.ims.icarus.util.strings.pattern.TextSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceGraphPresenter extends GraphPresenter implements Installable {

	private static final long serialVersionUID = -6564065119073757454L;

	public static final String NODE_DATA_TYPE = CorefNodeData.class.getName();

	public static final String EDGE_DATA_TYPE = CorefEdgeData.class.getName();

	protected CoreferenceDocumentDataPresenter parent;

	protected Filter filter;

	protected DocumentData document;
	protected CoreferenceAllocation allocation;
	protected CoreferenceAllocation goldAllocation;

	protected boolean includeGoldEdges = false;
	protected boolean includeGoldNodes = true;
	protected boolean markFalseEdges = true;
	protected boolean markFalseNodes = true;
	protected boolean filterSingletons = true;
	protected boolean showSpanBounds = true;

	protected CoreferenceDocumentDataPresenter.PresenterMenu presenterMenu;

//	protected SpanCache cache = new SpanCache();

//	protected PatternLabelBuilder labelBuilder = new PatternLabelBuilder(
//			"$form$\\\\ns-b-e", ""); //$NON-NLS-1$ //$NON-NLS-2$

	protected TextSource nodeTextPattern;
	protected TextSource virtualNodeTextPattern;
	protected TextSource edgeTextPattern;

	protected JComboBox<Object> nodePatternSelect;
	protected JComboBox<Object> virtualNodePatternSelect;
	protected JComboBox<Object> edgePatternSelect;
	protected JLabel patternSelectInfo;

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
		return new CoreferenceGraphRenderer2();
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#createConnectPreview()
	 */
	@Override
	protected mxConnectPreview createConnectPreview() {
		return null;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#createConnectionHandler()
	 */
	@Override
	protected mxConnectionHandler createConnectionHandler() {
		return null;
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

	public Filter getfFilter() {
		return filter;
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
	public DocumentData getPresentedData() {
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
		document = (DocumentData) data;

		if(options==null) {
			options = Options.emptyOptions;
		}
		allocation = (CoreferenceAllocation) options.get("allocation"); //$NON-NLS-1$
		goldAllocation = (CoreferenceAllocation) options.get("goldAllocation"); //$NON-NLS-1$

		filter = (Filter) options.get("filter"); //$NON-NLS-1$
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

	protected JComboBox<Object> createPatternSelect(String defaultPattern) {
		JComboBox<Object> patternSelect = new JComboBox<>();
		String pattern = defaultPattern;

		if(pattern!=null && pattern.trim().isEmpty()) {
			pattern = null;
		}

		patternSelect.setEditable(true);
		if(pattern!=null) {
			MutableComboBoxModel<Object> model = (MutableComboBoxModel<Object>) patternSelect.getModel();
			model.addElement(pattern);
		}
		patternSelect.setSelectedItem(pattern);
		patternSelect.addActionListener(getHandler());
		UIUtil.resizeComponent(patternSelect, 300, 24);

		return patternSelect;
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

	protected String createPatternSelectTooltip() {
		return CorefPatternContext.getInfoText();

//		StringBuilder sb = new StringBuilder(300);
//		ResourceManager rm = ResourceManager.getInstance();
//
//		sb.append("<html>"); //$NON-NLS-1$
//		sb.append("<h3>").append(rm.get("plugins.coref.labelPattern.title")).append("</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		sb.append("<table>"); //$NON-NLS-1$
//		sb.append("<tr><th>") //$NON-NLS-1$
//			.append(rm.get("plugins.coref.labelPattern.character")).append("</th><th>") //$NON-NLS-1$ //$NON-NLS-2$
//			.append(rm.get("plugins.coref.labelPattern.description")).append("</th></tr>"); //$NON-NLS-1$ //$NON-NLS-2$
//
//		Map<Object, Object> mc = PatternLabelBuilder.magicCharacters;
//		for(Entry<Object, Object> entry : mc.entrySet()) {
//			String c = entry.getKey().toString();
//			String key = entry.getValue().toString();
//
//			sb.append("<tr><td>").append(HtmlUtils.escapeHTML(c)) //$NON-NLS-1$
//			.append("</td><td>").append(rm.get(key)).append("</td></tr>"); //$NON-NLS-1$ //$NON-NLS-2$
//		}
//
//		sb.append("</table>"); //$NON-NLS-1$
//
//		return sb.toString();
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
		actionManager.addHandler(
				"plugins.coref.coreferenceGraphPresenter.editLabelPatternsAction",  //$NON-NLS-1$
				callbackHandler, "editLabelPatterns"); //$NON-NLS-1$
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

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#refreshAll()
	 */
	@Override
	public void refreshAll() {
		super.refreshAll();


		Filter filter = getfFilter();

		if(filter==null) {
			return;
		}

//		this.filter = null;
		Collection<Object> selection = new ArrayList<>();

		mxIGraphModel model = graph.getModel();
		final Object parent = graph.getDefaultParent();
		int cellCount = model.getChildCount(parent);

		for(int i=0; i<cellCount; i++) {
			Object cell = model.getChildAt(parent, i);

			if(model.isEdge(cell)) {
				continue;
			}

			CorefNodeData value = (CorefNodeData) model.getValue(cell);

			if(filter.accepts(value.getSpan())) {
				selection.add(cell);
			}
		}

		if(selection.isEmpty()) {
			return;
		}

		final Object[] cells = new Object[selection.size()];
		selection.toArray(cells);

		UIUtil.invokeLater(new Runnable() {

			@Override
			public void run() {
				graph.setSelectionCells(cells);
//				mxRectangle bounds = graph.getBoundsForGroup(parent, cells, 0);
//				scrollRectToVisible(bounds.getRectangle());
			}
		});
	}

	protected Object createVertex(Span span, CorefErrorType nodeType, boolean gold) {
		CoreferenceData sentence = (span.isROOT() || span.isVirtual()) ?
				CoreferenceUtils.emptySentence
				: document.get(span.getSentenceIndex());

		long highlight = 0L;
//		CoreferenceDocumentAnnotationManager annotationManager = getAnnotationManager();
//		if(annotationManager!=null && annotationManager.hasAnnotation() && !span.isROOT()) {
//			int index = cache.getIndex(span);
//			long hl = annotationManager.getHighlight(index);
//			if(CoreferenceDocumentHighlighting.getInstance().isNodeHighlighted(hl)) {
//				long mask = ~(CoreferenceDocumentHighlighting.getInstance().getEdgeGroupingMask()
//						| CoreferenceDocumentHighlighting.getInstance().getEdgeHighlightMask());
//				highlight |= (hl & mask);
//			}
//		}

		mxCell cell = new mxCell(new CorefNodeData(span, sentence, nodeType, gold, highlight));
		cell.setVertex(true);
		cell.setGeometry(new mxGeometry());

		return cell;
	}

	protected Object createEdge(Edge edge, Object source, Object target, boolean gold) {

		long highlight = 0L;
//		CoreferenceDocumentAnnotationManager annotationManager = getAnnotationManager();
//		if(annotationManager!=null && annotationManager.hasAnnotation()) {
//			int index = cache.getIndex(edge.getTarget());
//			long hl = annotationManager.getHighlight(index);
//			if(CoreferenceDocumentHighlighting.getInstance().isEdgeHighlighted(hl)) {
//				long mask = ~(CoreferenceDocumentHighlighting.getInstance().getNodeGroupingMask()
//						| CoreferenceDocumentHighlighting.getInstance().getNodeHighlightMask());
//				highlight |= (hl & mask);
//			}
//		}

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

			if(edgeSet==null) {
				edgeSet = CoreferenceUtils.defaultEmptyEdgeSet;
			}

			// Clear gold set if it is the same as the one displayed
			if(edgeSet==goldSet) {
				goldSet = null;
			}

			Map<Span, Object> cellMap = new HashMap<>();
			Object parent = graph.getDefaultParent();

			CorefComparison comparison = CoreferenceUtils.compare(edgeSet, goldSet, isFilterSingletons());

//			cache.clear();
//			cache.cacheEdges(comparison.getEdgeSet().getEdges());

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

	public int getIndex(Span span) {
		return span.getIndex();
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
		if(includeGoldEdges==this.includeGoldEdges)
			return;

		boolean oldValue = this.includeGoldEdges;
		this.includeGoldEdges = includeGoldEdges;

		rebuildGraph();

		getActionManager().setSelected(includeGoldEdges,
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldEdgesAction"); //$NON-NLS-1$

		firePropertyChange("includeGoldEdges", oldValue, includeGoldEdges); //$NON-NLS-1$
	}

	public void setIncludeGoldNodes(boolean includeGoldNodes) {
		if(includeGoldNodes==this.includeGoldNodes)
			return;

		boolean oldValue = this.includeGoldNodes;
		this.includeGoldNodes = includeGoldNodes;

		rebuildGraph();

		getActionManager().setSelected(includeGoldNodes,
				"plugins.coref.coreferenceGraphPresenter.toggleIncludeGoldNodesAction"); //$NON-NLS-1$

		firePropertyChange("includeGoldNodes", oldValue, includeGoldNodes); //$NON-NLS-1$
	}

	public void setMarkFalseEdges(boolean markFalseEdges) {
		if(markFalseEdges==this.markFalseEdges)
			return;

		boolean oldValue = this.markFalseEdges;
		this.markFalseEdges = markFalseEdges;

		rebuildGraph();

		getActionManager().setSelected(markFalseEdges,
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseEdgesAction"); //$NON-NLS-1$

		firePropertyChange("markFalseEdges", oldValue, markFalseEdges); //$NON-NLS-1$
	}

	public void setMarkFalseNodes(boolean markFalseNodes) {
		if(markFalseNodes==this.markFalseNodes)
			return;

		boolean oldValue = this.markFalseNodes;
		this.markFalseNodes = markFalseNodes;

		rebuildGraph();

		getActionManager().setSelected(markFalseNodes,
				"plugins.coref.coreferenceGraphPresenter.toggleMarkFalseNodesAction"); //$NON-NLS-1$

		firePropertyChange("markFalseNodes", oldValue, markFalseNodes); //$NON-NLS-1$
	}

	public void setFilterSingletons(boolean filterSingletons) {
		if(filterSingletons==this.filterSingletons)
			return;

		boolean oldValue = this.filterSingletons;
		this.filterSingletons = filterSingletons;

		rebuildGraph();

		getActionManager().setSelected(filterSingletons,
				"plugins.coref.coreferenceGraphPresenter.toggleFilterSingletonsAction"); //$NON-NLS-1$

		firePropertyChange("filterSingletons", oldValue, filterSingletons); //$NON-NLS-1$
	}

	public TextSource getNodeTextPattern() {
		return nodeTextPattern;
	}

	public TextSource getEdgeTextPattern() {
		return edgeTextPattern;
	}

	public TextSource getVirtualNodeTextPattern() {
		return virtualNodeTextPattern;
	}

	protected void setVirtualNodeTextPattern(String virtualNodeTextPattern) throws ParseException {
		this.virtualNodeTextPattern = CorefPatternContext.createTextSource(CorefLevel.SPAN, virtualNodeTextPattern);
	}

	protected void setNodeTextPattern(String nodeTextPattern) throws ParseException {
		this.nodeTextPattern = CorefPatternContext.createTextSource(CorefLevel.SPAN, nodeTextPattern);
	}

	protected void setEdgeTextPattern(String edgeTextPattern) throws ParseException {
		this.edgeTextPattern = CorefPatternContext.createTextSource(CorefLevel.EDGE, edgeTextPattern);
	}

	protected void outlineProperties(Object value) {
		if(parent==null)
			return;

		try {
			Collection<CorefMember> members = new LinkedList<>();

			if(value instanceof CorefNodeData) {
				Span span = ((CorefNodeData)value).getSpan();
				if(!span.isROOT() && !span.isVirtual()) {
					members.add(span);
				}
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

		if(parent==null)
			return;

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
		if(cells==null || cells.length==0)
			return null;

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
			throw new NullPointerException("Invalid cell buffer"); //$NON-NLS-1$

		if(document==null)
			throw new IllegalStateException("Cannot import cells without a reference document being present"); //$NON-NLS-1$

		if(!NODE_DATA_TYPE.equals(buffer.graphType)) {
			return;
		}

		Object[] cells = CellBuffer.buildCells(buffer);

		// TODO Verify content?

		if(cells==null || cells.length==0) {
			return;
		}

		//FIXME currently can only import stuff that originated from the same document

		mxIGraphModel model = graph.getModel();
		// Assign sentence objects to each node
		for(Object cell : cells) {
			if(model.isVertex(cell)) {
				CorefNodeData data = (CorefNodeData) model.getValue(cell);
				Span span = data.getSpan();
				data.setSentence(document.get(span.getSentenceIndex()));
			}
		}

		graph.moveCells(cells, 0, 0, true);
	}

	@Override
	public CellBuffer exportCells(Object[] cells) {
		return cells!=null ?
				CellBuffer.createBuffer(cells, graph.getModel(), NODE_DATA_TYPE)
				: CellBuffer.createBuffer(graph.getModel(),	null, NODE_DATA_TYPE);
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
			setBorder(20);
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

			String label = null;

			if(value instanceof CorefNodeData) {
				CorefNodeData nodeData = (CorefNodeData) value;

				if(nodeData.getSpan().isROOT()) {
					label = CoreferenceGraphRenderer2.ROOT_LABEL;
				}
			}

			CorefCellData<?> data = (CorefCellData<?>) value;

			if(label==null) {
				label = data.data.toString();
			}

			data.setLabel(label);

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

		private String getPattern(JComboBox<Object> comboBox) {
			Object value = comboBox.getSelectedItem();
			String pattern = null;

			if(value instanceof String) {
				pattern = (String) value;
			} else if(value instanceof PatternExample) {
				pattern = ((PatternExample)value).getPattern();
			}

			return pattern;
		}

		private void addPattern(Object pattern, JComboBox<Object> comboBox) {

			// Legal pattern
			MutableComboBoxModel<Object> model =
					(MutableComboBoxModel<Object>) comboBox.getModel();

			int index = ListUtils.indexOf(pattern, model);

			if(index==-1) {
				model.addElement(pattern);
			}
		}

		public void editLabelPatterns(ActionEvent e) {
			if(nodePatternSelect==null) {
				String pattern = ConfigRegistry.getGlobalRegistry().getString(
						"plugins.jgraph.appearance.coref.defaultNodeLabelPattern"); //$NON-NLS-1$
				nodePatternSelect = createPatternSelect(pattern);
			}
			if(edgePatternSelect==null) {
				String pattern = ConfigRegistry.getGlobalRegistry().getString(
						"plugins.jgraph.appearance.coref.defaultEdgeLabelPattern"); //$NON-NLS-1$
				edgePatternSelect = createPatternSelect(pattern);
			}
			if(virtualNodePatternSelect==null) {
				String pattern = ConfigRegistry.getGlobalRegistry().getString(
						"plugins.jgraph.appearance.coref.defaultVirtualNodeLabelPattern"); //$NON-NLS-1$
				virtualNodePatternSelect = createPatternSelect(pattern);
			}

			if(patternSelectInfo==null) {
				final JLabel label = new JLabel();
				label.addMouseListener(new TooltipFreezer());
				label.setIcon(UIUtil.getInfoIcon());

				Localizable localizable = new Localizable() {

					@Override
					public void localize() {
						label.setToolTipText(createPatternSelectTooltip());
					}
				};

				localizable.localize();
				ResourceManager.getInstance().getGlobalDomain().addItem(localizable);

				patternSelectInfo = label;
			}

			nodePatternSelect.setSelectedItem(PatternFactory.escape(nodeTextPattern.getExternalForm()));
			virtualNodePatternSelect.setSelectedItem(PatternFactory.escape(virtualNodeTextPattern.getExternalForm()));
			edgePatternSelect.setSelectedItem(PatternFactory.escape(edgeTextPattern.getExternalForm()));

			FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
			formBuilder.addEntry("info", new DummyFormEntry( //$NON-NLS-1$
					"plugins.coref.coreferenceGraphPresenter.dialogs.editPattern.info", patternSelectInfo)); //$NON-NLS-1$
			formBuilder.addEntry("nodePattern", new ChoiceFormEntry( //$NON-NLS-1$
					"plugins.coref.coreferenceGraphPresenter.dialogs.editPattern.nodePattern", nodePatternSelect)); //$NON-NLS-1$
			formBuilder.addEntry("virtualNodePattern", new ChoiceFormEntry( //$NON-NLS-1$
					"plugins.coref.coreferenceGraphPresenter.dialogs.editPattern.virtualNodePattern", virtualNodePatternSelect)); //$NON-NLS-1$
			formBuilder.addEntry("edgePattern", new ChoiceFormEntry( //$NON-NLS-1$
					"plugins.coref.coreferenceGraphPresenter.dialogs.editPattern.edgePattern", edgePatternSelect)); //$NON-NLS-1$

			formBuilder.buildForm();

			if(DialogFactory.getGlobalFactory().showGenericDialog(
					null,
					DialogFactory.OK_CANCEL_OPTION,
					"plugins.coref.coreferenceGraphPresenter.dialogs.editPattern.title", //$NON-NLS-1$
					"plugins.coref.coreferenceGraphPresenter.dialogs.editPattern.message", //$NON-NLS-1$
					formBuilder.getContainer(),
					true)) {

				// Node pattern
				String nodePattern = getPattern(nodePatternSelect);
				if(nodePattern==null || nodePattern.isEmpty()) {
					// Ensure minimum label!
					nodePattern = CoreferenceGraphRenderer2.FORM_LABEL_PATTERN;
				}
				nodePattern = PatternFactory.unescape(nodePattern);

				try {
					setNodeTextPattern(nodePattern);
					addPattern(nodePattern, nodePatternSelect);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Invalid node pattern: "+nodePattern, ex); //$NON-NLS-1$

					UIUtil.beep();
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.coref.coreferenceGraphPresenter.dialogs.invalidNodePattern.title",  //$NON-NLS-1$
							"plugins.coref.coreferenceGraphPresenter.dialogs.invalidNodePattern.message",  //$NON-NLS-1$
							nodePattern);

					return;
				}

				// Virtual node pattern
				String virtualNodePattern = getPattern(virtualNodePatternSelect);
				if(virtualNodePattern==null) {
					virtualNodePattern = ""; //$NON-NLS-1$
				}
				virtualNodePattern = PatternFactory.unescape(virtualNodePattern);

				//FIXME check why we always get an empty pattern

				try {
					setVirtualNodeTextPattern(virtualNodePattern);
					addPattern(virtualNodePattern, virtualNodePatternSelect);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Invalid virtual node pattern: "+virtualNodePattern, ex); //$NON-NLS-1$

					UIUtil.beep();
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.coref.coreferenceGraphPresenter.dialogs.invalidNodePattern.title",  //$NON-NLS-1$
							"plugins.coref.coreferenceGraphPresenter.dialogs.invalidNodePattern.message",  //$NON-NLS-1$
							virtualNodePattern);

					return;
				}

				// Edge pattern
				String edgePattern = getPattern(edgePatternSelect);
				if(edgePattern==null) {
					edgePattern = ""; //$NON-NLS-1$
				}
				edgePattern = PatternFactory.unescape(edgePattern);

				try {
					setEdgeTextPattern(edgePattern);
					addPattern(edgePattern, edgePatternSelect);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Invalid edge pattern: "+edgePattern, ex); //$NON-NLS-1$

					UIUtil.beep();
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.coref.coreferenceGraphPresenter.dialogs.invalidEdgePattern.title",  //$NON-NLS-1$
							"plugins.coref.coreferenceGraphPresenter.dialogs.invalidEdgePattern.message",  //$NON-NLS-1$
							edgePattern);

					return;
				}

//				refreshLayout();
				CoreferenceGraphPresenter.this.rebuildGraph();
			}
		}
	}
}
