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

import java.util.Map;

import net.ikarus_systems.icarus.config.ConfigDelegate;
import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.language.dependency.DependencyNodeData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphRenderer;
import net.ikarus_systems.icarus.plugins.jgraph.util.GraphUtils;
import net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.view.TextRenderer;
import net.ikarus_systems.icarus.util.HtmlUtils.HtmlTableBuilder;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.shape.mxITextShape;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyGraphRenderer extends GraphRenderer implements mxITextShape {
	
	protected TextRenderer renderer = new TextRenderer();
	
	protected ConfigDelegate configDelegate;
	
	protected HtmlTableBuilder tableBuilder = new HtmlTableBuilder(500);
	
	protected StringBuilder sb;

	public DependencyGraphRenderer() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(GraphOwner target) {
		if(target instanceof GraphPresenter) {
			configDelegate = ((GraphPresenter)target).getConfigDelegate();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(GraphOwner target) {
		configDelegate = null;
	}
	
	protected boolean isTrue(String key) {
		return configDelegate==null ? false : configDelegate.getBoolean(key);
	}

	@Override
	public mxRectangle getPreferredSizeForCell(GraphOwner owner, Object cell) {
		// TODO Auto-generated method stub
		return super.getPreferredSizeForCell(owner, cell);
	}
	
	protected String normalize(String s) {
		return s==null || s.isEmpty() ? "-" : s; //$NON-NLS-1$
	}

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
			
			// Ensure somthing to be displayed
			if(!showForm && !showLemma && !showFeatures && !showPos) {
				showForm = true;
			}
						
			if (showIndex) {
				sb.append(String.valueOf(nodeData.getIndex()+1)).append(": "); //$NON-NLS-1$
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
				sb.append(normalize(DependencyUtils.getFeatures(nodeData))).append("\n"); //$NON-NLS-1$
			}
			if(markRoot && nodeData.isRoot()) {
				sb.append(LanguageUtils.DATA_ROOT_LABEL);
			}
		} else if(nodeData.hasHead()) {
			boolean showRelation = isTrue("showRelation"); //$NON-NLS-1$
			boolean showDistance = isTrue("showDistance"); //$NON-NLS-1$
			boolean showDirection = isTrue("showDirection"); //$NON-NLS-1$

			if(showRelation) {
				sb.append(normalize(DependencyUtils.getRelation(nodeData))).append("\n"); //$NON-NLS-1$
			}
			if(showDirection) {
				sb.append(normalize(DependencyUtils.getDirection(nodeData)));
			}
			if(showDistance) {
				sb.append(" (").append(String.valueOf(Math.abs(nodeData.getIndex()-nodeData.getHead()))).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
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
					String.valueOf(data.isFlagSet(LanguageUtils.FLAG_PROJECTIVE)));
		}
		
		tableBuilder.finish();
		
		return tableBuilder.getResult();
	}

	@Override
	public mxITextShape getTextShape(Map<String, Object> style, boolean html) {
		return super.getTextShape(style, html);
				
		// TODO DEBUG
		
		//return html ? textShapes.get(TEXT_SHAPE_HTML) : this;
	}
	
	protected void prepareRenderer(mxCellState state) {
		
	}

	/**
	 * @see com.mxgraph.shape.mxITextShape#paintShape(com.mxgraph.canvas.mxGraphics2DCanvas, java.lang.String, com.mxgraph.view.mxCellState, java.util.Map)
	 */
	@Override
	public void paintShape(mxGraphics2DCanvas canvas, String text,
			mxCellState state, Map<String, Object> style) {
		// TODO Auto-generated method stub
		
	}

}
