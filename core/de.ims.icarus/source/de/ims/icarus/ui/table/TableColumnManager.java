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

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.resources.Localizable;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.data.DataSource;
import de.ims.icarus.util.data.DataSourceFactory;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TableColumnManager implements ChangeListener, Localizable {
	
	private DataSource dataSource;
	
	private DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
	
	public TableColumnManager(Handle handle) {
		if(handle==null)
			throw new IllegalArgumentException("Invalid handle"); //$NON-NLS-1$
		
		init(handle);
	}

	public TableColumnManager(ConfigRegistry registry, String path) {
		if(path==null)
			throw new IllegalArgumentException("Invalid path"); //$NON-NLS-1$
		
		if(registry==null) {
			registry = ConfigRegistry.getGlobalRegistry();
		}
		
		Handle handle = registry.getHandle(path);
		if(handle==null)
			throw new IllegalArgumentException("Unknown path: "+path); //$NON-NLS-1$
		
		init(handle);
	}
	
	private void init(Handle handle) {
		dataSource = DataSourceFactory.getInstance().getConfigDataSource(handle, this);
		
		ResourceManager.getInstance().getGlobalDomain().addItem(this);
	}
	
	public TableColumnModel getColumnModel() {
		return columnModel;
	}
	
	@SuppressWarnings("unchecked")
	private List<ColumnInfo> getInfos() {
		return (List<ColumnInfo>) dataSource.getData();
	}
	
	public ColumnInfo getInfo(int index) {
		List<ColumnInfo> infos = getInfos();
		return infos==null ? null : infos.get(index);
	}

	/**
	 * @see de.ims.icarus.resources.Localizable#localize()
	 */
	@Override
	public void localize() {
		List<ColumnInfo> infos = getInfos();
		if(infos==null) {
			return;
		}
		
		for(int i=0; i<infos.size(); i++) {
			if(i>=infos.size()) {
				break;
			}
			
			TableColumn column = columnModel.getColumn(i);
			column.setHeaderValue(infos.get(i));
		}
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		List<ColumnInfo> infos = getInfos();
		if(infos==null) {
			return;
		}
		
		for(int i=columnModel.getColumnCount(); i>-1; i++) {
			columnModel.removeColumn(columnModel.getColumn(i));
		}
		
		for(ColumnInfo info : infos) {
			if(!info.isActive() && !info.isRequired()) {
				continue;
			}
			
			TableColumn column = new TableColumn(columnModel.getColumnCount());
			column.setResizable(info.isResizable());
			column.setMinWidth(info.getMinWidth());
			column.setMaxWidth(info.getMaxWidth());
			column.setPreferredWidth(info.getPreferredWidth());
			column.setHeaderValue(info);
			
			columnModel.addColumn(column);
		}
	}
}
