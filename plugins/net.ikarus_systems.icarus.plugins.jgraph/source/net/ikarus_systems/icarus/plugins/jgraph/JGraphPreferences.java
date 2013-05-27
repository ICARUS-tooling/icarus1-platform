/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigConstants;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;

import com.mxgraph.swing.mxGraphComponent;

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
		
		buildDefaultGraphConfig(builder);
	}
	
	public static void buildDefaultGraphConfig(ConfigBuilder builder) {

		builder.addBooleanEntry("compressGraph", false); //$NON-NLS-1$
		builder.addBooleanEntry("autoZoom", false); //$NON-NLS-1$
		builder.addBooleanEntry("gridVisible", false); //$NON-NLS-1$
		builder.addIntegerEntry("gridSize", 10, 5, 50, 1); //$NON-NLS-1$
		builder.addColorEntry("gridColor", Color.black.getRGB()); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("gridStyle", 0,  //$NON-NLS-1$
				mxGraphComponent.GRID_STYLE_DOT, 
				mxGraphComponent.GRID_STYLE_CROSS, 
				mxGraphComponent.GRID_STYLE_LINE, 
				mxGraphComponent.GRID_STYLE_DASHED),
				ConfigConstants.RENDERER, gridStyleRenderer);
		
		// VERTEX SUBGROUP
		builder.addGroup("vertex", true); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("shape", 0,  //$NON-NLS-1$
				"rectangle", //$NON-NLS-1$
				"ellipse", //$NON-NLS-1$
				"doubleEllipse", //$NON-NLS-1$
				"triangle", //$NON-NLS-1$
				"hexagon", //$NON-NLS-1$
				"rhombus"), //$NON-NLS-1$
				ConfigConstants.RENDERER, shapeRenderer);
		builder.addBooleanEntry("shapeRounded", false); //$NON-NLS-1$
		builder.addOptionsEntry("direction", 0,  //$NON-NLS-1$
				"east", //$NON-NLS-1$
				"west", //$NON-NLS-1$
				"north", //$NON-NLS-1$
				"south"); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("perimeter", 0,  //$NON-NLS-1$
				"rectanglePerimeter", //$NON-NLS-1$
				"ellipsePerimeter", //$NON-NLS-1$
				"trianglePerimeter", //$NON-NLS-1$
				"hexagonPerimeter", //$NON-NLS-1$
				"rhombusPerimeter"), //$NON-NLS-1$
				ConfigConstants.RENDERER, shapeRenderer);
		builder.addOptionsEntry("align", 1,  //$NON-NLS-1$
				"left", //$NON-NLS-1$
				"center", //$NON-NLS-1$
				"right"); //$NON-NLS-1$
		builder.addOptionsEntry("verticalAlign", 1,  //$NON-NLS-1$
				"top", //$NON-NLS-1$
				"middle", //$NON-NLS-1$
				"bottom"); //$NON-NLS-1$
		builder.addOptionsEntry("labelPosition", 1,  //$NON-NLS-1$
				"left", //$NON-NLS-1$
				"center", //$NON-NLS-1$
				"right"); //$NON-NLS-1$
		builder.addOptionsEntry("verticalLabelPosition", 1,  //$NON-NLS-1$
				"top", //$NON-NLS-1$
				"middle", //$NON-NLS-1$
				"bottom"); //$NON-NLS-1$
		builder.addColorEntry("strokeColor", Color.blue.getRGB()); //$NON-NLS-1$
		builder.addColorEntry("fillColor", Color.white.getRGB()); //$NON-NLS-1$
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
		builder.setProperties(builder.addOptionsEntry("fontFamily", 0,  //$NON-NLS-1$
				"Dialog", //$NON-NLS-1$
				"Arial", //$NON-NLS-1$
				"Verdana", //$NON-NLS-1$
				"Times New Roman"), //$NON-NLS-1$
					ConfigConstants.RENDERER, fontFamilyRenderer);
		// TODO add more font family types
		builder.addIntegerEntry("fontSize", 12, 5, 35); //$NON-NLS-1$
		builder.addColorEntry("fontColor", Color.black.getRGB()); //$NON-NLS-1$
		builder.addBooleanEntry("bold", false); //$NON-NLS-1$
		builder.addBooleanEntry("italic", false); //$NON-NLS-1$
		builder.addBooleanEntry("underline", false); //$NON-NLS-1$
		builder.addBooleanEntry("shadow", false); //$NON-NLS-1$
		builder.back();
		// END VERTEX FONT SUBGROUP
		builder.back();
		// END VERTEX SUBGROUP
		
		// EDGE SUBGROUP
		builder.addGroup("edge", true); //$NON-NLS-1$
		builder.addOptionsEntry("startArrow", 0, //$NON-NLS-1$
				"none", //$NON-NLS-1$
				"classic", //$NON-NLS-1$
				"block", //$NON-NLS-1$
				"open", //$NON-NLS-1$
				"oval", //$NON-NLS-1$
				"diamond"); //$NON-NLS-1$
		builder.addOptionsEntry("endArrow", 1, //$NON-NLS-1$
				"none", //$NON-NLS-1$
				"classic", //$NON-NLS-1$
				"block", //$NON-NLS-1$
				"open", //$NON-NLS-1$
				"oval", //$NON-NLS-1$
				"diamond"); //$NON-NLS-1$
		builder.addOptionsEntry("align", 1,  //$NON-NLS-1$
				"top", //$NON-NLS-1$
				"center", //$NON-NLS-1$
				"bottom"); //$NON-NLS-1$
		builder.addOptionsEntry("verticalAlign", 1,  //$NON-NLS-1$
				"top", //$NON-NLS-1$
				"middle", //$NON-NLS-1$
				"bottom"); //$NON-NLS-1$
		builder.addColorEntry("strokeColor", Color.black.getRGB()); //$NON-NLS-1$
		builder.addIntegerEntry("strokeWidth", 1, 1, 5); //$NON-NLS-1$
		builder.addIntegerEntry("sourcePerimeterSpacing", 0, 0, 8); //$NON-NLS-1$
		builder.addIntegerEntry("targetPerimeterSpacing", 0, 0, 8); //$NON-NLS-1$
		builder.addBooleanEntry("clearLabel", false); //$NON-NLS-1$
		// EDGE FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		builder.virtual();
		builder.setProperties(builder.addOptionsEntry("fontFamily", 0,  //$NON-NLS-1$
				"Dialog", //$NON-NLS-1$
				"Arial", //$NON-NLS-1$
				"Verdana", //$NON-NLS-1$
				"Times New Roman"), //$NON-NLS-1$
					ConfigConstants.RENDERER, fontFamilyRenderer);
		builder.addIntegerEntry("fontSize", 12, 5, 35); //$NON-NLS-1$
		builder.addColorEntry("fontColor", Color.black.getRGB()); //$NON-NLS-1$
		builder.addBooleanEntry("bold", false); //$NON-NLS-1$
		builder.addBooleanEntry("italic", false); //$NON-NLS-1$
		builder.addBooleanEntry("underline", false); //$NON-NLS-1$
		builder.addBooleanEntry("shadow", false); //$NON-NLS-1$
		builder.back();
		// END EDGE FONT SUBGROUP
		builder.back();
		// END EDGE SUBGROUP
		builder.back();
		// END
	}

	public static ListCellRenderer<Object> fontFamilyRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = -333044504694220350L;
		
		private Map<String, Font> fonts;
		
		private Font getFont(String name) {
			if(fonts==null)
				fonts = new Hashtable<String, Font>();
			
			Font font = fonts.get(name);
			
			if(font==null) {
				font = getFont();
				font = new Font(name, font.getStyle(), font.getSize());
				fonts.put(name, font);
			}
			
			return font;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			setFont(getFont((String) value));
			
			return this;
		}
	};
	
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
