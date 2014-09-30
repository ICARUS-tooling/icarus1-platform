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
package de.ims.icarus.plugins.dependency.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Map;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.shape.mxITextShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

import de.ims.icarus.config.ConfigDelegate;
import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.dependency.DependencyNodeData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.annotation.DependencyAnnotationManager;
import de.ims.icarus.language.dependency.annotation.DependencyHighlighting;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.Grouping;
import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;
import de.ims.icarus.ui.view.TextRenderer;
import de.ims.icarus.util.HtmlUtils.HtmlTableBuilder;
import de.ims.icarus.util.annotation.AnnotationDisplayMode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyGraphRenderer extends GraphRenderer implements mxITextShape {

	protected TextRenderer renderer = new TextRenderer();

	protected GraphPresenter presenter;

	protected HtmlTableBuilder tableBuilder = new HtmlTableBuilder(500);

	protected StringBuilder sb;

	public DependencyGraphRenderer() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof GraphPresenter) {
			presenter = (GraphPresenter)target;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		presenter = null;
	}

	protected boolean isTrue(String key) {
		ConfigDelegate configDelegate = presenter==null ? null : presenter.getConfigDelegate();
		return configDelegate==null ? false : configDelegate.getBoolean(key);
	}

	protected String normalize(String s) {
		return s==null || s.isEmpty() ? "-" : s; //$NON-NLS-1$
	}

	protected BitmaskHighlighting getHighlighting() {
		return DependencyHighlighting.getInstance();
	}

	protected static final char DISTANCE_SYMBOL = 0x0394; // greek capital delta

	@Override
	public String convertValueToString(GraphOwner owner, Object cell) {
		mxIGraphModel model = owner.getGraph().getModel();

		if(GraphUtils.isOrderEdge(model, cell)) {
			return ""; //$NON-NLS-1$
		}

		Object value = null;
		if(model.isVertex(cell)) {
			value = model.getValue(cell);
		} else if(model.isEdge(cell)) {
			value = model.getValue(model.getTerminal(cell, false));
		}

		if(!(value instanceof DependencyNodeData)) {
			return super.convertValueToString(owner, cell);
		}

		DependencyNodeData nodeData = (DependencyNodeData)value;

		if(sb==null) {
			sb = new StringBuilder(200);
		}
		sb.setLength(0);

		if(model.isVertex(cell)) {
			boolean showIndex = isTrue("showIndex"); //$NON-NLS-1$
			boolean showForm = isTrue("showForm"); //$NON-NLS-1$
			boolean showPos = isTrue("showPos"); //$NON-NLS-1$
			boolean showLemma = isTrue("showLemma"); //$NON-NLS-1$
			boolean showFeatures = isTrue("showFeatures"); //$NON-NLS-1$
			boolean markRoot = isTrue("markRoot"); //$NON-NLS-1$

			// Ensure something to be displayed
			if(!showForm && !showLemma && !showFeatures && !showPos) {
				showForm = true;
			}

			if (showIndex) {
				sb.append(String.valueOf(nodeData.getIndex()+1)).append(":\n"); //$NON-NLS-1$
			}
			if(showForm) {
				sb.append(normalize(DependencyUtils.getForm(nodeData))).append("\n"); //$NON-NLS-1$
			}
			if(showLemma) {
				sb.append(normalize(DependencyUtils.getLemma(nodeData))).append("\n"); //$NON-NLS-1$
			}
			if(showPos) {
				sb.append(normalize(DependencyUtils.getPos(nodeData))).append("\n"); //$NON-NLS-1$
			}
			if(showFeatures) {
				// TODO enable compact view of features
				sb.append(normalize(DependencyUtils.getFeatures(nodeData))).append("\n"); //$NON-NLS-1$
			}
			if(markRoot && nodeData.isRoot()) {
				sb.append(LanguageConstants.DATA_ROOT_LABEL);
			}
		} else if(nodeData.hasHead()) {
			boolean showRelation = isTrue("showRelation"); //$NON-NLS-1$
			boolean showDistance = isTrue("showDistance"); //$NON-NLS-1$
			boolean showDirection = isTrue("showDirection"); //$NON-NLS-1$
			boolean markNonProjective = isTrue("markNonProjective"); //$NON-NLS-1$

			if(showRelation) {
				sb.append(normalize(DependencyUtils.getRelation(nodeData))).append("\n"); //$NON-NLS-1$
			}
			if(showDirection) {
				sb.append(normalize(DependencyUtils.getDirection(nodeData))).append("\n"); //$NON-NLS-1$
			}
			if(showDistance) {
				sb.append(DISTANCE_SYMBOL).append(String.valueOf(Math.abs(nodeData.getIndex()-nodeData.getHead()))).append("\n"); //$NON-NLS-1$
			}
			if(markNonProjective && !nodeData.isProjective()) {
				sb.append("(non-projective)"); //$NON-NLS-1$
			}
		}

		// Delete last line break if present
		if(sb.length()>0 && sb.charAt(sb.length()-1)=='\n') {
			sb.deleteCharAt(sb.length()-1);
		}

		return sb.toString();
	}

	@Override
	public String getToolTipForCell(GraphOwner owner, Object cell) {
		mxIGraphModel model = owner.getGraph().getModel();
		Object value = null;
		if(model.isVertex(cell)) {
			value = model.getValue(cell);
		} else if(model.isEdge(cell)) {
			value = model.getValue(model.getTerminal(cell, false));
		}

		if(!(value instanceof DependencyNodeData)) {
			return null;
		}

		DependencyNodeData data = (DependencyNodeData) value;

		if (model.isVertex(cell)) {
			DependencyNodeData[] children = data.getChildrenArray();

			tableBuilder.start(children==null ? 2 : 3, true);

			tableBuilder.addRow(
					ResourceManager.getInstance().get("plugins.dependency.labels.index"),  //$NON-NLS-1$
					String.valueOf(data.getIndex() + 1),
					DependencyUtils.getIndices(children));

			tableBuilder.addRowEscaped(
					ResourceManager.getInstance().get("plugins.dependency.labels.form"), //$NON-NLS-1$
					normalize(data.getForm()),
					DependencyUtils.getForms(children));

			tableBuilder.addRowEscaped(
					ResourceManager.getInstance().get("plugins.dependency.labels.lemma"), //$NON-NLS-1$
					normalize(data.getLemma()),
					DependencyUtils.getLemmas(children));

			tableBuilder.addRowEscaped(
					ResourceManager.getInstance().get("plugins.dependency.labels.features"), //$NON-NLS-1$
					normalize(data.getFeatures()),
					DependencyUtils.getFeatures(children));

			tableBuilder.addRowEscaped(
					ResourceManager.getInstance().get("plugins.dependency.labels.pos"), //$NON-NLS-1$
					normalize(data.getPos()),
					DependencyUtils.getPoss(children));

			tableBuilder.addRowEscaped(
					ResourceManager.getInstance().get("plugins.dependency.labels.head"), //$NON-NLS-1$
					normalize(LanguageUtils.getHeadLabel(data.getHead())),
					DependencyUtils.getHeads(children));

			tableBuilder.addRowEscaped(
					ResourceManager.getInstance().get("plugins.dependency.labels.relation"), //$NON-NLS-1$
					normalize(data.getRelation()),
					DependencyUtils.getRelations(children));
		} else {

			tableBuilder.start(2, true);

			tableBuilder.addRowEscaped(
					ResourceManager.getInstance().get("plugins.dependency.labels.relation"), //$NON-NLS-1$
					normalize(data.getRelation()));

			tableBuilder.addRowEscaped(
					ResourceManager.getInstance().get("plugins.dependency.labels.projective"), //$NON-NLS-1$
					String.valueOf(data.isFlagSet(LanguageConstants.FLAG_PROJECTIVE)));
		}

		tableBuilder.finish();

		return tableBuilder.getResult();
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
		DependencyAnnotationManager annotationManager = (DependencyAnnotationManager) presenter.getAnnotationManager();

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

		/*System.out.println(((mxCell)state.getCell()).getValue());
		System.out.print(CollectionUtils.toString(state.getStyle()));
		System.out.println();*/

		return super.drawCell(state);
	}

	/**
	 * @see com.mxgraph.shape.mxITextShape#paintShape(com.mxgraph.canvas.mxGraphics2DCanvas,
	 *      java.lang.String, com.mxgraph.view.mxCellState, java.util.Map)
	 */
	@Override
	public void paintShape(mxGraphics2DCanvas canvas, String text,
			mxCellState state, Map<String, Object> style) {
		Rectangle rect = state.getLabelBounds().getRectangle();
		Graphics2D g = canvas.getGraphics();

		if (g.getClipBounds() == null || g.getClipBounds().intersects(rect)) {

			// BEGIN ORIGINAL
			boolean horizontal = mxUtils.isTrue(style,
					mxConstants.STYLE_HORIZONTAL, true);
			double scale = canvas.getScale();
			int x = rect.x;
			int y = rect.y;
			int w = rect.width;
			int h = rect.height;

			if (!horizontal) {
				g.rotate(-Math.PI / 2, x + w / 2, y + h / 2);
				g.translate(w / 2 - h / 2, h / 2 - w / 2);
			}

			Color fontColor = mxUtils.getColor(style,
					mxConstants.STYLE_FONTCOLOR, Color.black);
			g.setColor(fontColor);

			// Shifts the y-coordinate down by the ascent plus a workaround
			// for the line not starting at the exact vertical location
			Font scaledFont = mxUtils.getFont(style, scale);
			g.setFont(scaledFont);
			int fontSize = mxUtils.getInt(style, mxConstants.STYLE_FONTSIZE,
					mxConstants.DEFAULT_FONTSIZE);
			FontMetrics fm = g.getFontMetrics();
			int scaledFontSize = scaledFont.getSize();
			double fontScaleFactor = ((double) scaledFontSize)
					/ ((double) fontSize);
			// This factor is the amount by which the font is smaller/
			// larger than we expect for the given scale. 1 means it's
			// correct, 0.8 means the font is 0.8 the size we expected
			// when scaled, etc.
			double fontScaleRatio = fontScaleFactor / scale;
			// The y position has to be moved by (1 - ratio) * height / 2
			y += 2 * fm.getMaxAscent() - fm.getHeight()
					+ mxConstants.LABEL_INSET * scale;

			Object vertAlign = mxUtils.getString(style,
					mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
			double vertAlignProportion = 0.5;

			if (vertAlign.equals(mxConstants.ALIGN_TOP)) {
				vertAlignProportion = 0;
			} else if (vertAlign.equals(mxConstants.ALIGN_BOTTOM)) {
				vertAlignProportion = 1.0;
			}

			y += (1.0 - fontScaleRatio) * h * vertAlignProportion;

			// Gets the alignment settings
			Object align = mxUtils.getString(style, mxConstants.STYLE_ALIGN,
					mxConstants.ALIGN_CENTER);

			if (align.equals(mxConstants.ALIGN_LEFT)) {
				x += mxConstants.LABEL_INSET * scale;
			} else if (align.equals(mxConstants.ALIGN_RIGHT)) {
				x -= mxConstants.LABEL_INSET * scale;
			}

			// END ORIGINAL

			// Try to paint the text in annotated form
			if(paintAnnotatedText(text, state, g, fm, x, y, w, h, align, horizontal)) {
				return;
			}

			// If annotating the text failed paint it in plain form
			paintText(text, state, g, fm, x, y, w, h, align, horizontal);
		}
	}

	private String[] nodeTokens = {
		null,
		"form", //$NON-NLS-1$
		"lemma", //$NON-NLS-1$
		"pos", //$NON-NLS-1$
		"features", //$NON-NLS-1$
		null,
	};
	private boolean[] nodeVisibility = new boolean[nodeTokens.length];

	private String[] edgeTokens = {
		"relation", //$NON-NLS-1$
		"direction", //$NON-NLS-1$
		"distance", //$NON-NLS-1$
		"projectivity", //$NON-NLS-1$
	};
	private boolean[] edgeVisibility = new boolean[edgeTokens.length];

	protected boolean paintAnnotatedText(String text, mxCellState state, Graphics2D g, FontMetrics fm,
			int x, int y, int w, int h, Object align, boolean horizontal) {

		if(presenter==null) {
			return false;
		}
		if(presenter.getAnnotationManager()==null) {
			return false;
		}
		DependencyAnnotationManager annotationManager = (DependencyAnnotationManager) presenter.getAnnotationManager();

		if(annotationManager.getDisplayMode()==AnnotationDisplayMode.NONE) {
			return false;
		}

		mxIGraphModel model = state.getView().getGraph().getModel();
		Object cell = state.getCell();
		boolean isNode = model.isVertex(cell);
		if(!isNode) {
			cell = model.getTerminal(cell, false);
		}
		Object value = model.getValue(cell);

		if(!(value instanceof DependencyNodeData)) {
			return false;
		}
		DependencyNodeData data = (DependencyNodeData)value;


		if(!annotationManager.hasAnnotation()) {
			return false;
		}

		long highlight = annotationManager.getHighlight(data.getIndex());
		if(highlight==0L) {
			return false;
		}

		String[] lines = text.split("\n"); //$NON-NLS-1$

		Color[] colors = new Color[lines.length];
		boolean[] vis;
		String[] tokens;

		if(isNode) {
			vis = nodeVisibility;
			vis[0] = isTrue("showIndex"); //$NON-NLS-1$
			vis[1] = isTrue("showForm"); //$NON-NLS-1$
			vis[2] = isTrue("showLemma"); //$NON-NLS-1$
			vis[3] = isTrue("showPos"); //$NON-NLS-1$
			vis[4] = isTrue("showFeatures"); //$NON-NLS-1$
			vis[5] = isTrue("markRoot"); //$NON-NLS-1$

			// Ensure something to be displayed
			if(!vis[1] && !vis[2] && !vis[3] && !vis[4]) {
				vis[1] = true;
			}
			tokens = nodeTokens;
		} else {
			vis = edgeVisibility;
			vis[0] = isTrue("showRelation"); //$NON-NLS-1$
			vis[1] = isTrue("showDirection"); //$NON-NLS-1$
			vis[2] = isTrue("showDistance"); //$NON-NLS-1$
			vis[3] = isTrue("markNonProjective"); //$NON-NLS-1$

			tokens = edgeTokens;
		}

		int index = 0;
		for(int i=0; i<tokens.length; i++) {
			if(vis[i]) {
				String token = tokens[i];
				if(token!=null) {
					colors[index] = getHighlighting().getHighlightColor(highlight, token);
				}
				index++;
			}
		}

		Color c = g.getColor();
		for (int i = 0; i < lines.length; i++) {
			int dx = 0;

			if (align.equals(mxConstants.ALIGN_CENTER)) {
				int sw = fm.stringWidth(lines[i]);

				if (horizontal) {
					dx = (w - sw) / 2;
				} else {
					dx = (h - sw) / 2;
				}
			} else if (align.equals(mxConstants.ALIGN_RIGHT)) {
				int sw = fm.stringWidth(lines[i]);
				dx = ((horizontal) ? w : h) - sw;
			}

			Color col = colors[i];
			if(col==null) {
				col = c;
			}

			g.setColor(col);
			g.drawString(lines[i], x + dx, y);
			y += fm.getHeight() + mxConstants.LINESPACING;
		}
		g.setColor(c);

		return true;
	}

	protected void paintText(String text, mxCellState state, Graphics2D g, FontMetrics fm,
			int x, int y, int w, int h, Object align, boolean horizontal) {
		// Draws the text line by line
		String[] lines = text.split("\n"); //$NON-NLS-1$

		for (int i = 0; i < lines.length; i++) {
			int dx = 0;

			if (align.equals(mxConstants.ALIGN_CENTER)) {
				int sw = fm.stringWidth(lines[i]);

				if (horizontal) {
					dx = (w - sw) / 2;
				} else {
					dx = (h - sw) / 2;
				}
			} else if (align.equals(mxConstants.ALIGN_RIGHT)) {
				int sw = fm.stringWidth(lines[i]);
				dx = ((horizontal) ? w : h) - sw;
			}

			g.drawString(lines[i], x + dx, y);
			y += fm.getHeight() + mxConstants.LINESPACING;
		}
	}
}
