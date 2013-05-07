/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyGraph extends mxGraph {

	public DependencyGraph() {
		init();
	}

	public DependencyGraph(mxIGraphModel model) {
		super(model);
		init();
	}

	public DependencyGraph(mxStylesheet stylesheet) {
		super(stylesheet);
		init();
	}

	public DependencyGraph(mxIGraphModel model, mxStylesheet stylesheet) {
		super(model, stylesheet);
		init();
	}
	
	protected void init() {

		setGridSize(15);
		
		setMultigraph(false);
		setAllowDanglingEdges(false);
		setAllowLoops(false);
		setCellsDisconnectable(false);
		setCellsResizable(false);
		setEdgeLabelsMovable(false);
		setAutoSizeCells(true);
		setHtmlLabels(false);
		setGridEnabled(false);

		getSelectionModel().setSingleSelection(false);
	}

	/**
	 * 
	 * @see com.mxgraph.view.mxGraph#getPreferredSizeForCell(java.lang.Object)
	 */
	@Override
	public mxRectangle getPreferredSizeForCell(Object cell) {
		// TODO Auto-generated method stub
		return super.getPreferredSizeForCell(cell);
	}

	/**
	 * 
	 * @see com.mxgraph.view.mxGraph#convertValueToString(java.lang.Object)
	 */
	@Override
	public String convertValueToString(Object cell) {
		// TODO Auto-generated method stub
		return super.convertValueToString(cell);
	}

	/**
	 * 
	 * @see com.mxgraph.view.mxGraph#getToolTipForCell(java.lang.Object)
	 */
	@Override
	public String getToolTipForCell(Object cell) {
		// TODO Auto-generated method stub
		return super.getToolTipForCell(cell);
	}
}