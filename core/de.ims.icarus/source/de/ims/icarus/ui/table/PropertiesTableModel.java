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
package de.ims.icarus.ui.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import de.ims.icarus.resources.ResourceManager;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PropertiesTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -8409765555152015560L;
	
	protected List<String> keys;
	protected Map<String, Object> properties;
	
	protected String keyLabel = "labels.property"; //$NON-NLS-1$
	protected String valueLabel = "labels.value"; //$NON-NLS-1$
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
		
		if(keys==null) {
			keys = new ArrayList<>();
		}		
		keys.clear();
		
		if(properties!=null && !properties.isEmpty()) {
			keys.addAll(properties.keySet());
			Collections.sort(keys);
		}
		
		fireTableDataChanged();
	}
	
	public String getKeyLabel() {
		return keyLabel;
	}

	public String getValueLabel() {
		return valueLabel;
	}

	public void setKeyLabel(String keyLabel) {
		this.keyLabel = keyLabel;
		fireTableStructureChanged();
	}

	public void setValueLabel(String valueLabel) {
		this.valueLabel = valueLabel;
		fireTableStructureChanged();
	}

	public void clear() {
		if(properties==null || properties.isEmpty()) {
			return;
		}
		
		properties.clear();
		keys.clear();

		fireTableDataChanged();
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public Object getValue(String key) {
		return properties==null ? null : properties.get(key);
	}
	
	public void setValue(String key, Object value) {
		if(properties==null) {
			properties = new HashMap<>();
			keys = new ArrayList<>();
		}
		
		if(value==null) {
			properties.remove(key);
		} else {
			properties.put(key, value);
		}
		keys.clear();
		keys.addAll(properties.keySet());
		Collections.sort(keys);
		fireTableDataChanged();
	}
	
	public String getKey(int rowIndex) {
		return keys==null ? null : keys.get(rowIndex);
	}
	
	public boolean containsKey(String key) {
		return properties==null ? false : properties.containsKey(key);
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return keys==null ? 0 : keys.size();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		String key = column==0 ? keyLabel : valueLabel;
		return ResourceManager.getInstance().get(key);
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(keys==null) {
			return null;
		}
		
		String key = keys.get(rowIndex);
		
		if(columnIndex==0) {
			return key;
		} else {
			return properties.get(key);
		}
	}
	
}