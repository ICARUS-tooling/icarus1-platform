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
import java.util.logging.Level;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigConstants;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.jgraph.util.GraphUtils;
import net.ikarus_systems.icarus.resources.DefaultResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxIShape;
import com.mxgraph.shape.mxITextShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxEdgeStyle.mxEdgeStyleFunction;
import com.mxgraph.view.mxStyleRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class JGraphPlugin extends Plugin {
	
	public static final String PLUGIN_ID = JGraphConstants.JGRAPH_PLUGIN_ID;

	public JGraphPlugin() {
		// no-op
	}

	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		// Make our resources accessible via the global domain
		ResourceLoader resourceLoader = new DefaultResourceLoader(
				getManager().getPluginClassLoader(getDescriptor()));
		ResourceManager.getInstance().addResource(
				"net.ikarus_systems.icarus.plugins.jgraph.resources.jgraph", resourceLoader); //$NON-NLS-1$

		// Register our icons
		IconRegistry.getGlobalRegistry().addSearchPath(getClass().getClassLoader(), 
				"net/ikarus_systems/icarus/plugins/jgraph/icons/"); //$NON-NLS-1$
		
		// Install shape renderer
		for(Extension extension : getDescriptor().getExtensionPoint("Shape").getConnectedExtensions()) { //$NON-NLS-1$
			try {
				String name = extension.getParameter("name").valueAsString(); //$NON-NLS-1$
				mxIShape shape = (mxIShape) PluginUtil.instantiate(extension);
				mxGraphics2DCanvas.putShape(name, shape);
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to install shape: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
		
		// Install text shape renderer
		for(Extension extension : getDescriptor().getExtensionPoint("TextShape").getConnectedExtensions()) { //$NON-NLS-1$
			try {
				String name = extension.getParameter("name").valueAsString(); //$NON-NLS-1$
				mxITextShape shape = (mxITextShape) PluginUtil.instantiate(extension);
				mxGraphics2DCanvas.putTextShape(name, shape);
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to install text-shape: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
		
		// Install edge styles
		for(Extension extension : getDescriptor().getExtensionPoint("EdgeStyle").getConnectedExtensions()) { //$NON-NLS-1$
			try {
				String name = extension.getParameter("name").valueAsString(); //$NON-NLS-1$
				if(mxStyleRegistry.getValue(name)!=null) {
					LoggerFactory.log(this, Level.WARNING, "Duplicate edge-style for name '"+name+"': "+extension.getUniqueId()); //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}
				
				mxEdgeStyleFunction edgeStyleFunction = (mxEdgeStyleFunction) PluginUtil.instantiate(extension);
				mxStyleRegistry.putValue(name, edgeStyleFunction);
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to install edge-style: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
		
		initConfig();
	}
	
	private void initConfig() {
		ConfigBuilder builder = new ConfigBuilder(ConfigRegistry.getGlobalRegistry());
		
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
				ConfigConstants.RENDERER, GraphUtils.gridStyleRenderer);
		
		// VERTEX SUBGROUP
		builder.addGroup("vertex", true); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("shape", 0,  //$NON-NLS-1$
				"rectangle", //$NON-NLS-1$
				"ellipse", //$NON-NLS-1$
				"doubleEllipse", //$NON-NLS-1$
				"triangle", //$NON-NLS-1$
				"hexagon", //$NON-NLS-1$
				"rhombus"), //$NON-NLS-1$
				ConfigConstants.RENDERER, GraphUtils.shapeRenderer);
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
				ConfigConstants.RENDERER, GraphUtils.shapeRenderer);
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
					ConfigConstants.RENDERER, GraphUtils.fontFamilyRenderer);
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
					ConfigConstants.RENDERER, GraphUtils.fontFamilyRenderer);
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

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
	}

}
