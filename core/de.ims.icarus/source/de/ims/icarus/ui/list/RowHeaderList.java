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

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RowHeaderList extends JList<String> {

	private static final long serialVersionUID = -4756968440956663052L;

	private boolean resizingAllowed = false;
	private ResizeHandler resizeHandler;
	
	private int minimumCellWidth = -1;
	
	private ListSelectionModel targetSelectionModel;
	private SelectionSynchronizer selectionSynchronizer;
	
	public RowHeaderList() {
		// no-op
	}
	
	public RowHeaderList(ListModel<String> model) {
		super(model);
	}
	
	public RowHeaderList(ListModel<String> model, ListSelectionModel targetSelectionModel) {
		this(model);
		
		setTargetSelectionModel(targetSelectionModel);
		
		addListSelectionListener(getSelectionSynchronizer());
	}

	public ListSelectionModel getTargetSelectionModel() {
		return targetSelectionModel;
	}
	
	private SelectionSynchronizer getSelectionSynchronizer() {
		if(selectionSynchronizer==null) {
			selectionSynchronizer = new SelectionSynchronizer();
		}
		
		return selectionSynchronizer;
	}

	public void setTargetSelectionModel(ListSelectionModel targetSelectionModel) {
		
		if(this.targetSelectionModel!=null) {
			this.targetSelectionModel.removeListSelectionListener(getSelectionSynchronizer());
		}
		
		this.targetSelectionModel = targetSelectionModel;
		
		if(this.targetSelectionModel!=null) {
			this.targetSelectionModel.addListSelectionListener(getSelectionSynchronizer());
		}
		
		setSelectionMode(targetSelectionModel==null ? 
				ListSelectionModel.SINGLE_SELECTION : targetSelectionModel.getSelectionMode());
	}

	public int getMinimumCellWidth() {
		return minimumCellWidth;
	}

	public void setMinimumCellWidth(int minimumCellWidth) {
		this.minimumCellWidth = minimumCellWidth;
		
		if(minimumCellWidth>getFixedCellWidth()) {
			setFixedCellWidth(minimumCellWidth);
		}
	}

	public boolean isResizingAllowed() {
		return resizingAllowed;
	}

	public void setResizingAllowed(boolean resizingAllowed) {
		if(this.resizingAllowed==resizingAllowed) {
			return;
		}
		
		boolean oldValue = this.resizingAllowed;
		this.resizingAllowed = resizingAllowed;
		
		refreshResizeHandler();
		
		firePropertyChange("resizingAllowed", oldValue, resizingAllowed); //$NON-NLS-1$
	}

	private void refreshResizeHandler() {
		if(!resizingAllowed && resizeHandler==null) {
			return;
		}
		
		if(resizingAllowed) {
			if(resizeHandler==null) {
				resizeHandler = new ResizeHandler();
			}
			addMouseListener(resizeHandler);
			addMouseMotionListener(resizeHandler);
		} else {
			removeMouseListener(resizeHandler);
			removeMouseMotionListener(resizeHandler);
		}
	}

	private final static Cursor RESIZE_CURSOR = Cursor.getPredefinedCursor(
			Cursor.E_RESIZE_CURSOR);
	
	private class ResizeHandler extends MouseAdapter {
		
		private Cursor defaultCursor;
		private int pressedX;
		private boolean resizing = false;
		private int startWidth;

		@Override
		public void mousePressed(MouseEvent e) {
			if(getCursor()==RESIZE_CURSOR || true) {
				pressedX = e.getX();
				resizing = true;
				//startWidth = getPreferredScrollableViewportSize().width;
				startWidth = getWidth();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			pressedX = -1;
			resizing = false;
			if(defaultCursor!=null) {
				setCursor(defaultCursor);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(resizing) {				
				int width =  startWidth + e.getX() - pressedX;
				
				if(minimumCellWidth!=-1) {
					width = Math.max(width, minimumCellWidth);
				}
				
				setFixedCellWidth(width);
				revalidate();
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int dif = getWidth()-e.getX();
			
			if(dif<=5) {
				if(defaultCursor==null) {
					defaultCursor = getCursor();
				}
				if(getCursor()!=RESIZE_CURSOR) {
					setCursor(RESIZE_CURSOR);
				}
			} else if(defaultCursor!=null) {
				if(getCursor()!=defaultCursor) {
					setCursor(defaultCursor);
				}
				defaultCursor = null;
			}
		}		
	}
	
	private class SelectionSynchronizer implements ListSelectionListener {
		
		private boolean ignoreChanges = false;

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(ignoreChanges || getTargetSelectionModel()==null) {
				return;
			}
			
			ListSelectionModel model = null;
			
			if(e.getSource() instanceof ListSelectionModel) {
				model = (ListSelectionModel) e.getSource();
			} else if(e.getSource()==RowHeaderList.this) {
				model = getSelectionModel();
			}
			
			if(model==null) {
				return;
			}
			
			try {
				ignoreChanges = true;
				
				if(model==getSelectionModel()) {
					ListUtils.copySelectionState(model, getTargetSelectionModel());
				} else {
					ListUtils.copySelectionState(getTargetSelectionModel(), getSelectionModel());
				}
			} finally {
				ignoreChanges = false;
			}
		}
		
	}
}
