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
package de.ims.icarus.util.data;

import java.awt.Component;

import javax.swing.JList;

import de.ims.icarus.ui.list.TooltipListCellRenderer;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DataListCellRenderer extends TooltipListCellRenderer {

	private static final long serialVersionUID = -3488624857141838661L;
	
	private static DataListCellRenderer sharedInstance;
	
	public static DataListCellRenderer getSharedIntance() {
		if(sharedInstance==null) {
			sharedInstance = new DataListCellRenderer();
		}
		return sharedInstance;
	}

	public DataListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		value = (index+1)+": "+value; //$NON-NLS-1$
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		return this;
	}

}
