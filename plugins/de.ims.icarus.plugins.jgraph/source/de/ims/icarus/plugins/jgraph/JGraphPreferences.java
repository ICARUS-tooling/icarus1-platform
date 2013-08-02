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
package de.ims.icarus.plugins.jgraph;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.mxgraph.swing.mxGraphComponent;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigConstants;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class JGraphPreferences {

	public JGraphPreferences() {
		ConfigBuilder builder = new ConfigBuilder();
		
		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// JGRAPH GROUP
		builder.addGroup("jgraph", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// DEFAULT GROUP
		builder.addGroup("default", true); //$NON-NLS-1$
		
		buildDefaultGraphConfig(builder, null);
	}
	
	public static void buildDefaultGraphConfig(ConfigBuilder builder, Options options) {
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		builder.addBooleanEntry("compressGraph",  //$NON-NLS-1$
				options.get("compressGraph", false)); //$NON-NLS-1$
		builder.addBooleanEntry("autoZoom",  //$NON-NLS-1$
				options.get("autoZoom", false)); //$NON-NLS-1$
		builder.addBooleanEntry("gridVisible",  //$NON-NLS-1$
				options.get("gridVisible", false)); //$NON-NLS-1$
		builder.addBooleanEntry("gridEnabled",  //$NON-NLS-1$
				options.get("gridEnabled", true)); //$NON-NLS-1$
		builder.addIntegerEntry("gridSize", 10, 5, 50, 1); //$NON-NLS-1$
		builder.addColorEntry("incomingEdgeColor",  //$NON-NLS-1$
				options.get("incomingEdgeColor", new Color(255, 127, 42).getRGB())); //$NON-NLS-1$
		builder.addColorEntry("outgoingEdgeColor",  //$NON-NLS-1$
				options.get("outgoingEdgeColor", new Color(85, 212, 255).getRGB())); //$NON-NLS-1$
		builder.addColorEntry("gridColor",  //$NON-NLS-1$
				options.get("gridColor", Color.black.getRGB())); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("gridStyle",  //$NON-NLS-1$
				options.get("selectedGridStyle", 0),  //$NON-NLS-1$
				mxGraphComponent.GRID_STYLE_DOT, 
				mxGraphComponent.GRID_STYLE_CROSS, 
				mxGraphComponent.GRID_STYLE_LINE, 
				mxGraphComponent.GRID_STYLE_DASHED),
				ConfigConstants.RENDERER, gridStyleRenderer);
		
		// VERTEX SUBGROUP
		builder.addGroup("vertex", true); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("shape",  //$NON-NLS-1$
				options.get("selectedVertexShape", 0),  //$NON-NLS-1$
				"rectangle", //$NON-NLS-1$
				"ellipse", //$NON-NLS-1$
				"doubleEllipse", //$NON-NLS-1$
				"triangle", //$NON-NLS-1$
				"hexagon", //$NON-NLS-1$
				"rhombus"), //$NON-NLS-1$
				ConfigConstants.RENDERER, shapeRenderer);
		builder.addBooleanEntry("shapeRounded",  //$NON-NLS-1$
				options.get("vertexShapeRounded", false)); //$NON-NLS-1$
		builder.addOptionsEntry("direction",  //$NON-NLS-1$
				options.get("selectedVertexDirection", 0),  //$NON-NLS-1$
				"east", //$NON-NLS-1$
				"west", //$NON-NLS-1$
				"north", //$NON-NLS-1$
				"south"); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("perimeter",  //$NON-NLS-1$
				options.get("selectedVertexPerimeter", 0),  //$NON-NLS-1$
				"rectanglePerimeter", //$NON-NLS-1$
				"ellipsePerimeter", //$NON-NLS-1$
				"trianglePerimeter", //$NON-NLS-1$
				"hexagonPerimeter", //$NON-NLS-1$
				"rhombusPerimeter"), //$NON-NLS-1$
				ConfigConstants.RENDERER, shapeRenderer);
		builder.addOptionsEntry("align",  //$NON-NLS-1$
				options.get("selectedVertexAlign", 1),  //$NON-NLS-1$
				"left", //$NON-NLS-1$
				"center", //$NON-NLS-1$
				"right"); //$NON-NLS-1$
		builder.addOptionsEntry("verticalAlign",  //$NON-NLS-1$
				options.get("selectedVertexVerticalALign", 1),  //$NON-NLS-1$
				"top", //$NON-NLS-1$
				"middle", //$NON-NLS-1$
				"bottom"); //$NON-NLS-1$
		builder.addOptionsEntry("labelPosition",  //$NON-NLS-1$
				options.get("selectedVertexLabelPosition", 1),  //$NON-NLS-1$
				"left", //$NON-NLS-1$
				"center", //$NON-NLS-1$
				"right"); //$NON-NLS-1$
		builder.addOptionsEntry("verticalLabelPosition",  //$NON-NLS-1$
				options.get("selectedVertexVerticalLabelPosition", 1),  //$NON-NLS-1$
				"top", //$NON-NLS-1$
				"middle", //$NON-NLS-1$
				"bottom"); //$NON-NLS-1$
		builder.addColorEntry("strokeColor",  //$NON-NLS-1$
				options.get("vertexStrokeColor", Color.blue.getRGB())); //$NON-NLS-1$
		builder.addColorEntry("fillColor",  //$NON-NLS-1$
				options.get("vertexFillColor", Color.white.getRGB())); //$NON-NLS-1$
		builder.addIntegerEntry("strokeWidth", 1, 1, 5); //$NON-NLS-1$
		//builder.addIntegerEntry("perimeterSpacing", 0, 0, 8);
		builder.addIntegerEntry("spacing", 3, 0, 25); //$NON-NLS-1$
		builder.addIntegerEntry("spacingTop", 0, 0, 25); //$NON-NLS-1$
		builder.addIntegerEntry("spacingLeft", 0, 0, 25); //$NON-NLS-1$
		builder.addIntegerEntry("spacingRight", 0, 0, 25); //$NON-NLS-1$
		builder.addIntegerEntry("spacingBottom", 0, 0, 25); //$NON-NLS-1$
		// VERTEX FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		builder.virtual();
		ConfigUtils.buildDefaultFontConfig(builder, 
				options.get("vertexFont", "Dialog")); //$NON-NLS-1$ //$NON-NLS-2$
		builder.addBooleanEntry("underline", false); //$NON-NLS-1$
		builder.addBooleanEntry("shadow", false); //$NON-NLS-1$
		builder.back();
		// END VERTEX FONT SUBGROUP
		builder.back();
		// END VERTEX SUBGROUP
		
		// EDGE SUBGROUP
		builder.addGroup("edge", true); //$NON-NLS-1$
		builder.addOptionsEntry("startArrow",  //$NON-NLS-1$
				options.get("selectedEdgeStartArrow", 0), //$NON-NLS-1$
				"none", //$NON-NLS-1$
				"classic", //$NON-NLS-1$
				"block", //$NON-NLS-1$
				"open", //$NON-NLS-1$
				"oval", //$NON-NLS-1$
				"diamond"); //$NON-NLS-1$
		builder.addOptionsEntry("endArrow",  //$NON-NLS-1$
				options.get("selectedEdgeEndArrow", 1), //$NON-NLS-1$
				"none", //$NON-NLS-1$
				"classic", //$NON-NLS-1$
				"block", //$NON-NLS-1$
				"open", //$NON-NLS-1$
				"oval", //$NON-NLS-1$
				"diamond"); //$NON-NLS-1$
		builder.addOptionsEntry("align",  //$NON-NLS-1$
				options.get("selectedEdgeAlign", 1),  //$NON-NLS-1$
				"top", //$NON-NLS-1$
				"center", //$NON-NLS-1$
				"bottom"); //$NON-NLS-1$
		builder.addOptionsEntry("verticalAlign",  //$NON-NLS-1$
				options.get("selectedEdgeVerticalAlign", 1),  //$NON-NLS-1$
				"top", //$NON-NLS-1$
				"middle", //$NON-NLS-1$
				"bottom"); //$NON-NLS-1$
		builder.addColorEntry("strokeColor",  //$NON-NLS-1$
				options.get("edgeStrokeColor", Color.black.getRGB())); //$NON-NLS-1$
		builder.addIntegerEntry("strokeWidth", 1, 1, 5); //$NON-NLS-1$
		builder.addIntegerEntry("sourcePerimeterSpacing", 0, 0, 8); //$NON-NLS-1$
		builder.addIntegerEntry("targetPerimeterSpacing", 0, 0, 8); //$NON-NLS-1$
		builder.addBooleanEntry("clearLabel", false); //$NON-NLS-1$
		// EDGE FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		builder.virtual();
		ConfigUtils.buildDefaultFontConfig(builder, 
				options.get("edgeFont", "Dialog")); //$NON-NLS-1$ //$NON-NLS-2$
		builder.addBooleanEntry("underline", false); //$NON-NLS-1$
		builder.addBooleanEntry("shadow", false); //$NON-NLS-1$
		builder.back();
		// END EDGE FONT SUBGROUP
		builder.back();
		// END EDGE SUBGROUP
		builder.back();
		// END
	}
	
	public static ListCellRenderer<?> shapeRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = -5300412794404495530L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			String shape = (String)value;
			shape = shape.replace("Perimeter", ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			setIcon(IconRegistry.getGlobalRegistry().getIcon(String.format("shape_%s.gif", shape))); //$NON-NLS-1$
			setText(ResourceManager.getInstance().get("config.options."+shape)); //$NON-NLS-1$
			
			return this;
		}
	};
	
	public static ListCellRenderer<?> gridStyleRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = -5300412794404495530L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			/*
			 * 	public static final int GRID_STYLE_DOT = 0;
			 *	public static final int GRID_STYLE_CROSS = 1;
			 *	public static final int GRID_STYLE_LINE = 2;
			 *  public static final int GRID_STYLE_DASHED = 3;
			 */
			switch ((Integer)value) {
			case 0:
				setText(ResourceManager.getInstance().get("config.options.gridStyleDot")); //$NON-NLS-1$
				setIcon(IconRegistry.getGlobalRegistry().getIcon("grid_dot.gif")); //$NON-NLS-1$
				break;

			case 1:
				setText(ResourceManager.getInstance().get("config.options.gridStyleCross")); //$NON-NLS-1$
				setIcon(IconRegistry.getGlobalRegistry().getIcon("grid_cross.gif")); //$NON-NLS-1$
				break;

			case 2:
				setText(ResourceManager.getInstance().get("config.options.gridStyleLine")); //$NON-NLS-1$
				setIcon(IconRegistry.getGlobalRegistry().getIcon("grid_line.gif")); //$NON-NLS-1$
				break;

			case 3:
				setText(ResourceManager.getInstance().get("config.options.gridStyleDashed")); //$NON-NLS-1$
				setIcon(IconRegistry.getGlobalRegistry().getIcon("grid_dashed.gif")); //$NON-NLS-1$
				break;

			default:
				break;
			}
			
			
			return this; 
		}
	};

}
