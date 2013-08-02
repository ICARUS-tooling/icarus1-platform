/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus G�rtner and Gregor Thiele
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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramQTableCellRenderer extends JLabel implements TableCellRenderer {

    public NGramQTableCellRenderer() {
        this.setOpaque(true);
        //this.setHorizontalAlignment(JLabel.CENTER);
    }
	
	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent (JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		switch (column) {
		case 0:
			setBackground(Color.CYAN);
			JCheckBox cb = new JCheckBox();
			if (value.equals(true)){
				cb.setSelected(true);
			}
			this.setText((value != null)?value.toString():"");
			//tableRenderer = (DefaultTableCellRenderer)tableRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			break;

		default:
			this.setText((value != null)?value.toString():"");
			// tableRenderer = (DefaultTableCellRenderer)tableRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			break;
		}
       // table.setOpaque(true);
        //tableRenderer = (DefaultTableCellRenderer)tableRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
        return this;
	}

}
