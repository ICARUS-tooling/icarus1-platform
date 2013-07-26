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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

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
