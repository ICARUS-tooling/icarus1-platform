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
package de.ims.icarus.ui.list;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DynamicWidthList<E extends Object> extends JList<E> {

	private static final long serialVersionUID = 6834514207357872598L;
	
	protected boolean trackViewportWidth = false;
	protected boolean synchronizeFixedCellWidth = true;
	
	public DynamicWidthList() {
		super();
		
		init();
	}

	public DynamicWidthList(E[] listData) {
		super(listData);
		
		init();
	}

	public DynamicWidthList(ListModel<E> dataModel) {
		super(dataModel);
		
		init();
	}

	public DynamicWidthList(Vector<? extends E> listData) {
		super(listData);
		
		init();
	}
	
	protected void init() {
		addComponentListener(createSizeTracker());
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return trackViewportWidth;
	}

	public boolean isTrackViewportWidth() {
		return trackViewportWidth;
	}
	
	public boolean isSynchronizeFixedCellWidth() {
		return synchronizeFixedCellWidth;
	}

	/**
	 * Note:<br>
	 * Disabling the automatic synchronization of the 'fixedCellWidth'
	 * property based on the current width of the list component should
	 * always be followed by manually setting the desired value for
	 * this property or deactivating it alltogether!
	 * 
	 * @param synchronizeFixedCellWidth
	 */
	public void setSynchronizeFixedCellWidth(boolean synchronizeFixedCellWidth) {
		this.synchronizeFixedCellWidth = synchronizeFixedCellWidth;
	}

	protected ComponentListener createSizeTracker() {
		return new SizeTracker();
	}

	public void setTrackViewportWidth(boolean trackViewportWidth) {
		if(trackViewportWidth==this.trackViewportWidth) {
			return;
		}
		
		boolean oldValue = this.trackViewportWidth;
		this.trackViewportWidth = trackViewportWidth;
		
		revalidate();
		
		firePropertyChange("trackViewportWidth", oldValue, trackViewportWidth); //$NON-NLS-1$
	}
	
	protected class SizeTracker extends ComponentAdapter {
		
		boolean ignoreResize = false;

		@Override
		public void componentResized(ComponentEvent e) {
			if(!isSynchronizeFixedCellWidth() || getWidth()==0) {
				return;
			}
			try {
				if(!ignoreResize) {
					setFixedCellWidth(getWidth());
				}
			} finally {
				ignoreResize = !ignoreResize;
			}
		}
	}
}
