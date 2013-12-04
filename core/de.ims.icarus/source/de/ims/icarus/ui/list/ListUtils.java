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

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ListUtils {

	private ListUtils() {
		// no-op
	}

	public static <T extends Object> int indexOf(T item, ListModel<T> model) {
		if(model==null)
			throw new NullPointerException("Invalid list model"); //$NON-NLS-1$
		
		if(item==null) {
			return -1;
		}
		
		for(int i=0; i<model.getSize(); i++) {
			if(item.equals(model.getElementAt(i))) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static void copySelectionState(ListSelectionModel m1, ListSelectionModel m2) {
		m2.setValueIsAdjusting(true);
		try {
			m2.clearSelection();
			
			if(m1.isSelectionEmpty()) {
				return;
			}
			
			switch (m1.getSelectionMode()) {
			case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION:
				for(int i=m1.getMinSelectionIndex(); i<=m1.getMaxSelectionIndex(); i++) {
					// TODO group intervals and apply selection on the target
				}
				break;

			default:
				m2.setSelectionInterval(m1.getMinSelectionIndex(), m1.getMaxSelectionIndex());
				break;
			}
		} finally {
			m2.setValueIsAdjusting(false);
		}
	}
}
