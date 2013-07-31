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
package de.ims.icarus.plugins.weblicht;

import java.util.ArrayList;

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
public class WebserviceOutputTableModel extends AbstractTableModel {


		private static final long serialVersionUID = -7108481950763435345L;
		
		ArrayList<WebserviceIOAttributes> ooList = null;
		
	    public WebserviceOutputTableModel()  {  
	    	//nopo
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
			return ooList==null ? 0 : ooList.size();
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(ooList==null) {
				return null;
			}
	        switch (columnIndex)
	        {
	            case 0: return ooList.get(rowIndex).getAttributename();
	            case 1: return ooList.get(rowIndex).getAttributevalues();
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
				ooList = null;
			} else {		
				ooList = new ArrayList<WebserviceIOAttributes>((ArrayList<WebserviceIOAttributes>) WebserviceRegistry.getInstance().getWebserviceOutput(webservice));
			}
			fireTableDataChanged();
		}

		
		protected int indexOf (WebserviceIOAttributes wio) {
			int index = -1;
			for (int i = 0; i < ooList.size();i++){
				if (ooList.get(i).getAttributename().equals(wio.getAttributename())){
					return i;
				}
			}
			return index;
		}


		/**
		 * @param wio
		 */
		public void addOutputAttribute(WebserviceIOAttributes wio) {
			ooList.add(wio);
			fireTableDataChanged();
		}


		/**
		 * @param key
		 * @param object
		 */
		public void deleteOutputAttribute(int index) {
			ooList.remove(ooList.get(index));
			fireTableDataChanged();		
		}

		/**
		 * @param wio
		 * @param index 
		 */
		public void setOutputAttributes(WebserviceIOAttributes wio, int index) {
			//add new item
			if (index == -1){
				addOutputAttribute(wio);
			}
			//edit item
			else {
				ooList.get(index).setAttributename(wio.getAttributename());
				ooList.get(index).setAttributevalues(wio.getAttributevalues());
			}
			fireTableDataChanged();			
		}

}
