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
package de.ims.icarus.plugins.jgraph.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

/**
 * Extended version of the default {@link mxUndoManager} with the ability to
 * track undo operations on a certain graph and to adjust to changes of the
 * model and view properties of that graph. In addition this implementation
 * can be paused to allow for changes on the graph that are not reflected in
 * the undo history.
 * <p>
 * Note:<br>
 * While pausing the undo manager and therefore excluding edits from being
 * stored is useful when moving cells in the context of some "graphical cleanup",
 * it can be particularly dangerous when the correct result of a chain of
 * edits depends on all of them being executed!
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class GraphUndoManager extends mxUndoManager 
		implements mxIEventListener, PropertyChangeListener {
	
	protected boolean paused;

	public GraphUndoManager() {
		super();
	}

	public GraphUndoManager(int size) {
		super(size);
	}

	public void setPaused(boolean paused) {
		if(this.paused==paused) {
			return;
		}
		this.paused = paused;
		
		fireEvent(new mxEventObject("paused")); //$NON-NLS-1$
	}
	
	public boolean isPaused() {
		return paused;
	}

	@Override
	public void undoableEditHappened(mxUndoableEdit undoableEdit) {
		if(isPaused()) {
			return;
		}
		super.undoableEditHappened(undoableEdit);
	}
	
	public void install(mxGraph graph) {
		graph.getModel().addListener(mxEvent.UNDO, this);
		graph.getView().addListener(mxEvent.UNDO, this);
		
		graph.addPropertyChangeListener("view", this); //$NON-NLS-1$
		graph.addPropertyChangeListener("model", this); //$NON-NLS-1$
	}
	
	public void uninstall(mxGraph graph) {
		graph.getModel().removeListener(this);
		graph.getView().removeListener(this);
		
		graph.removePropertyChangeListener(this);
	}

	/**
	 * @see com.mxgraph.util.mxEventSource.mxIEventListener#invoke(java.lang.Object, com.mxgraph.util.mxEventObject)
	 */
	@Override
	public void invoke(Object sender, mxEventObject evt) {
		undoableEditHappened((mxUndoableEdit) evt.getProperty("edit")); //$NON-NLS-1$
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if("view".equals(evt.getPropertyName())) { //$NON-NLS-1$
			mxGraphView oldView = (mxGraphView)evt.getOldValue();
			mxGraphView newView = (mxGraphView)evt.getNewValue();
			
			if(oldView!=null) {
				oldView.removeListener(this);
			}
			
			if(newView!=null) {
				newView.addListener(mxEvent.UNDO, this);
			}
		} else {
			mxIGraphModel oldModel = (mxIGraphModel)evt.getOldValue();
			mxIGraphModel newModel = (mxIGraphModel)evt.getNewValue();
			
			if(oldModel!=null) {
				oldModel.removeListener(this);
			}
			
			if(newModel!=null) {
				newModel.addListener(mxEvent.UNDO, this);
			}
		}
	}
	
	
}
