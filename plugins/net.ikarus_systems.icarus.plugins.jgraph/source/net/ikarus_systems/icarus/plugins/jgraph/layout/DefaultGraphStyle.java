/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.layout;

import java.awt.Color;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.config.ConfigDelegate;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter;
import net.ikarus_systems.icarus.util.Options;

import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxStylesheet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultGraphStyle implements GraphStyle, ChangeListener {
	
	protected mxStylesheet stylesheet;

	public DefaultGraphStyle() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(GraphOwner target) {
		if(target instanceof GraphPresenter) {
			ConfigDelegate configDelegate = ((GraphPresenter) target).getConfigDelegate();
			if(configDelegate!=null) {
				configDelegate.addChangeListener(this);
			}
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(GraphOwner target) {
		if(target instanceof GraphPresenter) {
			ConfigDelegate configDelegate = ((GraphPresenter) target).getConfigDelegate();
			if(configDelegate!=null) {
				configDelegate.removeChangeListener(this);
			}
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphStyle#createStylesheet(net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public mxStylesheet createStylesheet(GraphOwner owner, Options options) {
		if(stylesheet==null) {
			stylesheet = new mxStylesheet();
			
			if(owner instanceof GraphPresenter) {
				ConfigDelegate configDelegate = ((GraphPresenter) owner).getConfigDelegate();
				if(configDelegate!=null) {
					refreshStylesheet(configDelegate.getHandle());
				}
			}
		}
		
		return stylesheet;
	}
	
	protected void initStylesheet() {
		Map<String, Object> vertexStyle = stylesheet.getDefaultVertexStyle();
		vertexStyle.put("shape", "rectangle"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("perimeter", "rectanglePerimeter"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("verticalAlign", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("align", "center"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("verticalLabelPosition", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("labelPosition", "center"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("strokeColor", "blue"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("fontColor", "black"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("fontSize", 12); //$NON-NLS-1$
		vertexStyle.put("overflow", "fill"); //$NON-NLS-1$ //$NON-NLS-2$
		vertexStyle.put("fillColor", "white"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Map<String, Object> edgeStyle = stylesheet.getDefaultEdgeStyle();
		edgeStyle.put("shape", "arc"); //$NON-NLS-1$ //$NON-NLS-2$
		edgeStyle.put("edgeStyle", "topArcEdgeStyle"); //$NON-NLS-1$ //$NON-NLS-2$
		edgeStyle.put("labelBackgroundColor", "white"); //$NON-NLS-1$ //$NON-NLS-2$
		edgeStyle.put("endArrow", "classic"); //$NON-NLS-1$ //$NON-NLS-2$
		edgeStyle.put("verticalAlign", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
		edgeStyle.put("align", "center"); //$NON-NLS-1$ //$NON-NLS-2$
		edgeStyle.put("strokeColor", "black"); //$NON-NLS-1$ //$NON-NLS-2$
		edgeStyle.put("exitPerimeter", 1); //$NON-NLS-1$ 
		edgeStyle.put("entryPerimeter", 1); //$NON-NLS-1$ 
		edgeStyle.put("exitY", 0.0); //$NON-NLS-1$
		edgeStyle.put("exitX", 0.5); //$NON-NLS-1$
		edgeStyle.put("entryY", 0.0); //$NON-NLS-1$
		edgeStyle.put("entryX", 0.5); //$NON-NLS-1$
		
		edgeStyle.put("labelBorderColor", "white"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	protected void refreshStylesheet(Handle handle) {
		if(stylesheet==null) {
			stylesheet = new mxStylesheet();
		}

		ConfigRegistry config = handle.getSource();
		
		readDefaultNodeStyle(stylesheet.getDefaultVertexStyle(), 
				config.getChildHandle(handle, "vertex")); //$NON-NLS-1$
		
		readDefaultEdgeStyle(stylesheet.getDefaultEdgeStyle(), 
				config.getChildHandle(handle, "edge")); //$NON-NLS-1$
	}
	
	protected String int2ColString(int value) {		
		return mxUtils.getHexColorString(new Color(value));
	}
	
	protected void readDefaultNodeStyle(Map<String, Object> style, Handle handle) {
		ConfigRegistry config = handle.getSource();
		
		style.put("shape", config.getString(config.getChildHandle(handle, "shape"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("rounded", config.getBoolean(config.getChildHandle(handle, "shapeRounded"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("perimeter", config.getString(config.getChildHandle(handle, "perimeter"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("direction", config.getString(config.getChildHandle(handle, "direction"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("verticalAlign", config.getString(config.getChildHandle(handle, "verticalAlign"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("align", config.getString(config.getChildHandle(handle, "align"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("verticalLabelPosition", config.getString(config.getChildHandle(handle, "verticalLabelPosition"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("labelPosition", config.getString(config.getChildHandle(handle, "labelPosition"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("strokeColor", int2ColString(config.getInteger(config.getChildHandle(handle, "strokeColor")))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("fillColor", int2ColString(config.getInteger(config.getChildHandle(handle, "fillColor")))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("strokeWidth", config.getInteger(config.getChildHandle(handle, "strokeWidth"))); //$NON-NLS-1$ //$NON-NLS-2$
		//style.put("perimeterSpacing", config.getInteger(config.getChildHandle(handle, "perimeterSpacing")));
		style.put("spacing", config.getInteger(config.getChildHandle(handle, "spacing"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("spacingTop", config.getInteger(config.getChildHandle(handle, "spacingTop"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("spacingLeft", config.getInteger(config.getChildHandle(handle, "spacingLeft"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("spacingRight", config.getInteger(config.getChildHandle(handle, "spacingRight"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("spacingBottom", config.getInteger(config.getChildHandle(handle, "spacingBottom"))); //$NON-NLS-1$ //$NON-NLS-2$
		
		handle = config.getChildHandle(handle, "font"); //$NON-NLS-1$
		readFontStyle(style, handle);
	}
	
	protected void readDefaultEdgeStyle(Map<String, Object> style, Handle handle) {
		ConfigRegistry config = handle.getSource();
		
		style.put("startArrow", config.getString(config.getChildHandle(handle, "startArrow"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("endArrow", config.getString(config.getChildHandle(handle, "endArrow"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("verticalAlign", config.getString(config.getChildHandle(handle, "verticalAlign"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("align", config.getString(config.getChildHandle(handle, "align"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("strokeColor", int2ColString(config.getInteger(config.getChildHandle(handle, "strokeColor")))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("strokeWidth", config.getInteger(config.getChildHandle(handle, "strokeWidth"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("sourcePerimeterSpacing", config.getInteger(config.getChildHandle(handle, "sourcePerimeterSpacing"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("targetPerimeterSpacing", config.getInteger(config.getChildHandle(handle, "targetPerimeterSpacing"))); //$NON-NLS-1$ //$NON-NLS-2$
		
		if(config.getBoolean(config.getChildHandle(handle, "clearLabel"))) { //$NON-NLS-1$
			style.put("labelBackgroundColor", "white"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			style.remove("labelBackgroundColor"); //$NON-NLS-1$
		}
		
		handle = config.getChildHandle(handle, "font"); //$NON-NLS-1$
		readFontStyle(style, handle);
	}
	
	protected void readFontStyle(Map<String, Object> style, Handle handle) {
		ConfigRegistry config = handle.getSource();
		
		style.put("fontFamily", config.getString(config.getChildHandle(handle, "fontFamily"))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("fontColor", int2ColString(config.getInteger(config.getChildHandle(handle, "fontColor")))); //$NON-NLS-1$ //$NON-NLS-2$
		style.put("fontSize", config.getInteger(config.getChildHandle(handle, "fontSize"))); //$NON-NLS-1$ //$NON-NLS-2$
		
		int fontStyle = 0;
		if(config.getBoolean(config.getChildHandle(handle, "bold"))) //$NON-NLS-1$
			fontStyle |= mxConstants.FONT_BOLD;
		if(config.getBoolean(config.getChildHandle(handle, "italic"))) //$NON-NLS-1$
			fontStyle |= mxConstants.FONT_ITALIC;
		if(config.getBoolean(config.getChildHandle(handle, "underline"))) //$NON-NLS-1$
			fontStyle |= mxConstants.FONT_UNDERLINE;
		if(config.getBoolean(config.getChildHandle(handle, "shadow"))) //$NON-NLS-1$
			fontStyle |= mxConstants.FONT_SHADOW;
		
		style.put("fontStyle", fontStyle); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphStyle#getStyle(net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public String getStyle(GraphOwner owner, Object cell, Options options) {
		return owner.getGraph().getModel().isVertex(cell) ? "defaultVertex" : "defaultEdge"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		ConfigDelegate configDelegate = (ConfigDelegate) e.getSource();
		refreshStylesheet(configDelegate.getHandle());
	}

}
