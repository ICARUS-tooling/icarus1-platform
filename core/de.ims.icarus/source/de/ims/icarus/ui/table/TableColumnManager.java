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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.BiDiMap;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.data.DataSource;
import de.ims.icarus.util.data.DataSourceFactory;
import de.ims.icarus.util.id.DuplicateIdentifierException;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TableColumnManager extends DefaultTableColumnModel implements ChangeListener {
	
	private static final long serialVersionUID = -3540527402222543450L;

	private DataSource dataSource;
	
	private List<String> availableColumns = new ArrayList<>();
	private Map<String, TableColumn> columnLookup = new BiDiMap<>();
	private List<String> visibleColumns = new ArrayList<>();
	
	private boolean ignoreChange = false; 
	
	public TableColumnManager(DataSource dataSource) {
		setDataSource(dataSource);
	}

	public TableColumnManager(ConfigRegistry registry, String path) {
		if(path==null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		
		if(registry==null) {
			registry = ConfigRegistry.getGlobalRegistry();
		}
		
		Handle handle = registry.getHandle(path);
		if(handle==null)
			throw new IllegalArgumentException("Unknown path: "+path); //$NON-NLS-1$

		setDataSource(DataSourceFactory.getInstance().getConfigDataSource(handle, null));
	}
	
	public void registerColumn(String id, TableColumn column) {
		if(column==null)
			throw new NullPointerException("Invalid column");
		
		if(columnLookup.containsKey(id))
			throw new DuplicateIdentifierException("Column id already in use: "+id);
		if(columnLookup.containsValue(id))
			throw new IllegalArgumentException("Column already in use: "+id);
		
		column.setIdentifier(id);
		
		columnLookup.put(id, column);
		availableColumns.add(id);
	}
	
	public void unregisterColumn(String id) {
		if(id==null)
			throw new NullPointerException("Invalid id");
		
		TableColumn column = columnLookup.get(id);
		if(column==null)
			throw new IllegalArgumentException("Unknown column id: "+id);
		
		columnLookup.remove(id);
		availableColumns.remove(column);
	}
	
	public List<String> getVisibleColumns() {
		return new ArrayList<>(visibleColumns);
	}
	
	public TableColumn getColumn(String id) {
		if(id==null)
			throw new NullPointerException("Invalid id");
		
		TableColumn column = columnLookup.get(id);
		if(column==null)
			throw new IllegalArgumentException("No column registered for id: "+id);
		
		return column;
	}
	
	public List<String> getAvailableColumns() {
		return new ArrayList<>(availableColumns);
	}
	
	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		if(dataSource==null)
			throw new IllegalStateException("No data source available");
		
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		if(dataSource==null)
			throw new NullPointerException("Invalid data source");
		
		if(this.dataSource==dataSource) {
			return;
		}
		
		if(this.dataSource!=null) {
			this.dataSource.removeChangeListener(this);
		}
		
		this.dataSource = dataSource;
		
		this.dataSource.addChangeListener(this);
	}
	
	private void rebuild() {
		for(int i=getColumnCount()-1; i>-1; i--) {
			removeColumn(getColumn(i));
		}
		
		if(visibleColumns.isEmpty()) {
			for(String id : availableColumns) {
				addColumn(getColumn(id));
			}
		} else {
			for(String id : visibleColumns) {
				addColumn(getColumn(id));
			}
		}
	}

	public void rebuild(String...ids) {
		rebuild(CollectionUtils.asList(ids));
	}
	
	public void rebuild(List<String> ids) {
		if(ids==null)
			throw new NullPointerException("Invalid ids");
		
		visibleColumns.clear();
		
		Set<String> availableIds = columnLookup.keySet();
		for(String id : ids) {
			if(!availableIds.contains(id)) {
				LoggerFactory.warning(this, "Unknown column id: "+id);
				continue;
			}
			
			visibleColumns.add(id);
		}
		
		rebuild();
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if(ignoreChange) {
			ignoreChange = false;
			return;
		}

		visibleColumns.clear();
		
		String info = (String) getDataSource().getData();
		
		if(info!=null && !info.isEmpty()) {
			String[] ids = info.split(";");
			CollectionUtils.feedItems(visibleColumns, ids);
		}
		
		rebuild();
	}
}
