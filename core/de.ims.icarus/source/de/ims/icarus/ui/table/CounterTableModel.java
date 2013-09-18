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
package de.ims.icarus.ui.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.ims.icarus.util.Counter;
import de.ims.icarus.util.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */

public class CounterTableModel<E extends Comparable<? super E>> extends AbstractTableModel {

	private static final long serialVersionUID = 6898056399696712268L;
	
	private Counter<E> counter;
	private List<E> keyList;
	
	private boolean showIndex = true;

	public Counter<E> getCounter() {
		return counter;
	}

	public void setCounter(Counter<E> counter) {
		this.counter = counter;
		
		rebuild();
	}
	
	public boolean isShowIndex() {
		return showIndex;
	}

	public void setShowIndex(boolean showIndex) {
		if(this.showIndex==showIndex) {
			return;
		}
		
		this.showIndex = showIndex;
		
		fireTableStructureChanged();
	}

	public void rebuild() {
		keyList = null;
		
		if(counter!=null) {
			List<E> tmp = new ArrayList<>(counter.getItems());
			Collections.sort(tmp);
			keyList = tmp;
		}
		
		fireTableDataChanged();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return keyList==null ? 0 : keyList.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return showIndex ? 3 : 2;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(keyList==null || counter==null) {
			return null;
		}
		
		if(showIndex) {
			columnIndex--;
		}
		
		if(columnIndex==-1) {
			return StringUtil.formatDecimal(rowIndex+1);
		}
		
		E key = keyList.get(rowIndex); 
		
		return columnIndex==0 ? key : counter.getCount(key);
	}
	
}
