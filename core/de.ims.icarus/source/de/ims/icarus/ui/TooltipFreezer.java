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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ToolTipManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TooltipFreezer extends MouseAdapter {
	
	private int dismissDelayReminder = -1;
	private int initialDelayReminder = -1;

	public TooltipFreezer() {
		// no-op
	}
	
	/**
	 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		initialDelayReminder = ToolTipManager.sharedInstance().getInitialDelay();
		dismissDelayReminder = ToolTipManager.sharedInstance().getDismissDelay();
		
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		ToolTipManager.sharedInstance().setInitialDelay(0);
	}
	
	/**
	 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		if(dismissDelayReminder!=-1) {
			ToolTipManager.sharedInstance().setDismissDelay(dismissDelayReminder);
			dismissDelayReminder = -1;
		}
		if(initialDelayReminder!=-1) {
			ToolTipManager.sharedInstance().setInitialDelay(initialDelayReminder);
			initialDelayReminder = -1;
		}
	}

}
