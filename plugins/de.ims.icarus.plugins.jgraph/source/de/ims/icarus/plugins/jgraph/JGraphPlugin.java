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

import java.util.logging.Level;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxIShape;
import com.mxgraph.shape.mxITextShape;
import com.mxgraph.view.mxEdgeStyle.mxEdgeStyleFunction;
import com.mxgraph.view.mxStyleRegistry;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;

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
