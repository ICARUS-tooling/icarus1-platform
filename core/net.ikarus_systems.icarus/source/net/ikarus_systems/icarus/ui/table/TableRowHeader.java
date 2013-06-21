/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.table;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TableRowHeader extends JList<String> {

	private static final long serialVersionUID = -4756968440956663052L;

	private boolean resizingAllowed = false;
	private ResizeHandler resizeHandler;
	
	private Dimension preferredScrollableViewportSize;
	
	public TableRowHeader() {
		// no-op
	}
	
	public TableRowHeader(ListModel<String> model) {
		super(model);
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

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		if(preferredScrollableViewportSize!=null) {
			return preferredScrollableViewportSize;
		} else {
			return super.getPreferredScrollableViewportSize();
		}
	}

	public void setPreferredScrollableViewportSize(
			Dimension preferredScollableViewportSize) {
		this.preferredScrollableViewportSize = preferredScollableViewportSize;
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
				startWidth = getPreferredScrollableViewportSize().width;
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
				width = Math.max(width, getFixedCellWidth());
				
				setPreferredScrollableViewportSize(new Dimension(width, 50));

		        JScrollPane scrollPane = (JScrollPane)getParent().getParent();
		        scrollPane.revalidate();
		        
		        // TODO somehow dead slow repaint?
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
}
