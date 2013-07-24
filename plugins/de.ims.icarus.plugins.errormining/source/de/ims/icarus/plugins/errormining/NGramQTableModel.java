/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.dialog.DialogFactory;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramQTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -4442299092369264073L;
	
	protected ArrayList<NGramQAttributes> qList = null;

	
	
	public NGramQTableModel(){
		//noop
	}
	
	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
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
		return 3;
	}
	
	
	/**
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {

	      switch (columnIndex) {
		        case 0: return ResourceManager.getInstance().get(
		          		"plugins.errormining.labels.Included"); //$NON-NLS-1$
	            case 1: return ResourceManager.getInstance().get(
	            		"plugins.errormining.labels.Key"); //$NON-NLS-1$
	            case 2: return ResourceManager.getInstance().get(
	            		"plugins.errormining.labels.Value"); //$NON-NLS-1$
	            default: break;
	        }
	        return null;
	}
	

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return qList==null ? 0 : qList.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(qList==null) {
			return null;
		}
        switch (columnIndex)
        {
        	case 0: return qList.get(rowIndex).isInclude();
            case 1: return qList.get(rowIndex).getKey();
            case 2: return qList.get(rowIndex).getValue();
            default: break;
        }
        return null;
	}
	
	
	public void reload(List<NGramQAttributes> list) {		
		if(list==null) {
				qList = null;
		} else {		
			qList = new ArrayList<NGramQAttributes>(list);
		}

		fireTableDataChanged();
	}
	
	
	public boolean hasInputAttributeChanges(List<NGramQAttributes> ngramqList) {
		if (qList.size() != ngramqList.size()){
			return true;
		}

		return false;
	}
	
	private boolean checkDuplicateEntry(NGramQAttributes att){
		return qList.contains(att);
		
	}
	
	
	/**
	 * @param wio
	 */
	public void addQueryAttribute(NGramQAttributes att) {
		//TODO needed?
		if (qList == null){
			qList = new ArrayList<NGramQAttributes>();
		}
		if (checkDuplicateEntry(att)){
			DialogFactory.getGlobalFactory().showError(
					null,
					"plugins.errormining.dialogs.duplicateEntry.title", //$NON-NLS-1$
					"plugins.errormining.dialogs.duplicateEntry.message", //$NON-NLS-1$
					att.getKey());
			return;
		}
		qList.add(att);
		fireTableDataChanged();
	}
	
	/**
	 * @param key
	 * @param object
	 */
	public void deleteQueryAttribute(int index) {
		qList.remove(qList.get(index));
		fireTableDataChanged();	
	}
	
	/**
	 * @param key
	 * @param object
	 */
	public void removeAllQueryAttributes() {
		qList.clear();
		fireTableDataChanged();	
	}

	/**
	 * @param wio
	 * @param row 
	 */
	public void setQueryAttributes(NGramQAttributes att, int index) {

		if (index == -1){
			addQueryAttribute(att);
		}
		else {
			if (checkDuplicateEntry(att)){
				DialogFactory.getGlobalFactory().showError(
						null,
						"plugins.errormining.dialogs.duplicateEntry.title", //$NON-NLS-1$
						"plugins.errormining.dialogs.duplicateEntry.message", //$NON-NLS-1$
						att.getKey());
				return;
			}
			qList.get(index).setKey(att.getKey());
			qList.get(index).setValue(att.getValue());
		}
		fireTableDataChanged();
		
	}

	/**
	 * @param row
	 */
	public void setInclueQueryAttribute(int index) {
		boolean include = qList.get(index).isInclude();
		qList.get(index).setInclude(!include);
		fireTableDataChanged();
		
	}


}
