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

import java.util.logging.Level;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxIShape;
import com.mxgraph.shape.mxITextShape;
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
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
	}

}
