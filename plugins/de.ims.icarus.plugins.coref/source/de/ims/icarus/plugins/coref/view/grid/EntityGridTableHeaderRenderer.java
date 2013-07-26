/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.grid;

import java.awt.Component;

import javax.swing.JTable;

import de.ims.icarus.ui.table.MultilineTableHeaderRenderer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridTableHeaderRenderer extends MultilineTableHeaderRenderer {

	private static final long serialVersionUID = -3297234662414629249L;

	public EntityGridTableHeaderRenderer() {
		// no-op
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// TODO Auto-generated method stub
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}

}
