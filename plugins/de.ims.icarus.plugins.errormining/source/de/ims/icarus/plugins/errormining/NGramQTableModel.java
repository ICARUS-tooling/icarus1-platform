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
        switch (columnIndex) {
        case 0:
            return Boolean.class;
        case 1:
            return String.class;
        case 2:
            return String.class;
        default:
            return String.class;
        }
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
	
	
	/**
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {  
        if(columnIndex == 0){  
            //boolean isIncluded = (Boolean)value; 
            setInclueQueryAttribute(rowIndex); 
        }
        else {
        	NGramQAttributes att = new NGramQAttributes();
	        //key
	        if(columnIndex == 1){
	        	String keynew = (String) value;
	        	
	        	//no Changes
	        	if (keynew.equals(qList.get(rowIndex).getKey())){
	        		return;
	        	}
	        	
	        	att.setKey(keynew);
	        	att.setValue(getValueAt(rowIndex, columnIndex+1).toString());
	            setQueryAttributes(att, rowIndex);
	        }
	        //value
	        if(columnIndex == 2){
	        	String valnew = (String) value;
	        	
	        	//no Changes
	        	if (valnew.equals(qList.get(rowIndex).getValue())){
	        		return;
	        	}
	        	
	        	att.setKey(getValueAt(rowIndex, columnIndex-1).toString());
	        	att.setValue(valnew); 
	        	setQueryValue(att, rowIndex);
	        }
        }

        super.setValueAt(value, rowIndex, columnIndex);  
    } 

	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
//        if(columnIndex == 0){  
//            return true;  
//        }  
//		return super.isCellEditable(rowIndex, columnIndex);
		return true; 
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
	
	
	private boolean checkDuplicateEntry(NGramQAttributes att, int index){
		if (qList.contains(att) && index == qList.indexOf(att)){
			return false;
		}
		return qList.contains(att);
		
	}
	
	
	/**
	 * @param wio
	 */
	public void addQueryAttribute(NGramQAttributes att, int index) {
		//TODO needed?
		if (qList == null){
			qList = new ArrayList<NGramQAttributes>();
		}
		if (checkDuplicateEntry(att, index)){
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
			addQueryAttribute(att, index);
		}
		else {
	
			if (checkDuplicateEntry(att, index)){
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
	
	public void setQueryValue(NGramQAttributes att, int index) {

		if (index == -1){
			addQueryAttribute(att,index);
		}
		
		else {
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
