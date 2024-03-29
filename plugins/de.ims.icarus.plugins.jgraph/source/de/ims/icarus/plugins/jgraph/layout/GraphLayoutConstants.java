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
package de.ims.icarus.plugins.jgraph.layout;

import javax.swing.SwingConstants;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface GraphLayoutConstants {

	public static final String GRAPH_PRESENTER_KEY = "graphPresenter"; //$NON-NLS-1$
	public static final String STYLE_DECORATOR_KEY = "styleDecorator"; //$NON-NLS-1$
	public static final String CELL_MERGER_KEY = "cellMerger"; //$NON-NLS-1$
	public static final String CELL_FILTER_KEY = "cellFilter"; //$NON-NLS-1$
	
	public static final String MIN_BASELINE_KEY = "minBaseline"; //$NON-NLS-1$
	public static final String TOP_INSETS_KEY = "topInsets"; //$NON-NLS-1$
	public static final String BOTTOM_INSETS_KEY = "bottomInsets"; //$NON-NLS-1$
	public static final String LEFT_INSETS_KEY = "leftInsets"; //$NON-NLS-1$
	public static final String RIGHT_INSETS_KEY = "rightInsets"; //$NON-NLS-1$
	public static final String CENTER_GRAPH_KEY = "centerGraph"; //$NON-NLS-1$
	public static final String CONTENT_AREA_KEY = "contentArea"; //$NON-NLS-1$
	public static final String CELL_SPACING_KEY = "cellSpacing"; //$NON-NLS-1$

	public static final String OFFSET_X_KEY = "offsetX"; //$NON-NLS-1$
	public static final String OFFSET_Y_KEY = "offsetY"; //$NON-NLS-1$

	public static final String DEFAULT_EDGE_STYLE_KEY = "defaultEdgeStyle"; //$NON-NLS-1$
	public static final String ORDER_EDGE_STYLE_KEY = "orderEdgeStyle"; //$NON-NLS-1$
	public static final String LTR_EDGE_STYLE_KEY = "ltrEdgeStyle"; //$NON-NLS-1$
	public static final String RTL_EDGE_STYLE_KEY = "rtlEdgeStyle"; //$NON-NLS-1$
	
	public static final String DEFAULT_EDGE_STYLE = "defaultEdge"; //$NON-NLS-1$
	public static final String DEFAULT_ORDER_EDGE_STYLE = "orderEdge"; //$NON-NLS-1$
	public static final String DEFAULT_LTR_EDGE_STYLE = "ltrEdge"; //$NON-NLS-1$
	public static final String DEFAULT_RTL_EDGE_STYLE = "rtlEdge"; //$NON-NLS-1$
	public static final String DEFAULT_VERTEX_STYLE = "defaultVertex"; //$NON-NLS-1$

	public static final double ARC_HEIGHT_RATIO = 1d/7d;
	public static final double ARC_BASE_OFFSET = 10d;
	
	public static final double ARC_TOP_EXTEND = 5d;

	public static final int DEFAULT_MIN_BASELINE = 200;

	public static final int DEFAULT_CONTENT_AREA = SwingConstants.SOUTH;
	
	public static final int DEFAULT_TOP_INSETS = 20;
	public static final int DEFAULT_BOTTOM_INSETS = 20;
	public static final int DEFAULT_LEFT_INSETS = 20;
	public static final int DEFAULT_RIGHT_INSETS = 20;
}
