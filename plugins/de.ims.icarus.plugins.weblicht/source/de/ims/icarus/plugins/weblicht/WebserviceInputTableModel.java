/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.weblicht;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.ims.icarus.plugins.weblicht.webservice.Webservice;
import de.ims.icarus.plugins.weblicht.webservice.WebserviceIOAttributes;
import de.ims.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import de.ims.icarus.resources.ResourceManager;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceInputTableModel extends AbstractTableModel {


	private static final long serialVersionUID = -7108481950763435345L;
	
	protected ArrayList<WebserviceIOAttributes> ioList = null;
	//protected Webservice webservice = new Webservice();


	public WebserviceInputTableModel() {
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
	            		"plugins.weblicht.labels.webservice.AttributeName"); //$NON-NLS-1$
	            case 1: return ResourceManager.getInstance().get(
	            		"plugins.weblicht.labels.webservice.AttributeValue"); //$NON-NLS-1$
	            default: break;
	        }
	        return null;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return ioList==null ? 0 : ioList.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(ioList==null) {
			return null;
		}
        switch (columnIndex)
        {
            case 0: return ioList.get(rowIndex).getAttributename();
            case 1: return ioList.get(rowIndex).getAttributevalues();
            default: break;
        }
        return null;
	}


	/*
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub

	}
	*/

	public void reload(Webservice webservice) {		
		if(webservice==null) {
				ioList = null;
		} else {		
			ioList = new ArrayList<WebserviceIOAttributes>(
					(ArrayList<WebserviceIOAttributes>)
					WebserviceRegistry.getInstance().getWebserviceInput(webservice));
		}

		fireTableDataChanged();
	}

	
	protected int indexOf (WebserviceIOAttributes wio) {
		int index = -1;
		for (int i = 0; i < ioList.size();i++){
			if (ioList.get(i).getAttributename().equals(wio.getAttributename())){
				return i;
			}
		}
		return index;
	}


	
	
	public boolean hasInputAttributeChanges(List<WebserviceIOAttributes> wiol) {
		if (ioList.size() != wiol.size()){
			return true;
		}

		return false;
	}

	/**
	 * @param wio
	 */
	public void addInputAttribute(WebserviceIOAttributes wio) {
		ioList.add(wio);
		fireTableDataChanged();;
	}
	
	/**
	 * @param key
	 * @param object
	 */
	public void deleteInputAttribute(int index) {
		ioList.remove(ioList.get(index));
		fireTableDataChanged();	
	}

	/**
	 * @param wio
	 * @param row 
	 */
	public void setInputAttributes(WebserviceIOAttributes wio, int index) {
		//int index = indexOf(wio);
		
		if (index == -1){
			addInputAttribute(wio);
		}
		else {
			ioList.get(index).setAttributename(wio.getAttributename());
			ioList.get(index).setAttributevalues(wio.getAttributevalues());
		}
		fireTableDataChanged();
		
	}

}
