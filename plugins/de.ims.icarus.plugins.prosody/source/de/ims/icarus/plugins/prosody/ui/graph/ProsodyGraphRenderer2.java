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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.ParseException;
import java.util.Map;

import javax.swing.SwingConstants;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.shape.mxITextShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.config.ConfigDelegate;
import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.language.dependency.DependencyNodeData;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotationManager;
import de.ims.icarus.plugins.prosody.annotation.ProsodyHighlighting;
import de.ims.icarus.plugins.prosody.pattern.ProsodyData;
import de.ims.icarus.plugins.prosody.pattern.ProsodyLevel;
import de.ims.icarus.plugins.prosody.pattern.ProsodyPatternContext;
import de.ims.icarus.plugins.prosody.ui.TextArea;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;
import de.ims.icarus.plugins.prosody.ui.view.PreviewSize;
import de.ims.icarus.plugins.prosody.ui.view.SentenceInfo;
import de.ims.icarus.plugins.prosody.ui.view.SyllableInfo;
import de.ims.icarus.plugins.prosody.ui.view.WordInfo;
import de.ims.icarus.plugins.prosody.ui.view.outline.SentencePanel.PanelConfig;
import de.ims.icarus.search_tools.Grouping;
import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;
import de.ims.icarus.ui.events.ListenerProxies;
import de.ims.icarus.util.annotation.AnnotationDisplayMode;
import de.ims.icarus.util.strings.StringUtil;
import de.ims.icarus.util.strings.pattern.PatternFactory;
import de.ims.icarus.util.strings.pattern.TextSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyGraphRenderer2 extends GraphRenderer implements mxITextShape {

	private static final String FORM_LABEL_PATTERN = "{word:form}"; //$NON-NLS-1$

	public static final String DEFAULT_NODE_LABEL_PATTERN = FORM_LABEL_PATTERN+"\n{word:lemma}\n{word:pos}\n{word:features}"; //$NON-NLS-1$
	public static final String DEFAULT_EDGE_LABEL_PATTERN = "{word:deprel}"; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.jgraph.appearance.prosody"; //$NON-NLS-1$

	protected ProsodySentenceGraphPresenter presenter;

	protected TextArea textArea = new TextArea();
	protected SentenceInfo sentenceInfo;

	private static final ProsodyData patternProxy = new ProsodyData();

	protected TextSource nodeTextPattern;
	protected TextSource edgeTextPattern;
	private boolean showCurvePreview = true;
	private PreviewSize previewSize = PanelConfig.DEFAULT_PREVIEW_SIZE;
	private AntiAliasingType antiAliasingType = PanelConfig.DEFAULT_ANTIALIASING_TYPE;
	private Color curveColor = PanelConfig.DEFAULT_CURVE_COLOR;
	private Color textColor = TextArea.DEFAULT_TEXT_COLOR;
	private Font textFont = TextArea.DEFAULT_FONT;
	private float leftSyllableBound = PanelConfig.DEFAULT_LEFT_SYLLABLE_BOUND;
	private float rightSyllableBound = PanelConfig.DEFAULT_RIGHT_SYLLABLE_BOUND;

	private final ConfigListener configListener = new ConfigListener() {

		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			reloadConfig();
		}
	};

	public ProsodyGraphRenderer2() {
		// no-op
	}

	private static TextSource loadPattern(Handle handle, String defaultPattern, boolean forceFormPattern) {
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

		try {
			return ProsodyPatternContext.createTextSource(ProsodyLevel.WORD, s);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Not a valid pattern string: "+s, e); //$NON-NLS-1$
		}
	}

	protected void reloadConfig() {
		ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
		Handle handle = registry.getHandle(CONFIG_PATH);

		showCurvePreview = registry.getBoolean(registry.getChildHandle(handle, "showCurvePreview")); //$NON-NLS-1$

		nodeTextPattern = loadPattern(registry.getChildHandle(handle, "nodeLabelPattern"), DEFAULT_NODE_LABEL_PATTERN, showCurvePreview); //$NON-NLS-1$
		edgeTextPattern = loadPattern(registry.getChildHandle(handle, "edgeLabelPattern"), DEFAULT_EDGE_LABEL_PATTERN, false); //$NON-NLS-1$

		//TODO load other config entries

		// Reset cached info about syllable level sizing
		sentenceInfo = null;
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof ProsodySentenceGraphPresenter) {
			presenter = (ProsodySentenceGraphPresenter)target;
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

	protected boolean isTrue(String key) {
		ConfigDelegate configDelegate = presenter==null ? null : presenter.getConfigDelegate();
		return configDelegate==null ? false : configDelegate.getBoolean(key);
	}

	protected BitmaskHighlighting getHighlighting() {
		return ProsodyHighlighting.getInstance();
	}

	private static final String COL_KEY = "color"; //$NON-NLS-1$
	private static final String GROUP_KEY = "group"; //$NON-NLS-1$

	protected SentenceInfo sentenceInfo() {
		ProsodicSentenceData sentence = presenter.getSentence();
		if(sentenceInfo==null || sentenceInfo.getSentence()!=sentence) {
			sentenceInfo = new SentenceInfo(sentence);

			// Refresh cached info


			ProsodicAnnotationManager annotationManager = (ProsodicAnnotationManager) presenter.getAnnotationManager();
			ProsodicAnnotation annotation = (ProsodicAnnotation) annotationManager.getAnnotation();
			final boolean hasHighlight = annotationManager.hasAnnotation();

			FontMetrics fm = presenter.getFontMetrics(textFont);

			for(int wordIndex=0; wordIndex<sentenceInfo.wordCount(); wordIndex++) {

				WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);

				// Compute text lines and save them
				String token = wordInfo.getLabel();

//				System.out.printf("form=%s x=%d width=%d sw=%d\n",
//						lines[0], width, areaSize.width, fm.stringWidth(lines[0]));

				// Save word bounds
				wordInfo.setWidth(fm.stringWidth(token));

				boolean wordHighlighted = false;

				if(hasHighlight) {
					long highlight = annotationManager.getHighlight(wordIndex);

					wordHighlighted = ProsodyHighlighting.getInstance().isHighlighted(highlight);

					if(wordHighlighted) {
						Color col = ProsodyHighlighting.getInstance().getGroupColor(highlight);
						if(col!=null) {
							wordInfo.setProperty(GROUP_KEY, true);
						} else {
							col = ProsodyHighlighting.getInstance().getHighlightColor(highlight);
						}

						wordInfo.setProperty(COL_KEY, col);
					}
				}

				// Save syllable bounds
				int wordLength = 0;
				for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
					SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);
					int sylWidth = fm.stringWidth(sylInfo.getLabel());

					sylInfo.setX(wordLength);
					sylInfo.setWidth(sylWidth);

					wordLength += sylWidth;

					if(!wordHighlighted) {
						continue;
					}

					long highlight = annotation.getHighlight(wordIndex, sylIndex);
					if(ProsodyHighlighting.getInstance().isHighlighted(highlight)) {
						Color col = ProsodyHighlighting.getInstance().getGroupColor(highlight);
						if(col!=null) {
							sylInfo.setProperty(GROUP_KEY, true);
						} else {
							col = ProsodyHighlighting.getInstance().getHighlightColor(highlight);
						}

						sylInfo.setProperty(COL_KEY, col);
					}
				}
			}
		}

		return sentenceInfo;
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
		ProsodicAnnotationManager annotationManager = (ProsodicAnnotationManager) presenter.getAnnotationManager();

		if(annotationManager.getDisplayMode()==AnnotationDisplayMode.NONE) {
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

		if(!(value instanceof DependencyNodeData)) {
			return;
		}
		DependencyNodeData data = (DependencyNodeData)value;

		if(!annotationManager.hasAnnotation()) {
			return;
		}

		long highlight = annotationManager.getHighlight(data.getIndex());

		//System.out.println(data.getIndex()+":"+DependencyHighlighting.dumpHighlight(highlight));

		Color color = null;
		int groupId = isNode ?
				getHighlighting().getNodeGroupId(highlight)
				: getHighlighting().getEdgeGroupId(highlight);
		if(groupId!=-1) {
			color = Grouping.getGrouping(groupId).getColor();
		} else {
			color = isNode ?
					getHighlighting().getNodeHighlightColor(highlight)
					: getHighlighting().getEdgeHighlightColor(highlight);
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

		TextSource pattern = nodeTextPattern;

		boolean isNode = model.isVertex(cell);
		if(!isNode) {
			cell = model.getTerminal(cell, false);
			pattern = edgeTextPattern;
		}
		Object value = model.getValue(cell);

		if(!(value instanceof DependencyNodeData)) {
			return null;
		}

		DependencyNodeData nodeData = (DependencyNodeData) value;
		ProsodicSentenceData sentence = presenter.getSentence();

		patternProxy.set(sentence, nodeData.getIndex());


		String text = pattern.getText(patternProxy, null);

		String[] lines = StringUtil.splitLines(text);

		FontMetrics fm = presenter.getFontMetrics(textFont);

		Dimension size = textArea.getSize(lines, fm);

		if(showCurvePreview && isNode) {
			size.height += previewSize.getHeight(fm);
		}

		mxCellState state = graph.getView().getState(cell);
		Map<String, Object> style = (state != null) ? state.getStyle() : graph.getCellStyle(cell);

		if(style!=null) {

			// Adds spacings
			double spacing = mxUtils.getDouble(style, mxConstants.STYLE_SPACING);
			size.width += 2 * spacing;
			size.width += mxUtils.getDouble(style, mxConstants.STYLE_SPACING_LEFT);
			size.width += mxUtils.getDouble(style, mxConstants.STYLE_SPACING_RIGHT);

			size.height += 2 * spacing;
			size.height += mxUtils.getDouble(style, mxConstants.STYLE_SPACING_TOP);
			size.height += mxUtils.getDouble(style, mxConstants.STYLE_SPACING_BOTTOM);
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
			TextSource pattern = nodeTextPattern;

			boolean isNode = model.isVertex(cell);
			if(!isNode) {
				cell = model.getTerminal(cell, false);
				pattern = edgeTextPattern;
			}
			Object value = model.getValue(cell);

			if(!(value instanceof DependencyNodeData)) {
				return;
			}
			DependencyNodeData nodeData = (DependencyNodeData)value;

			ProsodicSentenceData sentence = presenter.getSentence();

			double sp = mxUtils.getDouble(style, mxConstants.STYLE_SPACING);
			double spL = mxUtils.getDouble(style, mxConstants.STYLE_SPACING_LEFT);
			double spR = mxUtils.getDouble(style, mxConstants.STYLE_SPACING_RIGHT);
			double spT = mxUtils.getDouble(style, mxConstants.STYLE_SPACING_TOP);
			double spB = mxUtils.getDouble(style, mxConstants.STYLE_SPACING_BOTTOM);

			rect.x += sp + spL;
			rect.width -= 2*sp + spL + spR;
			rect.y += sp + spT;
			rect.height -= 2*sp + spT + spB;

			// Preview part
			if(showCurvePreview && isNode) {
				antiAliasingType.apply(g);

				g.setColor(curveColor);

				WordInfo wordInfo = sentenceInfo().wordInfo(nodeData.getIndex());

				int width = wordInfo.getWidth();
				double x = rect.x;
				if(textArea.getHorizontalAlignment()==SwingConstants.LEFT) {
					x = rect.x;
				} else if(textArea.getHorizontalAlignment()==SwingConstants.CENTER) {
					x = rect.x + (rect.width - width)/2;
				} else if(textArea.getHorizontalAlignment()==SwingConstants.RIGHT) {
					x = rect.x + rect.width - width;
				}

				int h = previewSize.getHeight(g.getFontMetrics(textArea.getFont()));
				float minD = sentenceInfo.getMinD();
				float maxD = sentenceInfo.getMaxD();
				float scaleY = h/(maxD-minD);

				Color wordCol = (Color) wordInfo.getProperty(COL_KEY);
				if(wordCol==null) {
					wordCol = textColor;
				}

				// Paint curve preview
				for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
					SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);

					// Ignore syllables with alignment outside bounds
					if(leftSyllableBound!=rightSyllableBound
							&& (sylInfo.getB()<leftSyllableBound || sylInfo.getB()>rightSyllableBound)) {
						continue;
					}

					Color sylCol = (Color) sylInfo.getProperty(COL_KEY);
					if(sylCol==null) {
						sylCol = wordCol;
					}

					g.setColor(sylCol);

					int x0 = (int) x;
					int x2 = x0 + sylInfo.getWidth();
					int x1 = (x0+x2) >> 1;

					float d = sylInfo.getD();
					float c1 = sylInfo.getC1();
					float c2 = sylInfo.getC2();

					int y0 = rect.y + h - (int)((d-c1-minD) * scaleY);
					int y1 = rect.y + h - (int)((d-minD) * scaleY);
					int y2 = rect.y + h - (int)((d-c2-minD) * scaleY);

					// Prevent artifacts from antialiasing
					if(y0==y1 && y1==y2) {
						g.drawLine(x0, y0, x2, y2);
					} else {
						g.drawLine(x0, y0, x1, y1);
						g.drawLine(x1, y1, x2, y2);
					}

					x = x2;
				}

				rect.y += h;
			}

			// Label part
			patternProxy.set(sentence, nodeData.getIndex());

			String label = pattern.getText(patternProxy, null);

			String[] lines = StringUtil.splitLines(label);

			textArea.paint(g, lines, rect);

		}
	}
}
