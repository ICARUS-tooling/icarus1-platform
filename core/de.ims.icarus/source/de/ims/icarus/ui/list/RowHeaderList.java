/*
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

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
