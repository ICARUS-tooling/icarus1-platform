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
		return 2;
	}
	
	
	/**
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {

	      switch (columnIndex) {
	            case 0: return ResourceManager.getInstance().get(
	            		"plugins.errormining.labels.Key"); //$NON-NLS-1$
	            case 1: return ResourceManager.getInstance().get(
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
            case 0: return qList.get(rowIndex).getKey();
            case 1: return qList.get(rowIndex).getValue();
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
	
	
	/**
	 * @param wio
	 */
	public void addQueryAttribute(NGramQAttributes att) {
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
	 * @param wio
	 * @param row 
	 */
	public void setQueryAttributes(NGramQAttributes att, int index) {

		if (index == -1){
			addQueryAttribute(att);
		}
		else {
			qList.get(index).setKey(att.getKey());
			qList.get(index).setValue(att.getValue());
		}
		fireTableDataChanged();
		
	}


}
