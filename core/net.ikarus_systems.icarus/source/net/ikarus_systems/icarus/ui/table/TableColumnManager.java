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

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.resources.Localizable;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.util.data.DataSource;
import net.ikarus_systems.icarus.util.data.DataSourceFactory;


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
	 * @see net.ikarus_systems.icarus.resources.Localizable#localize()
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
