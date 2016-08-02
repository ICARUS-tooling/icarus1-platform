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

 * $Revision: 332 $
 * $Date: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.coref/source/de/ims/icarus/plugins/coref/view/graph/CoreferenceGraphRenderer.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.coref.view.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.ParseException;
import java.util.Map;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.shape.mxITextShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentHighlighting;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.coref.pattern.CorefDataProxy;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.search_tools.Grouping;
import de.ims.icarus.ui.events.ListenerProxies;
import de.ims.icarus.ui.text.TextArea;
import de.ims.icarus.util.annotation.AnnotationDisplayMode;
import de.ims.icarus.util.strings.StringUtil;
import de.ims.icarus.util.strings.pattern.PatternFactory;
import de.ims.icarus.util.strings.pattern.TextSource;

/**
 * @author Markus Gärtner
 * @version $Id: CoreferenceGraphRenderer.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class CoreferenceGraphRenderer2 extends GraphRenderer implements mxITextShape {

	public static final String FORM_LABEL_PATTERN = "{word:form}"; //$NON-NLS-1$

	public static final String DEFAULT_NODE_LABEL_PATTERN = FORM_LABEL_PATTERN+"\n{span:sentence_index}-{span:begin_index}-{span:end_index}"; //$NON-NLS-1$
	public static final String DEFAULT_VIRTUAL_NODE_LABEL_PATTERN = "{span:id}"; //$NON-NLS-1$
	public static final String DEFAULT_EDGE_LABEL_PATTERN = ""; //$NON-NLS-1$

	public static final String ROOT_LABEL = "\n   Document Root   \n \n"; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.jgraph.appearance.coref"; //$NON-NLS-1$

	protected CoreferenceGraphPresenter presenter;

	private final TextArea textArea = new TextArea();
	private final CorefDataProxy patternProxy = new CorefDataProxy();

	private final ConfigListener configListener = new ConfigListener() {

		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			reloadConfig();
		}
	};

	private static String loadPattern(Handle handle, String defaultPattern, boolean forceFormPattern) {
		//TODO add sanity check and user notification
		String s = handle.getSource().getString(handle);
		if(s==null) {
			s = defaultPattern;
		}

		s = PatternFactory.unescape(s);

		if(forceFormPattern && !s.startsWith(FORM_LABEL_PATTERN)) {
			if(!s.startsWith("\n")) { //$NON-NLS-1$
				s = "\n"+s; //$NON-NLS-1$
			}
			s = FORM_LABEL_PATTERN+s;
		}

		return s;
	}

	protected void reloadConfig() {
		ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
		Handle handle = registry.getHandle(CONFIG_PATH);

		try {
			presenter.setNodeTextPattern(loadPattern(registry.getChildHandle(handle, "defaultNodeLabelPattern"), DEFAULT_NODE_LABEL_PATTERN, true)); //$NON-NLS-1$
		} catch (ParseException e) {
			LoggerFactory.error(this, "Invalid pattern string for node content", e); //$NON-NLS-1$
		}

		try {
			presenter.setVirtualNodeTextPattern(loadPattern(registry.getChildHandle(handle, "defaultVirtualNodeLabelPattern"), DEFAULT_VIRTUAL_NODE_LABEL_PATTERN, false)); //$NON-NLS-1$
		} catch (ParseException e) {
			LoggerFactory.error(this, "Invalid pattern string for virtual node content", e); //$NON-NLS-1$
		}

		try {
			presenter.setEdgeTextPattern(loadPattern(registry.getChildHandle(handle, "defaultEdgeLabelPattern"), DEFAULT_EDGE_LABEL_PATTERN, false)); //$NON-NLS-1$
		} catch (ParseException e) {
			LoggerFactory.error(this, "Invalid pattern string for edge content", e); //$NON-NLS-1$
		}

		//TODO load other config entries

	}

	public CoreferenceGraphRenderer2() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof CoreferenceGraphPresenter) {
			presenter = (CoreferenceGraphPresenter)target;
		}

		ConfigListener listener = ListenerProxies.getProxy(ConfigListener.class, configListener);
		ConfigRegistry.getGlobalRegistry().addGroupListener(CONFIG_PATH, listener);

		reloadConfig();
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		presenter = null;

		ConfigListener listener = ListenerProxies.getProxy(ConfigListener.class, configListener);
		ConfigRegistry.getGlobalRegistry().removeGroupListener(CONFIG_PATH, listener);
	}

	@Override
	public mxITextShape getTextShape(Map<String, Object> style, boolean html) {
		return html ? textShapes.get(TEXT_SHAPE_HTML) : this;
	}

	protected void refreshCellStyle(mxCellState state) {
		if(presenter==null) {
			return;
		}
		if(presenter.getAnnotationManager()==null) {
			return;
		}
		CoreferenceDocumentAnnotationManager annotationManager = (CoreferenceDocumentAnnotationManager) presenter.getAnnotationManager();

		if(annotationManager.getDisplayMode()==AnnotationDisplayMode.NONE) {
			return;
		}

		if(!annotationManager.hasAnnotation()) {
			return;
		}

		mxIGraphModel model = state.getView().getGraph().getModel();
		Object cell = state.getCell();
		boolean isNode = model.isVertex(cell);
		if(!isNode) {

			if(presenter.isHighlightedIncomingEdge(cell)
					|| presenter.isHighlightedOutgoingEdge(cell)) {
				return;
			}

			cell = model.getTerminal(cell, false);
		}
		Object value = model.getValue(cell);

		if(!(value instanceof CorefNodeData)) {
			return;
		}
		CorefNodeData data = (CorefNodeData)value;

		if(data.getSpan().isROOT()) {
			return;
		}

		int spanIndex = presenter.getIndex(data.getSpan());
		long highlight = annotationManager.getHighlight(spanIndex);

		//System.out.println(data.getIndex()+":"+DependencyHighlighting.dumpHighlight(highlight));

		Color color = null;
		int groupId = isNode ?
				CoreferenceDocumentHighlighting.getInstance().getNodeGroupId(highlight)
				: CoreferenceDocumentHighlighting.getInstance().getEdgeGroupId(highlight);
		if(groupId!=-1) {
			color = Grouping.getGrouping(groupId).getColor();
		} else {
			color = isNode ?
					CoreferenceDocumentHighlighting.getInstance().getNodeHighlightColor(highlight)
					: CoreferenceDocumentHighlighting.getInstance().getEdgeHighlightColor(highlight);
		}

		if(color!=null) {
			Map<String, Object> style = state.getStyle();
			style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.hexString(color));
		}
	}

	@Override
	public Object drawCell(mxCellState state) {
		refreshCellStyle(state);

		return super.drawCell(state);
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphRenderer#getPreferredSizeForCell(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object)
	 */
	@Override
	public mxRectangle getPreferredSizeForCell(GraphOwner owner, Object cell) {

		mxGraph graph = owner.getGraph();
		mxIGraphModel model = graph.getModel();

		TextSource pattern = presenter.getNodeTextPattern();

		boolean isNode = model.isVertex(cell);
		if(!isNode) {
			cell = model.getTerminal(cell, false);
			pattern = presenter.getEdgeTextPattern();
		}
		Object value = model.getValue(cell);

		if(!(value instanceof CorefCellData)) {
			return null;
		}

		if(isNode) {
			CorefNodeData nodeData = (CorefNodeData) value;

			// Special case of artificial root node: let the default implementation take effect
			if(nodeData.getSpan().isROOT()) {
				return null;
			}

			if(nodeData.getSpan().isVirtual()) {
				pattern = presenter.getVirtualNodeTextPattern();
			}

			patternProxy.set(presenter.getPresentedData(), nodeData.getSpan());
		} else {
			CorefEdgeData edgeData = (CorefEdgeData) value;
			patternProxy.set(presenter.getPresentedData(), edgeData.getEdge());
		}

		String text = pattern.getText(patternProxy, null);

		String[] lines = StringUtil.splitLines(text);

		mxCellState state = graph.getView().getState(cell);
		Map<String, Object> style = (state != null) ? state.getStyle() : graph.getCellStyle(cell);

		Dimension size;

		if(style!=null) {

			Font font = mxUtils.getFont(style);
			FontMetrics fm = presenter.getFontMetrics(font);

			size = textArea.getSize(lines, fm);

			// Adds spacings
			double spacing = mxUtils.getDouble(style, mxConstants.STYLE_SPACING);
			size.width += 2 * spacing;
			size.width += mxUtils.getDouble(style, mxConstants.STYLE_SPACING_LEFT);
			size.width += mxUtils.getDouble(style, mxConstants.STYLE_SPACING_RIGHT);

			size.height += 2 * spacing;
			size.height += mxUtils.getDouble(style, mxConstants.STYLE_SPACING_TOP);
			size.height += mxUtils.getDouble(style, mxConstants.STYLE_SPACING_BOTTOM);
		} else {
			size = textArea.getSize(presenter, lines);
		}

		return new mxRectangle(0, 0, size.width, size.height);
	}

	/**
	 * @see com.mxgraph.shape.mxITextShape#paintShape(com.mxgraph.canvas.mxGraphics2DCanvas, java.lang.String, com.mxgraph.view.mxCellState, java.util.Map)
	 */
	@Override
	public void paintShape(mxGraphics2DCanvas canvas, String text,
			mxCellState state, Map<String, Object> style) {

		Rectangle rect = state.getLabelBounds().getRectangle();
		Graphics2D g = canvas.getGraphics();

		if (g.getClipBounds() == null || g.getClipBounds().intersects(rect)) {

			mxIGraphModel model = state.getView().getGraph().getModel();
			Object cell = state.getCell();
			TextSource pattern = presenter.getNodeTextPattern();

			boolean isNode = model.isVertex(cell);
			if(!isNode) {
//				cell = model.getTerminal(cell, false);
				pattern = presenter.getEdgeTextPattern();
			}
			Object value = model.getValue(cell);

			if(!(value instanceof CorefCellData)) {
				return;
			}

			boolean isRoot = false;

			if(isNode) {
				CorefNodeData nodeData = (CorefNodeData) value;
				isRoot = nodeData.getSpan().isROOT();
				patternProxy.set(presenter.getPresentedData(), nodeData.getSpan());

				if(nodeData.getSpan().isVirtual()) {
					pattern = presenter.getVirtualNodeTextPattern();
				}
			} else {
				CorefEdgeData edgeData = (CorefEdgeData) value;
				patternProxy.set(presenter.getPresentedData(), edgeData.getEdge());
			}

			double sp = mxUtils.getDouble(style, mxConstants.STYLE_SPACING);
			double spL = mxUtils.getDouble(style, mxConstants.STYLE_SPACING_LEFT);
			double spR = mxUtils.getDouble(style, mxConstants.STYLE_SPACING_RIGHT);
			double spT = mxUtils.getDouble(style, mxConstants.STYLE_SPACING_TOP);
			double spB = mxUtils.getDouble(style, mxConstants.STYLE_SPACING_BOTTOM);

			rect.x += sp + spL;
			rect.width -= 2*sp + spL + spR;
			rect.y += sp + spT;
			rect.height -= 2*sp + spT + spB;

			g.setFont(mxUtils.getFont(style));
			g.setColor(mxUtils.getColor(style, mxConstants.STYLE_FONTCOLOR, TextArea.DEFAULT_TEXT_COLOR));

			String label = isRoot ? ROOT_LABEL : pattern.getText(patternProxy, null);

			String[] lines = StringUtil.splitLines(label);

			textArea.paint(g, lines, rect);
		}
	}
}
