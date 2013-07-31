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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ListMouseDelegate extends MouseAdapter {
	
	@Override
	public void mousePressed(MouseEvent e) {
		dispatchMouseEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dispatchMouseEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		dispatchMouseEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		dispatchMouseEvent(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		dispatchMouseEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dispatchMouseEvent(e);
	}

	private void dispatchMouseEvent(MouseEvent e) {
		
		JList<?> list = (JList<?>) e.getSource();
		
		Point p = e.getPoint();
		int row = list.locationToIndex(p);
		if(row==-1) {
			return;
		}
		Rectangle bounds = list.getCellBounds(row, row);
		if(!bounds.contains(p)) {
			return;
		}

		p.translate(-bounds.x, -bounds.y);
		
		dispatchEvent(list, bounds, p, e);
	}
	
	protected void dispatchEvent(JList<?> list, Rectangle bounds, Point p, MouseEvent e) {
		Component target = null;
		ListCellRenderer<?> renderer = list.getCellRenderer();
		if(renderer instanceof Component) {
			target = SwingUtilities.getDeepestComponentAt(
					(Component)renderer, p.x, p.y);
		}
		
		if(target!=null) {
			MouseEvent newEvent = new MouseEvent(target, e.getID(), 
					e.getWhen(), e.getModifiers(), p.x, p.y, 
					e.getClickCount(), e.isPopupTrigger());
			target.dispatchEvent(newEvent);
			list.repaint(bounds);
		}
	}
}
