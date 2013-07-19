/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.table;

import javax.swing.AbstractListModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TableIndexListModel extends AbstractListModel<String> implements TableModelListener {

	private static final long serialVersionUID = 2382366113702464508L;
	
	protected TableModel tableModel;
	
	public TableIndexListModel() {
		// no-op
	}
	
	public TableIndexListModel(TableModel source) {
		setTableModel(source);
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public void setTableModel(TableModel tableModel) {
		int sizeBefore = getSize();
		
		if(this.tableModel!=null) {
			this.tableModel.removeTableModelListener(this);
		}
		
		this.tableModel = tableModel;
		
		if(this.tableModel!=null) {
			this.tableModel.addTableModelListener(this);
		}
		
		int sizeAfter = getSize();
		
		fireContentsChanged(this, 0, CollectionUtils.max(
				0, sizeBefore-1, sizeAfter-1));
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return tableModel==null ? 0 : tableModel.getRowCount();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public String getElementAt(int index) {
		return StringUtil.formatDecimal(index+1);
	}

	/**
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		fireContentsChanged(this, 0, Math.max(0, getSize()-1));
	}
}
